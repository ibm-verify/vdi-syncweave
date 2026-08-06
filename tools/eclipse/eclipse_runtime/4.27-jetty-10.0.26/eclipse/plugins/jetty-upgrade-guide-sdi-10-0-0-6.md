# Jetty Upgrade Guide — SDI 10.0.0.6

**Current build wiring:** Jetty jars are sourced from the Eclipse runtime plugin directory configured by
[`root.eclipse_runtime_vers`](rules_mk/tools_setup.xml:72) and included by wildcard in
[`rules_mk/tools_setup.xml:691`](rules_mk/tools_setup.xml:691).

**Current code compatibility:** The only direct Jetty API usage is in
[`HttpSessionCleanupEnabler`](osgi/plugins/org.eclipse.equinox.http.jetty.listener/src/com/ibm/di/http/jetty/listener/internal/impl/HttpSessionCleanupEnabler.java:37),
which imports `JettyCustomizer`, `ServletContextHandler`, and `HttpConfiguration`. These APIs are stable
across all Jetty 10.0.x patch versions.

**Servlet API status:** [`ivy.xml:100`](ivy.xml:100) pins `javax.servlet-api` `4.0.1`, which remains correct
for all Jetty 10.0.x versions. No changes to Servlet API or `javax.servlet` package names are needed.

---

## Corrected conclusion

Do **not** assume that a newer Eclipse 4.27.x build will provide Jetty 10.0.26. No standard Eclipse 4.27.x
release reliably bundles Jetty 10.0.26.

For this codebase, the practical upgrade path to Jetty 10.0.26 is to **curate a custom Eclipse runtime
plugin set** — copy the existing `4.27` runtime directory, replace only the runtime-critical Jetty OSGi
bundles, and point the build to it.

---

## How Jetty is currently wired into the build

| File | Role |
| --- | --- |
| [`rules_mk/tools_setup.xml:72`](rules_mk/tools_setup.xml:72) | Selects the Eclipse runtime version directory via `root.eclipse_runtime_vers`. |
| [`rules_mk/tools_setup.xml:691`](rules_mk/tools_setup.xml:691) | Wildcard `org.eclipse.jetty.*.jar` picks up all Jetty bundles from the runtime plugins folder. |
| [`osgi/build.xml:877–889`](osgi/build.xml:877) | Copies specific Jetty and Equinox HTTP jars into the embedded OSGi output directory using **exact jar names**. |
| [`ivy.xml:100`](ivy.xml:100) | Pulls `javax.servlet-api` `4.0.1` via Ivy — no Jetty jars come through Ivy. |
| [`HttpSessionCleanupEnabler`](osgi/plugins/org.eclipse.equinox.http.jetty.listener/src/com/ibm/di/http/jetty/listener/internal/impl/HttpSessionCleanupEnabler.java:37) | Only direct Jetty API consumer in source code. |

---

## Current Eclipse 4.27 Jetty bundle inventory

The `4.27/eclipse/plugins/` directory contains the following Jetty-related bundles.
They fall into three categories: **runtime** (required at JVM run time), **source** (developer reference, not
deployed), and **locale/NL fragments** (translations, not deployed to the server).

### Runtime bundles — the ones that matter

| Bundle filename | OSGi symbolic name | Notes |
| --- | --- | --- |
| `org.eclipse.equinox.http.jetty_3.8.200.v20221109-0702.jar` | `org.eclipse.equinox.http.jetty` | Equinox bridge — **high-risk** (see below) |
| `org.eclipse.jetty.http_10.0.15.jar` | `org.eclipse.jetty.http` | Core HTTP parsing |
| `org.eclipse.jetty.io_10.0.15.jar` | `org.eclipse.jetty.io` | I/O infrastructure |
| `org.eclipse.jetty.security_10.0.15.jar` | `org.eclipse.jetty.security` | Security/authentication |
| `org.eclipse.jetty.server_10.0.15.jar` | `org.eclipse.jetty.server` | Core server |
| `org.eclipse.jetty.servlet_10.0.15.jar` | `org.eclipse.jetty.servlet` | Servlet support |
| `org.eclipse.jetty.util_10.0.15.jar` | `org.eclipse.jetty.util` | Utilities |
| `org.eclipse.jetty.util.ajax_10.0.15.jar` | `org.eclipse.jetty.util.ajax` | AJAX/JSON utilities |

### Source bundles — not deployed, safe to ignore for the upgrade

These bundles contain Java source for IDE navigation. They have no effect on the runtime.

```
org.eclipse.equinox.http.jetty.source_3.8.200.v20221109-0702.jar
org.eclipse.jetty.http.source_10.0.15.jar
org.eclipse.jetty.io.source_10.0.15.jar
org.eclipse.jetty.security.source_10.0.15.jar
org.eclipse.jetty.server.source_10.0.15.jar
org.eclipse.jetty.servlet.source_10.0.15.jar
org.eclipse.jetty.util.source_10.0.15.jar
org.eclipse.jetty.util.ajax.source_10.0.15.jar
```

### Locale/NL fragment bundles — not deployed, safe to ignore for the upgrade

These carry translated strings and attach to their host bundle as OSGi fragments at development time only.

```
org.eclipse.equinox.http.jetty.nl1_3.8.200.v202304261923.jar
org.eclipse.equinox.http.jetty.nl2_3.8.200.v202304261923.jar
org.eclipse.equinox.http.jetty.nl2a_3.8.200.v202304261923.jar
org.eclipse.equinox.http.jetty.nlBidi_3.8.200.v202304261923.jar
org.eclipse.jetty.http.nl1_10.0.15.v202304261923.jar
org.eclipse.jetty.http.nl2_10.0.15.v202304261923.jar
org.eclipse.jetty.http.nl2a_10.0.15.v202304261923.jar
org.eclipse.jetty.http.nlBidi_10.0.15.v202304261923.jar
```

---

## Direct 10.0.15 → 10.0.26 bundle mapping

These are the **seven runtime Jetty bundles** to replace. The symbolic names are identical; only the version
suffix changes. The Maven Central coordinates to obtain the OSGi-packaged jars are shown alongside.

| Current filename | Target filename | Maven artifact |
| --- | --- | --- |
| `org.eclipse.jetty.http_10.0.15.jar` | `org.eclipse.jetty.http_10.0.26.jar` | `org.eclipse.jetty:jetty-http:10.0.26` |
| `org.eclipse.jetty.io_10.0.15.jar` | `org.eclipse.jetty.io_10.0.26.jar` | `org.eclipse.jetty:jetty-io:10.0.26` |
| `org.eclipse.jetty.security_10.0.15.jar` | `org.eclipse.jetty.security_10.0.26.jar` | `org.eclipse.jetty:jetty-security:10.0.26` |
| `org.eclipse.jetty.server_10.0.15.jar` | `org.eclipse.jetty.server_10.0.26.jar` | `org.eclipse.jetty:jetty-server:10.0.26` |
| `org.eclipse.jetty.servlet_10.0.15.jar` | `org.eclipse.jetty.servlet_10.0.26.jar` | `org.eclipse.jetty:jetty-servlet:10.0.26` |
| `org.eclipse.jetty.util_10.0.15.jar` | `org.eclipse.jetty.util_10.0.26.jar` | `org.eclipse.jetty:jetty-util:10.0.26` |
| `org.eclipse.jetty.util.ajax_10.0.15.jar` | `org.eclipse.jetty.util.ajax_10.0.26.jar` | `org.eclipse.jetty:jetty-util-ajax:10.0.26` |

> **Important:** Maven Central distributes these jars as standard Maven artifacts. Before dropping them into
> `eclipse/plugins/`, verify each jar has a valid `Bundle-SymbolicName` in its `META-INF/MANIFEST.MF`.
> Jetty 10.0.x ships proper OSGi manifests, so this is typically fine — but always check.

---

## Special concern: `org.eclipse.equinox.http.jetty`

This bundle is **not** a Jetty library. It is the Equinox-to-Jetty bridge written by Eclipse. It was compiled
against specific `org.eclipse.jetty.*` package imports and exports.

Current version in 4.27: `org.eclipse.equinox.http.jetty_3.8.200.v20221109-0702.jar`

**What this means for the upgrade:**

1. If `org.eclipse.equinox.http.jetty_3.8.200` imports packages from `org.eclipse.jetty.*` with
   `version="[10.0.0,11.0.0)"` (or similar range), it will wire cleanly to 10.0.26 bundles and **no change
   is needed** to this bundle.
2. If the import range is tighter (e.g. `[10.0.15,10.0.16)`), it will fail to resolve at runtime against
   10.0.26 bundles and you will need a newer `org.eclipse.equinox.http.jetty` from a later Eclipse build.

**How to check — run this against the current jar:**

```bash
unzip -p ${TOOLS_HOME}/eclipse/eclipse_runtime/4.27/eclipse/plugins/org.eclipse.equinox.http.jetty_3.8.200.v20221109-0702.jar \
  META-INF/MANIFEST.MF | grep -A1 "Import-Package\|Require-Bundle"
```

Look for any `org.eclipse.jetty` import with a version upper bound below `10.0.26`. If all bounds are open or
wide (e.g. `[10.0.0,11.0.0)`), proceed. If they are not, source a newer Equinox bridge bundle from Eclipse
2023-09 (4.29) or later.

---

## Step-by-step upgrade procedure

### Step 1 — Create a curated runtime directory

Copy the existing Eclipse 4.27 runtime tree to a versioned target:

```bash
cp -r ${TOOLS_HOME}/eclipse/eclipse_runtime/4.27 \
      ${TOOLS_HOME}/eclipse/eclipse_runtime/4.27-jetty-10.0.26
```

All non-Jetty bundles (platform, equinox, felix, OSGi, etc.) remain untouched. Only the seven Jetty runtime
bundles will be replaced.

### Step 2 — Download Jetty 10.0.26 OSGi bundles

Download from Maven Central (or your internal mirror):

```bash
JETTY_VER=10.0.26
JETTY_ARTIFACTS="jetty-http jetty-io jetty-security jetty-server jetty-servlet jetty-util jetty-util-ajax"
PLUGINS=${TOOLS_HOME}/eclipse/eclipse_runtime/4.27-jetty-10.0.26/eclipse/plugins

for artifact in $JETTY_ARTIFACTS; do
  mvn dependency:get \
    -Dartifact=org.eclipse.jetty:${artifact}:${JETTY_VER} \
    -Ddest=${PLUGINS}
done
```

After download, rename each jar to the expected OSGi plugin filename convention if necessary, for example:

```bash
# Maven downloads as jetty-server-10.0.26.jar; Eclipse expects org.eclipse.jetty.server_10.0.26.jar
# The jar's internal Bundle-SymbolicName will be org.eclipse.jetty.server regardless of filename,
# but consistent naming avoids confusion.
mv ${PLUGINS}/jetty-http-10.0.26.jar           ${PLUGINS}/org.eclipse.jetty.http_10.0.26.jar
mv ${PLUGINS}/jetty-io-10.0.26.jar             ${PLUGINS}/org.eclipse.jetty.io_10.0.26.jar
mv ${PLUGINS}/jetty-security-10.0.26.jar       ${PLUGINS}/org.eclipse.jetty.security_10.0.26.jar
mv ${PLUGINS}/jetty-server-10.0.26.jar         ${PLUGINS}/org.eclipse.jetty.server_10.0.26.jar
mv ${PLUGINS}/jetty-servlet-10.0.26.jar        ${PLUGINS}/org.eclipse.jetty.servlet_10.0.26.jar
mv ${PLUGINS}/jetty-util-10.0.26.jar           ${PLUGINS}/org.eclipse.jetty.util_10.0.26.jar
mv ${PLUGINS}/jetty-util-ajax-10.0.26.jar      ${PLUGINS}/org.eclipse.jetty.util.ajax_10.0.26.jar
```

Then remove the old 10.0.15 Jetty runtime jars from the curated directory:

```bash
rm ${PLUGINS}/org.eclipse.jetty.http_10.0.15.jar
rm ${PLUGINS}/org.eclipse.jetty.io_10.0.15.jar
rm ${PLUGINS}/org.eclipse.jetty.security_10.0.15.jar
rm ${PLUGINS}/org.eclipse.jetty.server_10.0.15.jar
rm ${PLUGINS}/org.eclipse.jetty.servlet_10.0.15.jar
rm ${PLUGINS}/org.eclipse.jetty.util_10.0.15.jar
rm ${PLUGINS}/org.eclipse.jetty.util.ajax_10.0.15.jar
```

Leave all source bundles, NL fragments, and Equinox bundles in place for now.

### Step 3 — Verify the Equinox bridge manifests

```bash
unzip -p ${PLUGINS}/org.eclipse.equinox.http.jetty_3.8.200.v20221109-0702.jar \
  META-INF/MANIFEST.MF | grep "org\.eclipse\.jetty"
```

- If version ranges are `[10.0.0,11.0.0)` or similar — the bundle wires to 10.0.26. **No action needed.**
- If version ranges are tighter — replace the Equinox bridge bundle with one from a later Eclipse build
  (Eclipse 2023-09 / 4.29 ships `org.eclipse.equinox.http.jetty_3.8.300` or later).

### Step 4 — Point the build to the curated runtime

In [`rules_mk/tools_setup.xml:72`](rules_mk/tools_setup.xml:72), change:

```xml
<!-- before -->
<property name="root.eclipse_runtime_vers" value="4.27"/>

<!-- after -->
<property name="root.eclipse_runtime_vers" value="4.27-jetty-10.0.26"/>
```

No other changes are needed in this file. The wildcard include at
[`rules_mk/tools_setup.xml:691`](rules_mk/tools_setup.xml:691) will automatically pick up the new
`org.eclipse.jetty.*_10.0.26.jar` bundles.

### Step 5 — Update exact jar-copy entries in [`osgi/build.xml`](osgi/build.xml)

[`osgi/build.xml:877–889`](osgi/build.xml:877) copies jars by **exact filename**. The current entries use
unsuffixed names such as `org.eclipse.jetty.server.jar`. The curated runtime jars are named
`org.eclipse.jetty.server_10.0.26.jar`.

Update each Jetty include line to use a `*` wildcard pattern:

```xml
<!-- before -->
<include name="org.eclipse.jetty.http.jar" />
<include name="org.eclipse.jetty.io.jar" />
<include name="org.eclipse.jetty.security.jar" />
<include name="org.eclipse.jetty.server.jar" />
<include name="org.eclipse.jetty.servlet.jar" />
<include name="org.eclipse.jetty.util.jar" />
<include name="org.eclipse.jetty.util.ajax.jar" />

<!-- after -->
<include name="org.eclipse.jetty.http_*.jar" />
<include name="org.eclipse.jetty.io_*.jar" />
<include name="org.eclipse.jetty.security_*.jar" />
<include name="org.eclipse.jetty.server_*.jar" />
<include name="org.eclipse.jetty.servlet_*.jar" />
<include name="org.eclipse.jetty.util_*.jar" />
<include name="org.eclipse.jetty.util.ajax_*.jar" />
```

Apply the same treatment to the Equinox HTTP bridge line if it also uses an unsuffixed name:

```xml
<!-- before -->
<include name="org.eclipse.equinox.http.jetty.jar" />

<!-- after -->
<include name="org.eclipse.equinox.http.jetty_*.jar" />
```

> Using `_*.jar` wildcards is safe because the version suffix is unique per bundle and these directories
> contain exactly one version of each bundle.

### Step 6 — Verify source-level compatibility

No source changes are expected. Confirm these three classes still exist in the curated bundles:

| Import | Bundle |
| --- | --- |
| `org.eclipse.equinox.http.jetty.JettyCustomizer` | `org.eclipse.equinox.http.jetty_*.jar` |
| `org.eclipse.jetty.servlet.ServletContextHandler` | `org.eclipse.jetty.servlet_10.0.26.jar` |
| `org.eclipse.jetty.server.HttpConfiguration` | `org.eclipse.jetty.server_10.0.26.jar` |

Quick check:

```bash
jar tf ${PLUGINS}/org.eclipse.jetty.servlet_10.0.26.jar | grep "ServletContextHandler"
jar tf ${PLUGINS}/org.eclipse.jetty.server_10.0.26.jar  | grep "HttpConfiguration"
```

Both should return a match. For Jetty 10.0.x patches these APIs are stable.

---

## OSGi bundle compatibility checklist

Before starting the full build, run through this checklist manually or with `jar tf` / `unzip -p … MANIFEST.MF`:

- [ ] Each of the seven Jetty 10.0.26 jars has `Bundle-SymbolicName` matching its `org.eclipse.jetty.*` name.
- [ ] Each jar has `Bundle-Version: 10.0.26`.
- [ ] `org.eclipse.equinox.http.jetty` `Import-Package` version ranges include `10.0.26` (see Step 3).
- [ ] No extra `Require-Bundle` or `Import-Package` entries in 10.0.26 bundles reference a bundle not present
  in the curated plugins directory.
- [ ] The `org.eclipse.equinox.http.jetty.listener` fragment-host declaration still resolves.
- [ ] `javax.servlet.jar` or `jakarta.servlet-api.jar` from the original runtime is still present (required
  by Jetty's servlet bundle).

---

## What does not need to change

| Item | Reason |
| --- | --- |
| [`ivy.xml:100`](ivy.xml:100) `javax.servlet-api` `4.0.1` | Jetty 10.0.x still requires `javax.servlet 4.0`. |
| All Java source files | No `javax.servlet` → `jakarta.servlet` rename needed. |
| All `MANIFEST.MF` files in `osgi/plugins/` | Package namespace unchanged for Jetty 10.0.x. |
| [`rules_mk/tools_setup.xml:691`](rules_mk/tools_setup.xml:691) wildcard include | Already picks up any `org.eclipse.jetty.*.jar`. |
| Source and NL fragment bundles | Not deployed to the embedded server; no action needed. |

---

## Validation steps

1. Inspect `org.eclipse.equinox.http.jetty` manifest imports (Step 3 above) and confirm version range covers 10.0.26.
2. Run `ant resolve init` to confirm Ivy dependencies still resolve cleanly.
3. Run `ant` (full build) and confirm `org.eclipse.equinox.http.jetty.listener` compiles without errors.
4. Confirm the copy phase in [`osgi/build.xml:877`](osgi/build.xml:877) succeeds and the 10.0.26 jars appear in the output directory.
5. Start the embedded OSGi server and confirm the Equinox HTTP service activates.
6. Verify the REST API responds at `/rest`.
7. Run `SDI/unit_tests/src_func/…/FN_39_TP_Server_Authentication_CVT.java` to exercise the Jetty layer end-to-end.

---

## Files to change — summary

| File | Change |
| --- | --- |
| [`rules_mk/tools_setup.xml:72`](rules_mk/tools_setup.xml:72) | Change `root.eclipse_runtime_vers` value to `4.27-jetty-10.0.26`. |
| [`osgi/build.xml:877–889`](osgi/build.xml:877) | Change exact Jetty jar includes to `_*.jar` wildcard patterns. |
| Curated runtime `plugins/` directory | Replace seven Jetty runtime jars `10.0.15` → `10.0.26`. |
| `org.eclipse.equinox.http.jetty_*.jar` | Keep unchanged unless manifest inspection reveals incompatible import ranges. |
| Everything else | No changes needed. |
