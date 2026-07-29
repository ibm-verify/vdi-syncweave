#!/usr/bin/env python3
#
# Copyright contributors to the SyncWeave project
#
# SPDX-License-Identifier: Apache-2.0
#
# Converts an SPDX 2.3 JSON SBOM to CycloneDX 1.6 JSON format.
#
# Usage:
#   python3 tools/spdx_to_cyclonedx.py <input_spdx.json> <output_cdx.json>

import json
import sys
import uuid
import re
from datetime import datetime, timezone


# ---------------------------------------------------------------------------
# SPDX licence expression → CycloneDX licence mapping helpers
# ---------------------------------------------------------------------------

def parse_licenses(spdx_expr):
    """
    Parse a SPDX licence expression into a CycloneDX licenses list.
    Handles AND / OR compound expressions and LicenseRef-* custom refs.
    """
    if not spdx_expr or spdx_expr in ("NOASSERTION", "NONE", ""):
        return []

    # Split on AND / OR (keep it simple — CycloneDX expression support)
    ids = re.split(r'\s+(?:AND|OR)\s+', spdx_expr)
    licenses = []
    for lid in ids:
        lid = lid.strip()
        if not lid:
            continue
        if lid.startswith("LicenseRef-"):
            licenses.append({"license": {"name": lid}})
        else:
            licenses.append({"license": {"id": lid}})
    return licenses


# ---------------------------------------------------------------------------
# PURL extraction helper
# ---------------------------------------------------------------------------

def extract_purl(pkg):
    """Extract the first purl from SPDX externalRefs."""
    for ref in pkg.get("externalRefs", []):
        if ref.get("referenceType") == "purl":
            return ref.get("referenceLocator", "")
    return ""


# ---------------------------------------------------------------------------
# Main conversion
# ---------------------------------------------------------------------------

def spdx_to_cyclonedx(spdx):
    creation_info = spdx.get("creationInfo", {})
    created = creation_info.get("created", datetime.now(timezone.utc).isoformat())

    # Build tool list
    tools = []
    for c in creation_info.get("creators", []):
        if c.startswith("Tool:"):
            tool_str = c[len("Tool:"):].strip()
            # Split name and version on last '-' preceded by 'v' or just use full string
            match = re.match(r'^(.+?)[-/]?(v\d[\d.\-+a-z]*)$', tool_str, re.IGNORECASE)
            if match:
                tools.append({"name": match.group(1).strip(), "version": match.group(2).strip()})
            else:
                tools.append({"name": tool_str})

    # Metadata component = the root document
    metadata = {
        "timestamp": created,
        "tools": {"components": [{"type": "application", **t} for t in tools]},
        "component": {
            "type": "library",
            "bom-ref": "root-component",
            "name": spdx.get("name", "unknown"),
            "version": "NOASSERTION",
        },
        "licenses": [{"license": {"id": "CC0-1.0"}}],
    }

    # Build a map of SPDXID -> package for relationship resolution
    spdx_id_map = {}
    bom_ref_map = {}  # SPDXID -> bom-ref

    # Convert packages → components
    components = []
    for pkg in spdx.get("packages", []):
        spdx_id = pkg.get("SPDXID", "")
        name = pkg.get("name", "")
        version = pkg.get("versionInfo", "")
        purl = extract_purl(pkg)
        license_concluded = pkg.get("licenseConcluded", "")
        copyright_text = pkg.get("copyrightText", "")
        download_url = pkg.get("downloadLocation", "")
        description = pkg.get("comment", "")

        # Generate a stable bom-ref from SPDXID
        bom_ref = spdx_id if spdx_id else str(uuid.uuid4())
        bom_ref_map[spdx_id] = bom_ref
        spdx_id_map[spdx_id] = pkg

        component = {
            "type": "library",
            "bom-ref": bom_ref,
            "name": name,
        }
        if version and version not in ("NOASSERTION", "NONE"):
            component["version"] = version
        if purl:
            component["purl"] = purl
        if description:
            component["description"] = description

        # Licenses
        licenses = parse_licenses(license_concluded)
        if licenses:
            component["licenses"] = licenses

        # Copyright
        if copyright_text and copyright_text not in ("NOASSERTION", "NONE"):
            component["copyright"] = copyright_text

        # External references
        ext_refs = []
        if download_url and download_url not in ("NOASSERTION", "NONE"):
            ext_refs.append({"type": "distribution", "url": download_url})
        if ext_refs:
            component["externalReferences"] = ext_refs

        components.append(component)

    # Convert relationships → dependencies
    # CycloneDX dependencies: ref → dependsOn list
    dep_map = {}  # bom-ref → set of bom-refs it depends on

    for rel in spdx.get("relationships", []):
        rel_type = rel.get("relationshipType", "")
        elem_id = rel.get("spdxElementId", "")
        related_id = rel.get("relatedSpdxElement", "")

        if rel_type in ("DEPENDS_ON", "DYNAMIC_LINK", "STATIC_LINK", "RUNTIME_DEPENDENCY_OF"):
            src = bom_ref_map.get(elem_id)
            tgt = bom_ref_map.get(related_id)
            if src and tgt:
                dep_map.setdefault(src, set()).add(tgt)
        elif rel_type == "DEPENDENCY_OF":
            # Reverse: related depends on elem
            src = bom_ref_map.get(related_id)
            tgt = bom_ref_map.get(elem_id)
            if src and tgt:
                dep_map.setdefault(src, set()).add(tgt)

    dependencies = []
    for ref, depends_on in dep_map.items():
        dependencies.append({
            "ref": ref,
            "dependsOn": sorted(depends_on),
        })

    cdx = {
        "bomFormat": "CycloneDX",
        "specVersion": "1.6",
        "serialNumber": f"urn:uuid:{uuid.uuid4()}",
        "version": 1,
        "metadata": metadata,
        "components": components,
    }
    if dependencies:
        cdx["dependencies"] = dependencies

    return cdx


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print(f"Usage: python3 {sys.argv[0]} <input_spdx.json> <output_cdx.json>")
        sys.exit(1)

    input_path = sys.argv[1]
    output_path = sys.argv[2]

    print(f"Reading SPDX SBOM from: {input_path}")
    with open(input_path, "r", encoding="utf-8") as f:
        spdx_data = json.load(f)

    print(f"Converting {len(spdx_data.get('packages', []))} packages and "
          f"{len(spdx_data.get('relationships', []))} relationships ...")

    cdx_data = spdx_to_cyclonedx(spdx_data)

    print(f"Writing CycloneDX 1.6 SBOM to: {output_path}")
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(cdx_data, f, indent=2, ensure_ascii=False)

    comp_count = len(cdx_data["components"])
    dep_count = len(cdx_data.get("dependencies", []))
    print(f"Done. Components: {comp_count}, Dependency entries: {dep_count}")
