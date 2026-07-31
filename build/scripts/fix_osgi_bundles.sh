#!/bin/sh

#
# Copyright contributors to the SyncWeave project
#
# This script fixes OSGi manifests for third-party JARs that don't have
# proper OSGi bundle metadata. This is required for Java 17 migration.
#
# Fixes applied:
# 1. javax.inject - Add OSGi bundle metadata
# 2. jersey-server - Make jersey-client dependency optional
# 3. Jackson JARs - Add/fix OSGi manifests
# 4. ASM JARs - Add/fix OSGi manifests
# 5. SLF4J JARs - Add/fix OSGi manifests
# 6. JAXB API - Add/fix OSGi manifest
#

set -e

#
# Check the command line usage.
#

if [ $# -ne 1 ] ; then
    echo "usage: $0 [lib directory]"
    exit 1
fi

libdir=$1

if [ ! -d "$libdir" ] ; then
    echo "Error> $libdir does not exist!"
    exit 1
fi

tmpdir=/tmp/fix_osgi_bundles_$$
mkdir -p "$tmpdir"

#
# Function to fix javax.inject JAR
#
fix_javax_inject() {
    jar="$libdir/javax.inject.jar"
    
    if [ ! -f "$jar" ] ; then
        echo "Warning> javax.inject.jar not found, skipping"
        return
    fi
    
    echo "Fixing OSGi manifest for: javax.inject.jar"
    
    # Extract the JAR
    workdir="$tmpdir/javax.inject"
    mkdir -p "$workdir"
    cd "$workdir"
    jar xf "$jar"
    mkdir -p META-INF
    
    # Create proper OSGi manifest
    cat > META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: javax.inject
Bundle-SymbolicName: javax.inject
Bundle-Version: 1.0.0
Bundle-Vendor: JSR-330
Export-Package: javax.inject;version="1.0.0"
Bundle-RequiredExecutionEnvironment: JavaSE-1.8

EOF
    
    # Repackage with new manifest
    jar cfm "$jar.new" META-INF/MANIFEST.MF .
    mv "$jar.new" "$jar"
    
    # Rename to OSGi convention
    mv "$jar" "$libdir/javax.inject_1.0.0.jar"
    
    cd - > /dev/null
    echo "  -> Created javax.inject_1.0.0.jar with proper OSGi manifest"
}

#
# Function to fix SLF4j API
#
fix_slf4j_api() {
    jar="$libdir/slf4j.api_2.0.3.jar"
    
    if [ ! -f "$jar" ] ; then
        echo "Warning> slf4j.api_2.0.3 JAR not found, skipping"
        return
    fi
    
    echo "Fixing OSGi manifest for: slf4j.api_2.0.3"
    
    workdir="$tmpdir/slf4j-api-2.0.3"
    mkdir -p "$workdir"
    cd "$workdir"
    jar xf "$jar"
    
    cat > META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Bundle-SymbolicName: slf4j.api
Bundle-Name: SLF4J API
Bundle-Version: 2.0.3
Bundle-ManifestVersion: 2
Bundle-Vendor: QOS.ch
Export-Package: org.slf4j;version="2.0.3",
 org.slf4j.spi;version="2.0.3",
 org.slf4j.helpers;version="2.0.3",
 org.slf4j.event;version="2.0.3"
Bundle-RequiredExecutionEnvironment: JavaSE-1.8

EOF
    
    jar cfm "$jar.new" META-INF/MANIFEST.MF .
    mv "$jar.new" "$jar"
    cd - > /dev/null
    echo "  -> Fixed slf4j.api_2.0.3 manifest"
}

#
# Function to fix SLF4j JDK
#
fix_slf4j_jdk() {
    jar="$libdir/slf4j.jdk14_1.7.36.jar"
    
    if [ ! -f "$jar" ] ; then
        echo "Warning> slf4j.jdk14_1.7.36 JAR not found, skipping"
        return
    fi
    
    echo "Fixing OSGi manifest for: slf4j.jdk14_1.7.36"
    
    workdir="$tmpdir/slf4j-jdk14-1.7.36"
    mkdir -p "$workdir"
    cd "$workdir"
    jar xf "$jar"
    
    cat > META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: SLF4J JDK14 Binding
Bundle-SymbolicName: slf4j.jdk14
Bundle-Version: 1.7.36
Bundle-Vendor: QOS.ch
Import-Package: org.slf4j;version="[1.7.0,3.0.0)",
 org.slf4j.spi;version="[1.7.0,3.0.0)",
 org.slf4j.helpers;version="[1.7.0,3.0.0)"
Export-Package: org.slf4j.impl;version="1.7.36"
Bundle-RequiredExecutionEnvironment: JavaSE-1.8
Fragment-Host: slf4j.api

EOF
    
    jar cfm "$jar.new" META-INF/MANIFEST.MF .
    mv "$jar.new" "$jar"
    cd - > /dev/null
    echo "  -> Fixed slf4j.jdk14_1.7.36 manifest"
}

#
# Function to fix javax.xml.bind
#
fix_javax_xml_bind() {
    jar="$libdir/javax.xml.bind_2.3.1.jar"
    
    if [ ! -f "$jar" ] ; then
        echo "Warning> javax.xml.bind_2.3.1 JAR not found, skipping"
        return
    fi
    
    echo "Fixing OSGi manifest for: javax.xml.bind_2.3.1"
    
    workdir="$tmpdir/javax-xml-bind-2.3.1"
    mkdir -p "$workdir"
    cd "$workdir"
    jar xf "$jar"
    
    cat > META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: JAXB API
Bundle-SymbolicName: javax.xml.bind
Bundle-Version: 2.3.1
Bundle-Vendor: Oracle
Export-Package: javax.xml.bind;version="2.3.1",
 javax.xml.bind.annotation;version="2.3.1",
 javax.xml.bind.annotation.adapters;version="2.3.1",
 javax.xml.bind.attachment;version="2.3.1",
 javax.xml.bind.helpers;version="2.3.1",
 javax.xml.bind.util;version="2.3.1"
Import-Package: javax.activation;resolution:=optional,
 javax.xml.namespace,
 javax.xml.stream,
 javax.xml.transform,
 javax.xml.transform.stream,
 org.w3c.dom
Bundle-RequiredExecutionEnvironment: JavaSE-1.8

EOF
    
    jar cfm "$jar.new" META-INF/MANIFEST.MF .
    mv "$jar.new" "$jar"
    cd - > /dev/null
    echo "  -> Fixed javax.xml.bind_2.3.1 manifest"
}

#
# Function to fix org.ow2.asm
#
fix_org_ow2_asm() {
    jar="$libdir/org.ow2.asm_9.6.0.jar"
    
    if [ ! -f "$jar" ] ; then
        echo "Warning> org.ow2.asm_9.6.0 JAR not found, skipping"
        return
    fi
    
    echo "Fixing OSGi manifest for: org.ow2.asm_9.6.0"
    
    workdir="$tmpdir/org-ow2-asm-9.6.0"
    mkdir -p "$workdir"
    cd "$workdir"
    jar xf "$jar"
    
    cat > META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: ASM Core
Bundle-SymbolicName: org.ow2.asm
Bundle-Version: 9.6.0
Bundle-Vendor: OW2
Export-Package: org.objectweb.asm;version="9.6.0",
 org.objectweb.asm.signature;version="9.6.0"
Bundle-RequiredExecutionEnvironment: JavaSE-1.8

EOF
    
    jar cfm "$jar.new" META-INF/MANIFEST.MF .
    mv "$jar.new" "$jar"
    cd - > /dev/null
    echo "  -> Fixed org.ow2.asm_9.6.0 manifest"
}

#
# Function to fix org.ow2.asm.commons
#
fix_org_ow2_asm_commons() {
    jar="$libdir/org.ow2.asm.commons_9.6.0.jar"
    
    if [ ! -f "$jar" ] ; then
        echo "Warning> org.ow2.asm.commons_9.6.0 JAR not found, skipping"
        return
    fi
    
    echo "Fixing OSGi manifest for: org.ow2.asm.commons_9.6.0"
    
    workdir="$tmpdir/org_ow2_asm_commons_9.6.0"
    mkdir -p "$workdir"
    cd "$workdir"
    jar xf "$jar"
    
    cat > META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Bundle-SymbolicName: org.ow2.asm.commons
Export-Package: org.objectweb.asm.commons;version="9.6.0"
Bundle-Name: ASM Commons
Bundle-Version: 9.6.0
Bundle-ManifestVersion: 2
Bundle-RequiredExecutionEnvironment: JavaSE-1.8
Bundle-Vendor: OW2
Import-Package: org.objectweb.asm;version="[9.6,10)",
 org.objectweb.asm.signature;version="[9.6,10)",
 org.objectweb.asm.tree;version="[9.6,10)"

EOF
    
    jar cfm "$jar.new" META-INF/MANIFEST.MF .
    mv "$jar.new" "$jar"
    cd - > /dev/null
    echo "  -> Fixed org.ow2.asm.commons_9.6.0 manifest"
}

#
# Function to fix org.ow2.asm.tree
#
fix_org_ow2_asm_tree() {
    jar="$libdir/org.ow2.asm.tree_9.6.0.jar"
    
    if [ ! -f "$jar" ] ; then
        echo "Warning> org.ow2.asm.tree_9.6.0 JAR not found, skipping"
        return
    fi
    
    echo "Fixing OSGi manifest for: org.ow2.asm.tree_9.6.0"
    
    workdir="$tmpdir/org-ow2-asm-tree-9.6.0"
    mkdir -p "$workdir"
    cd "$workdir"
    jar xf "$jar"
    
    cat > META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Bundle-SymbolicName: org.ow2.asm.tree
Export-Package: org.objectweb.asm.tree;version="9.6.0",
 org.objectweb.asm.tree.analysis;version="9.6.0"
Bundle-Name: ASM Tree
Bundle-Version: 9.6.0
Bundle-ManifestVersion: 2
Bundle-RequiredExecutionEnvironment: JavaSE-1.8
Bundle-Vendor: OW2
Import-Package: org.objectweb.asm;version="[9.6,10)"

EOF
    
    jar cfm "$jar.new" META-INF/MANIFEST.MF .
    mv "$jar.new" "$jar"
    cd - > /dev/null
    echo "  -> Fixed org.ow2.asm.tree_9.6.0 manifest"
}

#
# Function to fix org.ow2.asm.util
#
fix_org_ow2_asm_util() {
    jar="$libdir/org.ow2.asm.util_9.6.0.jar"
    
    if [ ! -f "$jar" ] ; then
        echo "Warning> org.ow2.asm.util_9.6.0 JAR not found, skipping"
        return
    fi
    
    echo "Fixing OSGi manifest for: org.ow2.asm.util_9.6.0"
    
    workdir="$tmpdir/org_ow2_asm_util"
    mkdir -p "$workdir"
    cd "$workdir"
    jar xf "$jar"
    
    cat > META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Bundle-SymbolicName: org.ow2.asm.util
Export-Package: org.objectweb.asm.util;version="9.6.0"
Bundle-Name: ASM Util
Bundle-Version: 9.6.0
Bundle-ManifestVersion: 2
Bundle-RequiredExecutionEnvironment: JavaSE-1.8
Bundle-Vendor: OW2
Import-Package: org.objectweb.asm;version="[9.6,10)",
 org.objectweb.asm.signature;version="[9.6,10)",
 org.objectweb.asm.tree;version="[9.6,10)",
 org.objectweb.asm.tree.analysis;version="[9.6,10)"

EOF
    
    jar cfm "$jar.new" META-INF/MANIFEST.MF .
    mv "$jar.new" "$jar"
    cd - > /dev/null
    echo "  -> Fixed org.ow2.asm.util_9.6.0 manifest"
}

#
# Function to fix org.ow2.asm.analysis
#
fix_org_ow2_asm_analysis() {
    jar="$libdir/org.ow2.asm.analysis_9.6.0.jar"
    
    if [ ! -f "$jar" ] ; then
        echo "Warning> org.ow2.asm.analysis_9.6.0 JAR not found, skipping"
        return
    fi
    
    echo "Fixing OSGi manifest for: org.ow2.asm.analysis_9.6.0 "
    
    workdir="$tmpdir/org-ow2-asm-analysis"
    mkdir -p "$workdir"
    cd "$workdir"
    jar xf "$jar"
    
    cat > META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Bundle-SymbolicName: org.ow2.asm.analysis
Export-Package: org.objectweb.asm.tree.analysis;version="9.6.0"
Bundle-Name: ASM Analysis
Bundle-Version: 9.6.0
Bundle-ManifestVersion: 2
Bundle-RequiredExecutionEnvironment: JavaSE-1.8
Bundle-Vendor: OW2
Import-Package: org.objectweb.asm;version="[9.6,10)",
 org.objectweb.asm.tree;version="[9.6,10)",
 org.objectweb.asm.tree.analysis;version="[9.6,10)"

EOF
    
    jar cfm "$jar.new" META-INF/MANIFEST.MF .
    mv "$jar.new" "$jar"
    cd - > /dev/null
    echo "  -> Fixed org.ow2.asm.analysis_9.6.0 manifest"
}

#
# Main execution
#

echo "Fixing OSGi bundles in: $libdir"
echo ""

fix_javax_inject
fix_slf4j_api
fix_slf4j_jdk
fix_javax_xml_bind
fix_org_ow2_asm
fix_org_ow2_asm_commons
fix_org_ow2_asm_tree
fix_org_ow2_asm_util
fix_org_ow2_asm_analysis

# Cleanup
rm -rf "$tmpdir"

echo ""
echo "OSGi bundle fixes completed successfully"