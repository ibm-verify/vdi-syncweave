# SyncWeave — Sanity & Unit Test Guide

**Prerequisites:** A successful build (see `docs/syncweave_local_build_steps.md`)

---

## Overview

SyncWeave is a directory integration platform built around the concept of
**Assembly Lines (ALs)** — data flows that read from a source connector, apply attribute mapping and
scripted logic, and write to a target connector.  After building SyncWeave, the unit-test suite
validates the key engine layers without needing an external LDAP or database server.

The repository contains three test tiers:

| Tier | Source tree | Runner script | What it covers |
|---|---|---|---|
| **Unit tests** (JUnit) | `unit_tests/src/` | `unit_tests/bin/runTestSuite.sh` | Core engine: Entry, AssemblyLine, Parsers, ScriptEngine, Connectors, REST API, … |
| **CVT / Functional tests** | `unit_tests/src_func/` | `unit_tests/bin/runcvt.sh` | Integration flows that start an embedded SyncWeave server |
| **Performance tests** | `unit_tests/configs/perf/` | `unit_tests/bin/runIntPerfSuite.sh` | Assembly Line throughput benchmarks |

For a first-time sanity check or post-code-change validation, **Step A** (build the test JARs) and
**Step B** (run the JUnit unit-test suite) are the minimum required steps.

---

## Environment Variables

The same variables used for the main build apply here.
If you have already set them, skip to [Step A](#step-a--build-the-unit-test-jars).

```bash
export JAVA_HOME=<PROJECT_HOME>/SyncWeave/adks/ibm/jdk/jdk-21.0.12+8
export PATH=$JAVA_HOME/bin:$PATH
export TOOLS_HOME=<PROJECT_HOME>/SyncWeave/tools
export ANT_HOME=<PROJECT_HOME>/ant/apache-ant-1.10.17
export PATH=$ANT_HOME/bin:$PATH
```

---

## Step A — Build the unit-test JARs

The unit-test suite has its own compilation and packaging targets.
They depend on `ant package` having run first (so that `export/jars/`, `export/osgi/` etc. exist).

### A.1 — Compile and package the test JARs

```bash
cd <PROJECT_HOME>/SyncWeave
ant package_unit_tests 2>&1 | tee unit_tests_build.log
grep "BUILD SUCCESSFUL\|BUILD FAILED" unit_tests_build.log
```

This target:
1. Runs `compile_unit_tests` — compiles all sources under `unit_tests/src/` and
   `unit_tests/src_func/` using JUnit, Hamcrest, EasyMock, Wink, Jackson and the product JARs as
   dependencies.
2. Produces `export/unit_tests/test.jar` — all compiled test classes.
3. Produces `export/unit_tests/test_DummyConnector.jar` — the stub `DummyConnector` used by
   Assembly Line tests.
4. Produces `export/unit_tests/boot.jar` — the `TestFrameworkBootLoader` entry point.
5. Copies `etc/TouchpointTemplate.xml` into `unit_tests/resources/tp/server/`.

**Shortcut — run tests in one shot after a full build:**

```bash
ant package package_unit_tests 2>&1 | tee full_build_and_tests.log
```

---

## Step B — Run the JUnit unit-test suite

The `runTestSuite.sh` script bootstraps the JVM using the product's own `setupCmdLine.sh`, then
launches `boot.jar` which in turn discovers every class ending in `Test` inside the classpath and
runs them via `JUnitCore`.

### B.1 — Quick run (all unit tests, output to console)

The script expects the product installation tree (`bin/setupCmdLine.sh`) to be present.
Point it at your `export/` tree:

```bash
cd <PROJECT_HOME>/SyncWeave

# Copy the built test JARs into the unit_tests tree so the runner can find them
cp export/unit_tests/boot.jar  unit_tests/
cp export/unit_tests/test.jar  unit_tests/lib/

# Run all unit tests, results to stdout
unit_tests/bin/runTestSuite.sh
```

> **Note:** The scripts call `../../bin/setupCmdLine.sh` relative to `unit_tests/bin/`, which
> resolves to `<PROJECT_HOME>/SyncWeave/bin/setupCmdLine.sh`.  
> Make sure `export/jars/` and `export/osgi/` are populated (i.e. `ant package` succeeded) before
> running the suite.

### B.2 — Run with XML results file

```bash
unit_tests/bin/runTestSuite.sh -o /tmp/syncweave_test_results.xml
```

The XML output follows the standard JUnit report format understood by CI tools (Jenkins,
GitHub Actions, etc.).

### B.3 — Run with a one-line summary file

```bash
unit_tests/bin/runTestSuite.sh \
    -o /tmp/syncweave_test_results.xml \
    -simple /tmp/syncweave_test_summary.txt

cat /tmp/syncweave_test_summary.txt
```

Expected output (all tests passing):

```
Tests run: NNN, Failures: 0, Errors: 0, Ignored: 0
```

### B.4 — Interpreting the exit code

| Exit code | Meaning |
|---|---|
| `0` | All tests passed |
| `> 0` | Number of failures + ignored tests — inspect the XML report |

---

## Step C — What the unit tests cover

The test suite exercises the major SyncWeave engine components.
The table below maps source packages to the corresponding architectural layers.

| Test class(es) | Package | Engine layer |
|---|---|---|
| `AssemblyLineTest`, `AssemblyLineComponentTest` | `com.ibm.di.server` | Assembly Line engine |
| `ConnectorComponentTest`, `BranchingComponentTest` | `com.ibm.di.server` | Connector components |
| `SimpleAttributeMappingTest`, `AdvancedAttributeMappingTest`, `AttributeMapping700Test` | `com.ibm.di.server` | Attribute mapping |
| `ScriptEngineTest`, `ScriptEngineOptionsTest`, `ScriptEngineOptions700Test` | `com.ibm.di.script` | JavaScript/scripting hooks |
| `Entry611Test`, `Entry700Test`, `Attribute700Test`, `Property700Test` | `com.ibm.di.entry` | Entry / Attribute data model |
| `LDIFParser700Test`, `XMLParser700Test`, `HTTPParserTest`, `IdMLParserTest`, `SPMLv2Parser700Test` | `com.ibm.di.parser` | Data parsers |
| `FileManagementConnectorTest` | `com.ibm.di.connector.filemanagement` | File connector |
| `Axis2WSServerConnectorTest` | `com.ibm.di.connector.axis2` | Web-service (Axis2) connector |
| `DISBConnectorTest`, `DISBIDMLMessageTransformerTest`, `DISBJSONMessageXFormerTest` | `com.ibm.di.connector.disb` | DISB / IdML connector |
| `ResourceHashTest` | `com.ibm.di.server` | Resource/NLS resolution |
| `BindUtilTest` | `com.ibm.di.config.bind` | Config binding utilities |
| `ConfigurationEntryTest`, `ConfigurationsFeedTest`, `JacksonInputOutputSymmetryTest`, `ServiceDocumentTest`, `ServerFeedTest`, `RecursiveTreeTraversalTest` | `com.ibm.di.api.rest` | REST API layer |
| `LogUtilsTest` | `com.ibm.di.api.syslog` | Syslog/logging utilities |
| `BindAddressPolicyImplTest`, `PortPoolSocketFactoryTest` | `com.ibm.di.api.remote.impl` | Remote (RMI) API |
| `StringUtilsTest` | `com.ibm.di.util` | Common utilities |
| `BaseUtility710Test` | `com.ibm.di.migration` | Migration utilities |

---

## Step D — Run the CVT (Component Verification Test) suite

CVT tests are functional integration tests that spin up an **embedded SyncWeave server** in-process and
exercise components end-to-end, including Derby-based Delta Store / Delta FC, the Remote API, and
Touchpoint scenarios.

```bash
cd <PROJECT_HOME>/SyncWeave

# Run all CVT tests
unit_tests/bin/runcvt.sh
```

The CVT runner uses `CVTFrameworkRunner` which discovers classes ending in `CVT`.
Each CVT class bears a `@CVTComponent` annotation that groups tests by component name (e.g.
`DeltaFC`, `DeltaStore`, `serverapi`).

### CVT components available

| CVT class | Component | Description |
|---|---|---|
| `PF1_FC_Improvement_Tests_CVT` | `DeltaFC` | Delta File Connector improvement tests (14 cases) |
| `PF1_DF_Improvement_Tests_CVT` | `DeltaStore` | Delta Store improvement tests (3 cases) |
| `PF1_SS_Improvement_Tests_CVT` | DeltaSSConnector | System Store connector tests |
| `ConfigInstanceCVT` | `serverapi` | Remote API / Config Instance |
| `FN_39_TP_Server_*_CVT` | `tp.container`, `tp.implementation`, `tp.template` | Touchpoint server tests |

> **Prerequisite for CVT tests:** The test server helper class reads the product installation
> directory from the file `unit_tests/tdi_install_dir.properties`.
> Create this file before running CVT tests:
>
> ```bash
> echo "tdi.install.dir=<PROJECT_HOME>/SyncWeave" \
>   > <PROJECT_HOME>/SyncWeave/unit_tests/tdi_install_dir.properties
> ```

---

## Step E — Quick post-change sanity checklist

Use this checklist after any code change before pushing:

```
[ ] ant package succeeds (BUILD SUCCESSFUL in build.log)
[ ] ant package_unit_tests succeeds
[ ] unit_tests/bin/runTestSuite.sh exits 0
[ ] XML results show 0 Failures, 0 Errors
[ ] If touching Entry/Attribute code   → verify Entry700Test, Attribute700Test pass
[ ] If touching parser code            → verify LDIFParser700Test, XMLParser700Test pass
[ ] If touching scripting              → verify ScriptEngineTest, ScriptEngineOptions700Test pass
[ ] If touching AssemblyLine/connector → verify AssemblyLineTest, ConnectorComponentTest pass
[ ] If touching REST API               → verify ConfigurationEntryTest, ServiceDocumentTest pass
[ ] If touching Remote API             → verify BindAddressPolicyImplTest, PortPoolSocketFactoryTest pass
```

---

## Troubleshooting

### `boot.jar` not found

The runner script looks for `boot.jar` in the `unit_tests/` directory.
Copy it from the build output:

```bash
cp export/unit_tests/boot.jar unit_tests/
```

### `test.jar` not found or class not found errors

```bash
mkdir -p unit_tests/lib
cp export/unit_tests/test.jar unit_tests/lib/
```

### `setupCmdLine.sh: No such file`

The test scripts source `../../bin/setupCmdLine.sh` relative to `unit_tests/bin/`.
That file is generated by `ant package`.
Run `ant package` first, then verify:

```bash
ls <PROJECT_HOME>/SyncWeave/bin/setupCmdLine.sh
```

### CVT tests hang or fail to connect

CVT tests start an in-process SyncWeave server on a dynamically chosen free port
(`PortProbe.getAvailablePort()`).
If the server takes too long to start, ensure no firewall blocks loopback ports and that
`tdi_install_dir.properties` points to the correct install root.

### `ClassNotFoundException: com.ibm.di.*` in test runner

The unit-test classpath is assembled from `export/osgi/embedded/` and `export/jars/`.
Re-run `ant package` to regenerate those directories before retrying:

```bash
ant package package_unit_tests
```

### Performance tests

Internal performance tests (`runIntPerfSuite.sh`) start a SyncWeave server and run
the Assembly Line defined in `unit_tests/configs/perf/BasePerfTestingAL.xml`.
These are not required for basic sanity but can be used to detect regression in throughput:

```bash
unit_tests/bin/runIntPerfSuite.sh
```

Performance properties (repeat counts, thresholds) are controlled by
`unit_tests/etc/perf.properties`.

---

## Quick Reference: Full sanity sequence

```bash
# 1. Set environment
export JAVA_HOME=<PROJECT_HOME>/SyncWeave/adks/ibm/jdk/jdk-21.0.12+8
export TOOLS_HOME=<PROJECT_HOME>/SyncWeave/tools
export ANT_HOME=<PROJECT_HOME>/ant/apache-ant-1.10.17
export PATH=$JAVA_HOME/bin:$ANT_HOME/bin:$PATH

cd <PROJECT_HOME>/SyncWeave

# 2. Build product + unit tests
ant package package_unit_tests 2>&1 | tee build.log
grep "BUILD SUCCESSFUL\|BUILD FAILED" build.log

# 3. Stage test JARs
cp export/unit_tests/boot.jar unit_tests/
mkdir -p unit_tests/lib
cp export/unit_tests/test.jar unit_tests/lib/

# 4. Run unit test suite
unit_tests/bin/runTestSuite.sh -o /tmp/results.xml -simple /tmp/summary.txt
cat /tmp/summary.txt

# 5. (Optional) Run CVT tests
echo "tdi.install.dir=<PROJECT_HOME>/SyncWeave" > unit_tests/tdi_install_dir.properties
unit_tests/bin/runcvt.sh
```
