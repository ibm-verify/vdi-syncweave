# SyncWeave — Sanity & Unit Test Guide

**Prerequisites:** A successful `ant package package_unit_tests` build
(see [`docs/syncweave_local_build_steps.md`](syncweave_local_build_steps.md))

---

## Overview

SyncWeave is an integration platform built around **Assembly Lines (ALs)**, which process data flows by
reading from a source connector, applying attribute mapping and scripted logic, and writing to a target connector.
After building syncweave, the unit-test suite validates the key engine layers without requiring access to an 
external LDAP or database server.

The repository contains three test tiers:

| Tier | Source tree | Runner script | What it covers |
|---|---|---|---|
| **Unit tests** (JUnit) | `unit_tests/src/` | `unit_tests/bin/runTestSuite.sh` | Core engine: Entry, AssemblyLine, Parsers, ScriptEngine, Connectors, REST API, TP server |
| **CVT / Functional tests** | `unit_tests/src_func/` | `unit_tests/bin/runcvt.sh` | Integration flows that start an embedded SyncWeave server |
| **Performance tests** | `unit_tests/configs/perf/` | `unit_tests/bin/runIntPerfSuite.sh` | Assembly Line throughput benchmarks |

For a first-time sanity check or post-code-change validation, **Step A** (build the test JARs) and
**Step B** (run the JUnit unit-test suite) are the minimum required steps.

---

## Environment Variables

The same variables used for the main build apply here.
If you have already sourced the build environment, skip to [Step A](#step-a--build-the-unit-test-jars).

```bash
. <BUILD_HOME>/non_docker_build.sh
```

Or manually:

```bash
export JAVA_HOME=<BUILD_HOME>/SyncWeave/adks/ibm/jdk/jdk-21.0.12+8
export PATH=$JAVA_HOME/bin:$PATH
export TOOLS_HOME=<BUILD_HOME>/SyncWeave/tools
export ANT_HOME=<BUILD_HOME>/ant/apache-ant-1.10.17
export PATH=$ANT_HOME/bin:$PATH
```

---

## Step A — Build the unit-test JARs

The unit-test suite has its own compilation and packaging targets.
`ant package` must have already run so that `export/osgi/embedded/` and `export/jars/` exist.

```bash
cd <BUILD_HOME>/SyncWeave
ant package_unit_tests 2>&1 | tee unit_tests_build.log
grep "BUILD SUCCESSFUL\|BUILD FAILED" unit_tests_build.log
```

This target:
1. Compiles all sources under `unit_tests/src/` and `unit_tests/src_func/` against the product JARs.
2. Generates `unit_tests/test.jar`, containing all compiled test classes.
3. Generates `unit_tests/boot.jar`, containing the `TestFrameworkBootLoader` entry point.

**Shortcut: Run test with single command after full build**

```bash
ant package package_unit_tests 2>&1 | tee full_build.log
grep "BUILD SUCCESSFUL\|BUILD FAILED" full_build.log
```

---

## Step B — Run the JUnit unit-test suite

### B.1 — One-time setup: stage the test suite into the install

`runTestSuite.sh` resolves the product environment via `../../bin/setupCmdLine.sh` relative to
`unit_tests/bin/`, which means `unit_tests/` must live **physically inside the product install
root**, not in the source tree. A symlink will not work because `../..` traversal follows the
symlink source, not the link target.

```bash
INSTALL_ROOT=/tmp/sync_install/linux-x86_64/test_install    # adjust to your install path

# Copy the built unit_tests directory into the install root
cp -r <BUILD_HOME>/SyncWeave/unit_tests  ${INSTALL_ROOT}/unit_tests
chmod +x ${INSTALL_ROOT}/unit_tests/bin/*.sh
chmod +x ${INSTALL_ROOT}/bin/*.sh          # ensure setupCmdLine.sh and javaHome.sh are executable

# Verify the path resolves correctly
ls ${INSTALL_ROOT}/unit_tests/bin/../../bin/setupCmdLine.sh
```

### B.2 — Stage the runtime JARs

The test bootloader (`boot.jar`) builds its classpath by scanning `${INSTALL_ROOT}/jars/`.
Several JARs needed at test runtime are not part of the product install, hence copy them once:

```bash
REPO=<BUILD_HOME>/SyncWeave
INSTALL_JARS=${INSTALL_ROOT}/jars

cp ${REPO}/unit_tests/test.jar                                              ${INSTALL_JARS}/
cp ${REPO}/export/osgi/embedded/com.ibm.di.tp.server.jar                   ${INSTALL_JARS}/
cp ${REPO}/lib/ivy/unittest/junit-dep.jar                                   ${INSTALL_JARS}/
cp ${REPO}/lib/ivy/unittest/hamcrest-all.jar                                ${INSTALL_JARS}/
cp ${REPO}/lib/ivy/unittest/hamcrest-core.jar                               ${INSTALL_JARS}/
cp ${REPO}/lib/ivy/unittest/easymock.jar                                    ${INSTALL_JARS}/
cp ${REPO}/lib/ivy/unittest/spring-test.jar                                 ${INSTALL_JARS}/
cp ${REPO}/export/osgi/runtime/org.eclipse.equinox.http.service.api.jar    ${INSTALL_JARS}/
# Java 17+: replace old JAXB 2.3.0.1 (uses removed sun.misc.Unsafe.defineClass)
# with the multi-release 2.3.6 runtime that supports Java 9+
cp ${REPO}/lib/ivy/axis-2/jaxb-runtime-2.3.6.jar                           ${INSTALL_JARS}/
```

> **After a code change** that touches any product OSGi bundle, re-run `ant package` and then
> repeat the `cp …/com.ibm.di.tp.server.jar` line (and any other changed bundle's embedded JAR)
> before re-running the test suite.

### B.3 — Run all unit tests

```bash
${INSTALL_ROOT}/unit_tests/bin/runTestSuite.sh 2>&1 | tee /tmp/syncweave_test_run.log
```

A clean run produces an XML result block at the end of the output with an empty `<Failures>` section:

```xml
<Result>
    <Failures>
    </Failures>
</Result>
```

### B.4 — Run with XML results file

```bash
${INSTALL_ROOT}/unit_tests/bin/runTestSuite.sh \
    -o /tmp/syncweave_test_results.xml 2>&1 | tee /tmp/syncweave_test_run.log
```

### B.5 — Quick pass/fail check

```bash
# Zero TestHeader entries = all tests passed
grep -c "<TestHeader>" /tmp/syncweave_test_run.log
```

Output `0` means all tests passed. Any number > 0 shows failing test count — inspect the
`<Failure>` blocks in the log for `<Message>` and `<Trace>` details.

### B.6 — How `"$@"` works (passing options to the runner)

`runTestSuite.sh` ends with `"$@"` — all command-line arguments are forwarded verbatim to
`TestFrameworkRunner.main()`. This lets you pass runner options such as:

```bash
# Run only a specific test class
${INSTALL_ROOT}/unit_tests/bin/runTestSuite.sh -suite com.ibm.di.server.ConnectorComponentTest

# Run with verbose output
${INSTALL_ROOT}/unit_tests/bin/runTestSuite.sh -verbose
```

---

## Step C — What the unit tests cover
The following table maps source packages to their corresponding architectural layers.

| Test class(es) | Package | Engine layer |
|---|---|---|
| `AssemblyLineTest`, `AssemblyLineComponentTest` | `com.ibm.di.server` | Assembly Line engine |
| `ConnectorComponentTest`, `BranchingComponentTest` | `com.ibm.di.server` | Connector components |
| `SimpleAttributeMappingTest`, `AdvancedAttributeMappingTest` | `com.ibm.di.server` | Attribute mapping |
| `ScriptEngineTest`, `ScriptEngineOptionsTest` | `com.ibm.di.script` | JavaScript/scripting hooks |
| `Entry700Test`, `Attribute700Test`, `Property700Test` | `com.ibm.di.entry` | Entry / Attribute data model |
| `LDIFParser700Test`, `XMLParser700Test`, `HTTPParserTest`, `SPMLv2Parser700Test` | `com.ibm.di.parser` | Data parsers |
| `FileManagementConnectorTest` | `com.ibm.di.connector.filemanagement` | File connector |
| `DISBConnectorTest` | `com.ibm.di.connector.disb` | DISB / IdML connector |
| `ConfigurationEntryTest`, `ServiceDocumentTest`, `ServerFeedTest` | `com.ibm.di.api.rest` | REST API layer |
| `BindAddressPolicyImplTest`, `PortPoolSocketFactoryTest` | `com.ibm.di.api.remote.impl` | Remote (RMI) API |
| `TPNodeFeedTest`, `TPNodeEntryTest`, `TPTypeFeedTest`, `TPTypeEntryTest` | `com.ibm.di.tp.server.handler` | Touchpoint server — node/type REST handlers |
| `TPInstFeedTest`, `TPInstEntryTest` | `com.ibm.di.tp.server.handler` | Touchpoint server — instance REST handlers |
| `EntryTest`, `RawDataStorageFileSystemImplTest` | `com.ibm.di.tp.server` | TP server internals |
| `BindUtilTest` | `com.ibm.di.config.bind` | Config binding utilities |
| `StringUtilsTest` | `com.ibm.di.util` | Common utilities |

---

## Step D — Run the CVT (Component Verification Test) suite

CVT tests are functional integration tests that start an **embedded SyncWeave server** in-process and
exercise components end-to-end, including Derby-based Delta Store / Delta FC, the Remote API, and
Touchpoint scenarios.

```bash
${INSTALL_ROOT}/unit_tests/bin/runcvt.sh 2>&1 | tee /tmp/syncweave_cvt_run.log
```

The CVT runner uses `CVTFrameworkRunner` which discovers classes annotated with `@CVTComponent`.

### CVT components available

| CVT class | Component | Description |
|---|---|---|
| `PF1_FC_Improvement_Tests_CVT` | `DeltaFC` | Delta File Connector tests |
| `PF1_DF_Improvement_Tests_CVT` | `DeltaStore` | Delta Store tests |
| `PF1_SS_Improvement_Tests_CVT` | `DeltaSSConnector` | System Store connector tests |
| `ConfigInstanceCVT` | `serverapi` | Remote API / Config Instance |
| `FN_39_TP_Server_*_CVT` | `tp.container`, `tp.implementation`, `tp.template` | Touchpoint server tests |

> **Prerequisite:** The test server helper reads the product install path from
> the `unit_tests/tdi_install_dir.properties` file. Create it before running CVT tests:
>
> ```bash
> echo "tdi.install.dir=${INSTALL_ROOT}" \
>   > ${INSTALL_ROOT}/unit_tests/tdi_install_dir.properties
> ```

---

## Step E — Quick post-change verification checklist

Use this checklist after any code change before pushing:

```
[ ] ant package succeeds (BUILD SUCCESSFUL)
[ ] ant package_unit_tests succeeds
[ ] Re-copy changed embedded JARs into ${INSTALL_ROOT}/jars/
[ ] runTestSuite.sh produces 0 <TestHeader> entries in the log
[ ] If touching Entry/Attribute code    → verify Entry700Test, Attribute700Test pass
[ ] If touching parser code             → verify LDIFParser700Test, XMLParser700Test pass
[ ] If touching scripting               → verify ScriptEngineTest, ScriptEngineOptionsTest pass
[ ] If touching AssemblyLine/connector  → verify AssemblyLineTest, ConnectorComponentTest pass
[ ] If touching REST API                → verify ConfigurationEntryTest, ServiceDocumentTest pass
[ ] If touching Remote API              → verify BindAddressPolicyImplTest, PortPoolSocketFactoryTest pass
[ ] If touching TP server               → verify TPNodeFeedTest, TPTypeFeedTest, TPInstFeedTest pass
```

---

## Troubleshooting

### `setupCmdLine.sh: No such file` or `Incorrect TDI_JAVA_HOME`

The script resolves `../../bin/setupCmdLine.sh` relative to `unit_tests/bin/` using physical
directory traversal. Two things must be true:

1. `unit_tests/` is a **physical copy** (not a symlink) inside the install root.
2. All scripts in `${INSTALL_ROOT}/bin/` are executable: `chmod +x ${INSTALL_ROOT}/bin/*.sh`

```bash
# Verify the path resolves to the install's bin/:
ls ${INSTALL_ROOT}/unit_tests/bin/../../bin/javaHome.sh
```

### `ClassNotFoundException: com.ibm.di.test.runner.TestFrameworkRunner`

`test.jar` is not on the bootloader's scanned classpath. Copy it to `${INSTALL_ROOT}/jars/`:

```bash
cp <BUILD_HOME>/SyncWeave/unit_tests/test.jar ${INSTALL_ROOT}/jars/
```

### `NoClassDefFoundError: org/easymock/EasyMock` or `MockHttpServletRequest`

Missing test-only JARs. Re-run the full staging block from [Step B.2](#b2--stage-the-runtime-jars).

### `NoClassDefFoundError: com.ibm.di.tp.server.*`

The TP server embedded JAR is missing or stale. Re-copy it:

```bash
cp <BUILD_HOME>/SyncWeave/export/osgi/embedded/com.ibm.di.tp.server.jar \
   ${INSTALL_ROOT}/jars/
```

### `IllegalAnnotationsException` / `ExceptionInInitializerError` in TP tests

This indicates a JAXB annotation conflict in a product model class, typically caused by duplicate
`@XmlElement` and `@XmlAnyElement` on the same field (e.g. `TouchpointStatus#any`). Fix the source
file, run `ant package`, and re-copy the rebuilt `com.ibm.di.tp.server.jar`.

### `JMSException: Could not connect to broker URL`

`DISBConnectorTest` requires an ActiveMQ broker on `localhost:61616`. These tests are expected to
fail in a standalone build environment with no broker running — this is a known pre-existing
limitation, not a regression.

### CVT tests hang or fail to connect

CVT tests start an in-process SyncWeave server on a dynamically chosen free port.
Ensure no firewall blocks loopback ports and that `tdi_install_dir.properties` points to the correct
install root.

### Performance tests

Internal performance tests (`runIntPerfSuite.sh`) start a SyncWeave server and run
the Assembly Line defined in `unit_tests/configs/perf/BasePerfTestingAL.xml`.
These tests are not required for basic verification but can help detect throughput regressions:

```bash
${INSTALL_ROOT}/unit_tests/bin/runIntPerfSuite.sh
```

Performance thresholds are controlled by `unit_tests/etc/perf.properties`.

---

## Quick Reference: End-to-end sanity sequence

```bash
REPO=<BUILD_HOME>/SyncWeave
INSTALL_ROOT=/tmp/sync_install/linux-x86_64/test_install   # your install path

# 1. Source build environment
cd ${REPO}
. ../non_docker_build.sh

# 2. Build product + unit tests
ant package package_unit_tests 2>&1 | tee build.log
grep "BUILD SUCCESSFUL\|BUILD FAILED" build.log

# 3. Stage unit_tests into install (first time or after clean install)
cp -r unit_tests ${INSTALL_ROOT}/unit_tests
chmod +x ${INSTALL_ROOT}/unit_tests/bin/*.sh
chmod +x ${INSTALL_ROOT}/bin/*.sh

# 4. Stage runtime JARs into install (first time, or after adding new deps)
IJARS=${INSTALL_ROOT}/jars
cp unit_tests/test.jar                                           ${IJARS}/
cp export/osgi/embedded/com.ibm.di.tp.server.jar                ${IJARS}/
cp lib/ivy/unittest/junit-dep.jar                                ${IJARS}/
cp lib/ivy/unittest/hamcrest-all.jar                             ${IJARS}/
cp lib/ivy/unittest/hamcrest-core.jar                            ${IJARS}/
cp lib/ivy/unittest/easymock.jar                                 ${IJARS}/
cp lib/ivy/unittest/spring-test.jar                              ${IJARS}/
cp export/osgi/runtime/org.eclipse.equinox.http.service.api.jar ${IJARS}/
cp lib/ivy/axis-2/jaxb-runtime-2.3.6.jar                        ${IJARS}/

# 5. Run unit test suite
${INSTALL_ROOT}/unit_tests/bin/runTestSuite.sh 2>&1 | tee /tmp/test_run.log
grep -c "<TestHeader>" /tmp/test_run.log   # 0 = all tests passed

# 6. (Optional) Run CVT tests
echo "tdi.install.dir=${INSTALL_ROOT}" > ${INSTALL_ROOT}/unit_tests/tdi_install_dir.properties
${INSTALL_ROOT}/unit_tests/bin/runcvt.sh 2>&1 | tee /tmp/cvt_run.log
```
