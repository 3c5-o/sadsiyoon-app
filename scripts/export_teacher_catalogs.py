#!/usr/bin/env python3
"""Export every teacher lecture catalogue/index from SADESION without downloading media."""
from __future__ import annotations

import csv
import io
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "dist" / "sadesion-all-teacher-catalogs.zip"
DATA_DIRS = (
    ROOT / "research" / "edison" / "2026",
    ROOT / "app" / "src" / "main" / "assets" / "catalog",
    ROOT / "app" / "src" / "main" / "assets" / "catalog_2027",
    ROOT / "app" / "src" / "main" / "assets" / "exams",
)
DATA_FILES = (
    ROOT / "app" / "src" / "main" / "java" / "com" / "sadsiyoon" / "app" / "Catalog.java",
)

def collect_files() -> list[Path]:
    files: set[Path] = set()
    for directory in DATA_DIRS:
        if directory.exists():
            files.update(p for p in directory.rglob("*") if p.is_file())
    files.update(p for p in DATA_FILES if p.is_file())
    return sorted(files, key=lambda p: p.relative_to(ROOT).as_posix())

def chapters_of(doc: dict) -> list:
    value = doc.get("chapters", [])
    return value if isinstance(value, list) else []

def lessons_of(chapter: dict) -> list:
    for key in ("lessons", "lectures"):
        value = chapter.get(key)
        if isinstance(value, list):
            return value
    return []

def read_json(path: Path) -> dict | None:
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
        return value if isinstance(value, dict) else None
    except (OSError, json.JSONDecodeError):
        return None

def csv_bytes(headers: list[str], rows: list[dict]) -> bytes:
    buffer = io.StringIO(newline="")
    writer = csv.DictWriter(buffer, fieldnames=headers, extrasaction="ignore")
    writer.writeheader()
    writer.writerows(rows)
    return buffer.getvalue().encode("utf-8-sig")

def build_indexes(files: list[Path]) -> tuple[bytes, bytes, int]:
    manifest_rows: list[dict] = []
    lecture_rows: list[dict] = []
    for path in files:
        if path.suffix.lower() != ".json":
            continue
        doc = read_json(path)
        if doc is None:
            continue
        rel = path.relative_to(ROOT).as_posix()
        if "/catalog/" not in rel and "/catalog_2027/" not in rel and "/research/edison/2026/" not in rel:
            continue
        chapters = chapters_of(doc)
        total = 0
        for chapter_num, chapter in enumerate(chapters, 1):
            if not isinstance(chapter, dict):
                continue
            entries = lessons_of(chapter)
            total += len(entries)
            chapter_title = chapter.get("title") or chapter.get("name") or f"الفصل {chapter_num}"
            for fallback_index, lesson in enumerate(entries, 1):
                if not isinstance(lesson, dict):
                    continue
                lecture_rows.append({
                    "teacher": doc.get("teacher", ""),
                    "batch": doc.get("batch", ""),
                    "subject": doc.get("subject", ""),
                    "chapter": chapter_title,
                    "lecture_index": lesson.get("index", fallback_index),
                    "lecture_title": lesson.get("title", ""),
                    "url": lesson.get("url", ""),
                    "provider": lesson.get("provider", ""),
                    "access": lesson.get("access", ""),
                    "verification": lesson.get("verification", lesson.get("verified", lesson.get("playback_tested", ""))),
                    "source_file": rel,
                })
        manifest_rows.append({
            "source_file": rel,
            "teacher": doc.get("teacher", ""),
            "batch": doc.get("batch", ""),
            "subject": doc.get("subject", ""),
            "chapter_count": len(chapters),
            "indexed_lectures": total,
            "reported_total_lessons": doc.get("reported_total_lessons", doc.get("observed_lessons", "")),
            "extracted_lessons": doc.get("extracted_lessons", doc.get("public_urls_recorded", "")),
            "remaining_unindexed": doc.get("remaining_unindexed", ""),
            "verification_or_coverage": doc.get("verification", doc.get("coverage", "")),
            "restrictions_or_notes": doc.get("restrictions", doc.get("source_notes", "")),
        })
    manifest = csv_bytes([
        "source_file", "teacher", "batch", "subject", "chapter_count",
        "indexed_lectures", "reported_total_lessons", "extracted_lessons",
        "remaining_unindexed", "verification_or_coverage", "restrictions_or_notes",
    ], manifest_rows)
    lecture_index = csv_bytes([
        "teacher", "batch", "subject", "chapter", "lecture_index", "lecture_title",
        "url", "provider", "access", "verification", "source_file",
    ], lecture_rows)
    return manifest, lecture_index, len(lecture_rows)

def main() -> None:
    files = collect_files()
    if not files:
        raise SystemExit("No teacher catalogue files found.")
    manifest, lecture_index, lecture_rows = build_indexes(files)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    research_root = ROOT / "research" / "edison" / "2026"
    teacher_folders = sum(1 for p in research_root.iterdir() if p.is_dir()) if research_root.exists() else 0
    legacy_root = ROOT / "app" / "src" / "main" / "assets" / "catalog"
    new_root = ROOT / "app" / "src" / "main" / "assets" / "catalog_2027"
    legacy_count = len(list(legacy_root.glob("*.json"))) if legacy_root.exists() else 0
    cohort_2027_count = len(list(new_root.glob("*.json"))) if new_root.exists() else 0

    readme = f"""# أرشيف فهارس محاضرات سادسيون

## محتويات الأرشيف
- ملفات البحث الأصلية لدفعة 2026 داخل research/edison/2026/، بما فيها JSON وCSV وTXT وملاحظات التوثيق.
- ملفات فهرس التطبيق القديمة داخل app/src/main/assets/catalog/ ({legacy_count} ملف JSON).
- ملفات فهرس دفعة 2027 داخل app/src/main/assets/catalog_2027/ ({cohort_2027_count} ملفات JSON).
- فهارس الوزاريات/المصادر داخل app/src/main/assets/exams/.
- ملف Catalog.java لمرجع أسماء المدرسين وصورهم.
- CATALOG_MANIFEST.csv: جرد لكل ملف JSON مع المدرس والدفعة والمادة وعدد الفصول والمحاضرات المفهرسة والملاحظات المتاحة.
- LECTURES_INDEX.csv: فهرس موحّد لعناوين المحاضرات والروابط المسجلة ومعلومات التحقق المتوفرة.

## حدود مهمة
هذا أرشيف فهارس وبيانات وروابط، وليس أرشيف فيديوهات أو ملازم PDF. لم تُنزّل ملفات فيديو أو ملفات محمية، ولم يتم تجاوز اشتراك أو قيود وصول. روابط المصادر قد تتوقف أو تتطلب اشتراكاً، ووجود الرابط لا يعني أن تشغيله اختُبر أو أن إعادة نشر المحتوى مسموحة.
تُحفظ الملفات المتكررة في مساراتها الأصلية لأن فهرس التطبيق وملفات البحث قد تمثل نسخاً مختلفة من البيانات، كما تُترك أرقام المحاضرات وملاحظات التحقق كما وردت في المصدر دون تصحيح صامت.

## إحصاء وقت التصدير
- مجلدات المدرسين في بحث 2026: {teacher_folders}
- ملفات JSON لفهرس 2026 داخل التطبيق: {legacy_count}
- ملفات JSON لفهرس 2027 داخل التطبيق: {cohort_2027_count}
- ملفات البيانات الأصلية المضمّنة: {len(files)}
- صفوف المحاضرات المجمّعة في LECTURES_INDEX.csv: {lecture_rows}
"""
    with zipfile.ZipFile(OUT, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for path in files:
            archive.write(path, arcname=path.relative_to(ROOT).as_posix())
        archive.writestr("EXPORT_README_AR.md", readme.encode("utf-8"))
        archive.writestr("CATALOG_MANIFEST.csv", manifest)
        archive.writestr("LECTURES_INDEX.csv", lecture_index)
    with zipfile.ZipFile(OUT, "r") as archive:
        bad = archive.testzip()
        if bad:
            raise SystemExit(f"ZIP validation failed at {bad}")
    print(f"Created {OUT} ({OUT.stat().st_size} bytes), {len(files)} source files, {lecture_rows} indexed lecture rows.")

if __name__ == "__main__":
    main()
