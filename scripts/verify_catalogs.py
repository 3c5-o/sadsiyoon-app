#!/usr/bin/env python3
"""Offline regression checks for bundled cohort catalogs; no video fetching."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
older = list((root / "app/src/main/assets/catalog").glob("*.json"))
assert len(older) == 22, f"Expected 22 unchanged indexed 2026 files; found {len(older)}"
for f in older:
    d = json.loads(f.read_text(encoding="utf-8"))
    assert "chapters" in d, f"Missing chapter array: {f}"
p = root / "app/src/main/assets/catalog_2027/01-haider-abdul-imma.json"
d = json.loads(p.read_text(encoding="utf-8"))
assert d["batch"] == "2027"
assert d["teacher"] == "حيدر عبدالائمة"
assert len(d["chapters"]) == 3, "Expected chapters one, two, three"
chap = d["chapters"][0]
assert len(chap["lessons"]) == 28
fallback_numbers = {6, 19, 20, 21}
seen = set()
for n, entry in enumerate(chap["lessons"], 1):
    assert entry["index"] == n
    assert entry["title"] == f"المحاضرة {n}"
    url = entry["url"]
    assert url.startswith("https://")
    assert url not in seen, f"Duplicate iframe URL: lesson {n}"
    seen.add(url)
    if n in fallback_numbers:
        assert entry["provider"] == "bitchute"
        assert url.startswith("https://www.bitchute.com/embed/")
        assert entry["access"] == "unverified"
    else:
        assert entry["provider"] == "rumble"
        assert url.startswith("https://rumble.com/embed/")
        assert entry["access"] == "premium"
    assert entry["verified"] == "embed_present_not_playback"
print(f"OK: {len(older)} legacy 2026 JSON files retained, "
      f"{len(chap['lessons'])} indexed 2027 lessons "
      f"({28-len(fallback_numbers)} premium Rumble, {len(fallback_numbers)} Bitchute unverified)")

# Validate chapter two while retaining chapter one's exact ordering.
chapter2=d["chapters"][1]
assert chapter2["title"] == "الفصل الثاني"
assert len(chapter2["lessons"]) == 26
assert len({v["url"] for v in chapter2["lessons"]}) == 26
for i,lesson in enumerate(chapter2["lessons"],1):
    assert lesson["index"] == i
    assert lesson["title"] == f"المحاضرة {i}"
    assert lesson["url"].startswith("https://rumble.com/embed/")
    assert lesson["provider"] == "rumble"
    assert lesson["access"] == "premium"
    assert lesson["verified"] == "embed_present_not_playback"
print("OK: second chapter — 26 indexed Rumble embed URLs; all premium-restricted")

# Third-chapter Rumble embeds on public site server 4; playback remains subject to Premium.
chapter3=d["chapters"][2]
assert chapter3["title"] == "الفصل الثالث"
assert len(chapter3["lessons"]) == 7
assert len({v["url"] for v in chapter3["lessons"]}) == 7
for n,entry in enumerate(chapter3["lessons"],1):
    assert entry["index"] == n
    assert entry["title"] == f"المحاضرة {n}"
    assert entry["provider"] == "rumble"
    assert entry["server"] == "4"
    assert entry["url"].startswith("https://rumble.com/embed/")
    assert entry["access"] == "premium"
    assert entry["verified"] == "embed_present_not_playback"
assert sum(len(ch["lessons"]) for ch in d["chapters"]) == 61
print("OK: chapter three 7 Rumble embeds indexed; 61 total 2027 lessons")


# 2027: preserve original chapter ordering and verify the additional three teachers.
import re
for asset,expected in {
    "02-muhammad-qasim.json": [24],
    "03-hisham-al-mamouri.json": [10,24,10,9],
    "04-sajjad-al-obaidi.json": [4,3,22,12,2,2],
}.items():
    path=root/"app/src/main/assets/catalog_2027"/asset
    doc=json.loads(path.read_text(encoding="utf-8"))
    assert doc["batch"]=="2027" and doc["teacher"].strip()
    assert [len(ch["lessons"]) for ch in doc["chapters"]]==expected,asset
    for chapter in doc["chapters"]:
        urls=[]
        for n,lesson in enumerate(chapter["lessons"],1):
            assert lesson["index"]==n,(asset,chapter["title"],n)
            assert lesson["title"]==f"المحاضرة {n}"
            assert lesson["url"].startswith("https://rumble.com/embed/")
            assert lesson["provider"]=="rumble"
            assert lesson["access"]=="premium"
            assert lesson["verified"]=="embed_present_not_playback"
            urls.append(lesson["url"])
        assert len(set(urls))==len(urls),asset
print("OK: 2027 index contains four teachers, 183 lectures across all 2027 chapters")

# Validate externally hosted portraits against the names already in the Java catalog.
# The user supplied two spelling variants; the canonical catalog labels are kept.
from urllib.parse import urlparse
source=(root/"app/src/main/java/com/sadsiyoon/app/Catalog.java").read_text(encoding="utf-8")
image_chunk=source.split("private static final String[] IMAGES={",1)[1].split("};",1)[0]
name_chunk=source.split("private static final String[] NAMES={",1)[1].split("};",1)[0]
labels=re.findall(r'"([^"]*)"',name_chunk)
images=re.findall(r'"([^"]*)"',image_chunk)
assert len(labels)==len(images)==19
teacher_images=dict(zip(labels,images))
expected_images={
    "علي السوداني": "https://j.top4top.io/p_3938l5zla1.jpg",
    "ساجد العكيلي": "https://l.top4top.io/p_3938ii0nw1.jpg",
    "مهند السوداني": "https://d.top4top.io/p_3938josxy1.jpg",
    "رفل الزبيدي": "https://j.top4top.io/p_3938dihl31.jpg",
    "نورس الموسوي": "https://a.top4top.io/p_3938yv4y11.jpg",
    "هاشم الغرباوي": "https://h.top4top.io/p_3938lmygb1.jpg",
    "محمد قاسم": "https://c.top4top.io/p_3938su4u21.jpg",
    "أزل سلوان": "https://d.top4top.io/p_3938pjd2e1.jpg",
    "جعفر الحسني": "https://h.top4top.io/p_3938f3g7s1.jpg",
    "هشام المعموري": "https://i.top4top.io/p_3938bfw3s1.jpg",
    "حسين محمد": "https://f.top4top.io/p_3938pnnw51.jpg",
    "عباس الدراجي": "https://l.top4top.io/p_3938p9alv1.jpg",
    "سجاد العبيدي": "https://d.top4top.io/p_3938suhes1.jpg",
    "حيدر عبدالائمة": "https://j.top4top.io/p_3938gg6om1.jpg",
}
assert len(expected_images)==14
for teacher,url in expected_images.items():
    assert teacher_images.get(teacher)==url, f"Portrait mismatch for {teacher}"
    parsed=urlparse(url)
    assert parsed.scheme=="https" and parsed.hostname.endswith(".top4top.io") and parsed.path.endswith(".jpg")
assert len(set(expected_images.values()))==14
print("OK: 14 external teacher portraits mapped to the existing 2026/2027 catalogs")

# Don't mislabel another grade's or year's exam sheets as 2025.
exams=json.loads((root/"app/src/main/assets/exams/2025.json").read_text(encoding="utf-8"))
assert exams["year"] == 2025
assert exams["status"] == "source_links_verified"
assert len(exams["papers"]) == 4
expected = {("scientific","الرياضيات"),("literary","الرياضيات"),("scientific","اللغة العربية"),("literary","اللغة العربية")}
assert {(p["branch"],p["subject"]) for p in exams["papers"]} == expected
assert all(p["year"] == 2025 and p["round"] == "الدور الأول" and p["source"].startswith("https://mlazemna.com/") and p["images"] == [] for p in exams["papers"])
assert len({p["source"] for p in exams["papers"]}) == 4
source=(root/"app/src/main/java/com/sadsiyoon/app/MainActivity.java").read_text(encoding="utf-8")
assert 'private void exams(){' in source
assert 'private void displayExamPaper(' in source
assert 'لا توجد ورقة 2025 مؤكدة' in source
print("OK: 4 authentic 2025 scientific/literary math/Arabic exam source pages, image rights preserved")
