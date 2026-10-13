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
assert len(d["chapters"]) == 1
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
