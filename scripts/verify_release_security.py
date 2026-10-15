#!/usr/bin/env python3
"""Validate intentional Android permissions and core privacy protections.

This is a static configuration check, NOT a malware scan or a Google Play
Protect verdict. A signed APK still needs device testing and distribution review.
"""
from pathlib import Path
from xml.etree import ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ANDROID = "{http://schemas.android.com/apk/res/android}"
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"
RULES = ROOT / "app/src/main/res/xml/data_extraction_rules.xml"

def require(condition, message):
    if not condition:
        raise SystemExit("Release security check failed: " + message)

root = ET.parse(MANIFEST).getroot()
permissions = {
    item.get(ANDROID + "name")
    for item in root.findall("uses-permission")
}
expected = {
    "android.permission.INTERNET",
    "android.permission.ACCESS_NETWORK_STATE",
}
require(permissions == expected, f"Review changed permissions: {sorted(permissions)}")
application = root.find("application")
require(application is not None, "Missing application")
require(application.get(ANDROID + "allowBackup") == "false",
        "Automatic backup must be disabled for private local student notes")
require(application.get(ANDROID + "usesCleartextTraffic") == "false",
        "Cleartext traffic must stay disabled")
require(application.get(ANDROID + "dataExtractionRules") == "@xml/data_extraction_rules",
        "Missing data transfer exclusions")
require(application.get(ANDROID + "debuggable") != "true",
        "Debuggable flag must not be enabled in manifest")

activities = application.findall("activity")
require(activities, "Missing activities")
for activity in activities:
    name = activity.get(ANDROID + "name", "")
    exported = activity.get(ANDROID + "exported")
    if name == ".IntroActivity":
        require(exported == "true", "Launcher must remain launchable")
    else:
        require(exported == "false", f"Unexpected exported activity: {name}")

rules = ET.parse(RULES).getroot()
require(rules.tag == "data-extraction-rules", "Invalid data extraction rules")
for section in ("cloud-backup", "device-transfer"):
    node = rules.find(section)
    require(node is not None, f"Missing {section} rules")
    denied_domains = {item.get("domain") for item in node.findall("exclude")
                      if item.get("path") == "."}
    require({"root", "file", "database", "sharedpref"} <= denied_domains,
            f"Missing exclusions in {section}")

print("Static Android security configuration checks passed.")
print("Note: This does not establish a Play Protect safety verdict.")
