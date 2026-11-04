#!/usr/bin/env python3
"""Checks the APK's own manifest (as aapt2 prints it): no backups, no plain-HTTP traffic, not debuggable,
and nothing open to other apps except what is listed below or guarded by a permission."""
import re
import sys

# The only components other apps (or Android itself) may start without a permission
EXPORTED_OK = {"ua.surma.ui.MainActivity", "ua.surma.watch.BootReceiver"}
COMPONENTS = {"activity", "activity-alias", "service", "receiver", "provider"}

text = open(sys.argv[1], encoding="utf-8").read()
errors = []
stack = []  # (indent, element name, attributes)
elements = []

for line in text.splitlines():
    m = re.match(r"^(\s*)E: ([\w:-]+)", line)
    if m:
        indent = len(m.group(1))
        while stack and stack[-1][0] >= indent:
            stack.pop()
        el = [indent, m.group(2), {}]
        stack.append(el)
        elements.append(el)
        continue
    a = re.match(r"^\s*A: (?:http://schemas\.android\.com/apk/res/android:)?([\w]+)(?:\(0x[0-9a-f]+\))?=(.*)$", line)
    if a and stack:
        value = a.group(2).strip()
        q = re.match(r'^"([^"]*)"', value)
        stack[-1][2][a.group(1)] = q.group(1) if q else value


def is_true(v):
    return v is not None and (v == "true" or v.endswith("0xffffffff"))


def is_false(v):
    return v is not None and (v == "false" or re.search(r"\)0x0+$", v) is not None)


apps = [e for e in elements if e[1] == "application"]
if len(apps) != 1:
    errors.append("expected exactly one <application>")
else:
    app = apps[0][2]
    if not is_false(app.get("allowBackup")):
        errors.append(f"allowBackup must be false (is {app.get('allowBackup')})")
    if is_true(app.get("debuggable")):
        errors.append("the APK is debuggable")
    if is_true(app.get("usesCleartextTraffic")):
        errors.append("usesCleartextTraffic is true")
    if "networkSecurityConfig" not in app:
        errors.append("no networkSecurityConfig")
    if "dataExtractionRules" not in app:
        errors.append("no dataExtractionRules")

for _, name, attrs in elements:
    if name in COMPONENTS and is_true(attrs.get("exported")):
        cls = attrs.get("name", "?")
        if cls not in EXPORTED_OK and "permission" not in attrs:
            errors.append(f"exported without a permission: {name} {cls}")

if errors:
    for e in errors:
        print(f"::error::Manifest: {e}")
    sys.exit(1)
print("Manifest OK")
