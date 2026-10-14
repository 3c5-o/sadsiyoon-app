#!/usr/bin/env python3
"""Offline audit; does not prove external playback."""
import json
from pathlib import Path
from urllib.parse import urlparse
from collections import defaultdict

root=Path(__file__).resolve().parents[1]
result=[]
for batch,directory in (("2026","catalog"),("2027","catalog_2027")):
    for path in sorted((root/"app/src/main/assets"/directory).glob("*.json")):
        doc=json.loads(path.read_text(encoding="utf-8"))
        lessons=[(ch.get("title",""),l) for ch in doc["chapters"] for l in ch.get("lessons",ch.get("lectures",[]))]
        missing=[];links=defaultdict(list);invalid=[]
        for i,(chapter,lesson) in enumerate(lessons,1):
            url=lesson.get("url") or ""
            if not url:
                missing.append({"index":i,"chapter":chapter,"title":lesson.get("title","")})
                continue
            parsed=urlparse(url)
            if parsed.scheme!="https" or not parsed.netloc:invalid.append(url)
            links[url].append({"chapter":chapter,"title":lesson.get("title","")})
        repeated=[{"url":url,"entries":values} for url,values in links.items() if len(values)>1]
        result.append({"batch":batch,"path":path.name,"teacher":doc.get("teacher",""),
            "chapters":len(doc["chapters"]),"total":len(lessons),"with_link":len(lessons)-len(missing),
            "missing":len(missing),"missing_examples":missing,"duplicate_groups":len(repeated),
            "repeated_links":repeated,"invalid":invalid})
report=root/"reports";report.mkdir(exist_ok=True)
summary={b:{"total":sum(r["total"] for r in result if r["batch"]==b),
            "with_link":sum(r["with_link"] for r in result if r["batch"]==b),
            "missing":sum(r["missing"] for r in result if r["batch"]==b),
            "duplicate_groups":sum(r["duplicate_groups"] for r in result if r["batch"]==b)}
        for b in ("2026","2027")}
(report/"catalog_health.json").write_text(json.dumps({"notice":"No online playback test performed. Some duplicates are intentional cross-references.", "summary":summary,"files":result},ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
lines=["# Sadsiyoon source index health","", "Playability is not checked by this offline audit.","",
"| Batch | Lessons | URL present | Missing | Duplicate groups |","|---|---:|---:|---:|---:|"]
for batch,r in summary.items():lines.append(f'| {batch} | {r["total"]} | {r["with_link"]} | {r["missing"]} | {r["duplicate_groups"]} |')
lines.extend(["","## Files needing review","", "| File | Teacher | Missing | Duplicate groups |", "|---|---|---:|---:|"])
for r in result:
    if r["missing"] or r["duplicate_groups"]:
        lines.append(f'| {r["path"]} | {r["teacher"]} | {r["missing"]} | {r["duplicate_groups"]} |')
(report/"catalog_health.md").write_text("\n".join(lines)+"\n",encoding="utf-8")
assert len([r for r in result if r["batch"]=="2026"])==22
assert len([r for r in result if r["batch"]=="2027"])==4
assert all(not r["invalid"] for r in result), "Malformed video URLs"
assert summary["2026"]["missing"]<=101, "Missing URLs regressed"
assert summary["2027"]["missing"]==0, "2027 catalog regressed"
print(json.dumps(summary,ensure_ascii=False))
