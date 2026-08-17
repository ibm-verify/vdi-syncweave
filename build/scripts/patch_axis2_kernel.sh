#!/bin/sh
# patch_axis2_kernel.sh <axis2-kernel.jar>
#
# Removes the LocalTransportSender <transportSender> entry from both
# axis2.xml and org/apache/axis2/deployment/axis2_default.xml that are
# embedded inside axis2-kernel.jar.
#
# LocalTransportSender was removed from Axis2 1.8.x transport JARs but
# the embedded XML configuration files still reference it, which causes
# a ClassNotFoundException / DeploymentException at startup.
#
# Usage (called from build.xml rename_jars target):
#   sh patch_axis2_kernel.sh lib/ivy/axis-2/axis2-kernel.jar

set -e

JAR="$1"
if [ -z "$JAR" ] || [ ! -f "$JAR" ]; then
    echo "Usage: $0 <path-to-axis2-kernel.jar>" >&2
    exit 1
fi

# Resolve to absolute path so cd into WORK_DIR doesn't break the reference
case "$JAR" in
    /*) ABS_JAR="$JAR" ;;
    *)  ABS_JAR="$(pwd)/$JAR" ;;
esac

WORK_DIR=$(mktemp -d)
trap 'rm -rf "$WORK_DIR"' EXIT

# Unpack the two XML files we need to patch
cd "$WORK_DIR"
jar xf "$ABS_JAR" \
    axis2.xml \
    org/apache/axis2/deployment/axis2_default.xml

PATCHED=0

for XML_FILE in axis2.xml org/apache/axis2/deployment/axis2_default.xml; do
    if [ ! -f "$XML_FILE" ]; then
        continue
    fi

    # Remove the <transportSender name="local" .../> block (single-line form)
    if grep -q 'LocalTransportSender' "$XML_FILE"; then
        # Handle single-line self-closing form:
        # <transportSender name="local" class="org.apache.axis2.transport.local.LocalTransportSender"/>
        # Also handle multi-line form with child elements (ended by </transportSender>).
        # We use Python for reliable multi-line XML surgery.
        python3 - "$XML_FILE" <<'PYEOF'
import sys, re

path = sys.argv[1]
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()

# Remove self-closing transportSender for local transport (single line)
text = re.sub(
    r'\s*<transportSender[^>]*class="org\.apache\.axis2\.transport\.local\.LocalTransportSender"[^/]*/>\s*\n?',
    '\n',
    text
)

# Remove multi-line transportSender block for local transport
text = re.sub(
    r'\s*<transportSender[^>]*class="org\.apache\.axis2\.transport\.local\.LocalTransportSender"[^>]*>.*?</transportSender>\s*\n?',
    '\n',
    text,
    flags=re.DOTALL
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(text)

print("Patched: " + path)
PYEOF
        PATCHED=1
    fi
done

if [ "$PATCHED" -eq 1 ]; then
    # Update the JAR in place with patched files
    jar uf "$ABS_JAR" \
        axis2.xml \
        org/apache/axis2/deployment/axis2_default.xml
    echo "axis2-kernel.jar patched successfully: LocalTransportSender removed."
else
    echo "axis2-kernel.jar: LocalTransportSender not found — no patch needed."
fi
