# SyncWeave — Host Build with Java 17

**Host:** `sunrhel1.fyre.ibm.com` (RHEL 9, x86_64)
**Java:** IBM Semeru OpenJDK 17.0.19 (`<PROJECT_HOME>/adks/ibm/jdk/jdk-17.0.19+10`)
**Ant:** 1.10.17 (`<PROJECT_HOME>/ant/apache-ant-1.10.17`)
**Eclipse PDE:** 4.27 (`<PROJECT_HOME>/tools/eclipse/eclipse_runtime/4.27`)
**Workspace:** `<PROJECT_HOME>/SyncWeave`

> The host build uses the paths below.

---

## Prerequisites

### 1. Clone the SyncWeave repository

```bash
git clone git@github.com:IBM/SyncWeave.git <PROJECT_HOME>/SyncWeave
cd <PROJECT_HOME>/SyncWeave
```

### 2. Verify Java 17 is available

```bash
<PROJECT_HOME>/SyncWeave/adks/ibm/jdk/jdk-17.0.19+10/bin/java -version
```

Expected output:
```
openjdk version "17.0.19" 2026-04-21
IBM Semeru Runtime Open Edition 17.0.19.0 (build 17.0.19+10)
```

### 3. Place Ivy in Ant's lib directory (one-time setup)

Ivy must be loaded by Ant's system classloader at startup — **not** via a
`<taskdef>` inside the build. Loading it via `<taskdef>` creates a child
classloader, and when the `resolve` target runs a second time in the same
build (e.g. as a dependency of both `resolve` and `rename_jars`), the two
classloader instances produce incompatible class objects, causing:
```
ClassCastException: DefaultModuleDescriptor incompatible with ModuleDescriptor
```

Copy `ivy.jar` into `ANT_HOME/lib` once:

```bash
cp <PROJECT_HOME>/SyncWeave/ivy/ivy.jar \
   <PROJECT_HOME>/ant/apache-ant-1.10.17/lib/ivy.jar
```

Verify:
```bash
ls <PROJECT_HOME>/ant/apache-ant-1.10.17/lib/ivy.jar
```

### 4. Verify the tools tree is present

```bash
ls <PROJECT_HOME>/SyncWeave/tools/eclipse/eclipse_runtime/4.27/eclipse/plugins/org.eclipse.pde.build_*
```


## Environment Variables

Set these in your shell before running any `ant` command.
You can put them in `~/.bashrc` or a local `env.sh` script.

```bash
# Java 17 from adks
export JAVA_HOME=<PROJECT_HOME>/SyncWeave/adks/ibm/jdk/jdk-17.0.19+10
export PATH=$JAVA_HOME/bin:$PATH

# Tools tree (equivalent of /build/tools_git in Docker)
export TOOLS_HOME=<PROJECT_HOME>/SyncWeave/tools

# Ant 1.10.17 standalone installation
export ANT_HOME=<PROJECT_HOME>/ant/apache-ant-1.10.17
export PATH=$ANT_HOME/bin:$PATH
```

Verify:
```bash
java -version          # must show 17.x
ant -version           # must show Apache Ant 1.10.17
echo $TOOLS_HOME       # <PROJECT_HOME>/tools
```

---

## Build Steps

### Step 1 — Set environment

```bash
export JAVA_HOME=<PROJECT_HOME>/SyncWeave/adks/ibm/jdk/jdk-17.0.19+10
export PATH=$JAVA_HOME/bin:$PATH
export TOOLS_HOME=<PROJECT_HOME>/SyncWeave/tools
export ANT_HOME=<PROJECT_HOME>/ant/apache-ant-1.10.17
export PATH=$ANT_HOME/bin:$PATH
```

### Step 2 — Patch org.eclipse.osgi (first time only, idempotent) (Optional)

The patch injects JavaSE-10 through JavaSE-25 profile descriptors into the
Eclipse OSGi JAR so that the PDE resolver accepts modern BREE declarations.
This is called automatically by the `preSetup` target in `ce_rcp/customTargets.xml`,
but you can run it manually to verify before the full build:

```bash
chmod +x build/scripts/patch_osgi_ee_profiles.sh
build/scripts/patch_osgi_ee_profiles.sh \
  $TOOLS_HOME/eclipse/eclipse_runtime/4.27/eclipse/plugins
```

Expected output:
```
Successfully patched org.eclipse.osgi_3.18.300.v20230220-1352.jar with JavaSE-10 through JavaSE-25 profiles.
```
On subsequent runs:
```
org.eclipse.osgi already patched with JavaSE-11+ profiles, skipping.
```

### Step 3 — Resolve Ivy dependencies

```bash
cd <PROJECT_HOME>/SyncWeave
ant resolve rename_jars
```

This downloads all third-party JARs into `lib/ivy/`. On a machine with no
internet access, copy `lib/ivy/` from a previous successful build.

### Step 4 — Full build

```bash
cd <PROJECT_HOME>/SyncWeave
ant images 2>&1 | tee build.log
```

Or for a faster developer build (no installer, no javadoc):

```bash
ant package 2>&1 | tee build.log
```

### Step 5 — Check for success

```bash
grep "BUILD SUCCESSFUL\|BUILD FAILED" build.log
```

Output should be:
```
BUILD SUCCESSFUL
```

### Step 7 — Locate build outputs

| Output | Path |
|---|---|
| Server JARs | `export/jars/` |
| CE RCP zips (Linux, Windows, macOS) | `export/eclipse/` |
| OSGi embedded bundles | `export/osgi/embedded/` |
| Ship tree | `ship/` |
| Build log | `build.log` |

---

## Troubleshooting

### `TOOLS_HOME is not set` or `project.tools_home` resolves to empty

`project_setup.xml` reads `TOOLS_HOME` from the environment via
`${project_env_vars.TOOLS_HOME}`. Ensure it is exported before running ant:

```bash
echo $TOOLS_HOME   # must not be blank
export TOOLS_HOME=<PROJECT_HOME>/SyncWeave/tools
```

### `Unable to create javax script engine for javascript`

The `nashorn-core.jar` must be on the classpath for `osgi/build.xml` tasks.
It is picked up automatically from `lib/ivy/` after `ant resolve`.
If you skipped the Ivy resolve step, run it.


### PDE build fails: `Host plug-in JavaSE_0.0.0 has not been found`

Re-run the OSGi patch script (Step 3). This happens if the tools tree was
refreshed or the JAR was replaced since the last patch:

```bash
build/scripts/patch_osgi_ee_profiles.sh \
  $TOOLS_HOME/eclipse/eclipse_runtime/4.27/eclipse/plugins
```

### `Overriding a previous definition of ivy:settings is not allowed`

Caused by Ant 1.10.9+ changing the default `override` mode from `yes` to
`notallowed`. Fixed in `build.xml` by adding `onerror="ignore"` to the
`<taskdef>` and `override="true"` to `<ivy:configure>`. If you see this
again it means an older unpatched `build.xml` is being used — verify the
file matches the version in this repository.

### `exec returned: 13` from `eclipse_rules.xml`

Check `export/ce_rcp_error.log` (the `errorlog.log` passed to the macro).
This is the PDE headless build stderr output and will contain the actual error.

### Javadoc errors: `package org.eclipse.*.* does not exist`

These are non-fatal warnings during `javadoc_internal`. They do not affect
`ant package`. The missing packages are Eclipse-internal APIs not needed
in the shipped Javadoc. Run `ant package` instead of `ant images` to skip
javadoc generation entirely.

---

## Quick Reference: One-liner for clean full build

```bash
export JAVA_HOME=<PROJECT_HOME>/SyncWeave/adks/ibm/jdk/jdk-21.0.12+8 && \
export TOOLS_HOME=<PROJECT_HOME>/SyncWeave/tools && \
export ANT_HOME=<PROJECT_HOME>/ant/apache-ant-1.10.17 && \
export PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$PATH && \
cd <PROJECT_HOME>/SyncWeave && \
ant images 2>&1 | tee build.log && \
grep "BUILD SUCCESSFUL\|BUILD FAILED" build.log
```
