# SyncWeave

This is the official GitHub repository for SyncWeave — an open-source version of IBM Verify Directory Integrator.

## Overview

SyncWeave is an enterprise-grade tool for the real-time synchronization and transformation of identity data across heterogeneous repositories — including LDAP directories, relational databases, and operating system account stores. Because identity schemas vary significantly between systems, SyncWeave does not simply move data; it also transforms records as they flow, applying configurable attribute mapping rules and data transformations expressed through Java/JavaScript scripting. The result is an authoritative, enterprise-spanning identity infrastructure with virtually no limitation on the type of data or connected systems it can handle — backed by a rich library of built-in connectors and an open-architecture Java development environment for building or extending connectors as needs evolve.

SyncWeave approaches integration as an incremental, verifiable engineering discipline. Solutions are built one AssemblyLine at a time, with each step immediately runnable and testable, enabling continuous feedback throughout the project. This granularity makes integration work easier to estimate and plan — effort can often be reduced to counting and costing individual data flows — and allows teams to demonstrate working progress to stakeholders at every stage rather than waiting for a finished product. VDI further accelerates this by abstracting away the technical differences between data sources, so architects and engineers spend their time on business requirements rather than the low-level mechanics of each connected system.

The SyncWeave toolset is built on Eclipse and centres on two programs that work in concert: the **Configuration Editor (CE)**, where solutions are designed, built, tested, and debugged; and the Server, the **runtime engine** that executes deployed workflows. The CE and Server are designed to work seamlessly together — and across platforms — so a developer can build and test locally while running solutions on a remote mainframe or inside a container. Integration projects accumulate into libraries of reusable components and business logic that compound in value over time, making each new challenge faster to address.

## Features

| Feature | Description |
|---|---|
| **AssemblyLines** | Components are assembled into ordered pipelines called AssemblyLines. Each AL carries a single Entry — a schema-flexible bucket of Attribute-Value pairs — from input sources through transformation logic to one or more output targets, one record at a time per cycle. |
| **Configuration Editor** | An Eclipse-based graphical IDE for writing, testing, and debugging AssemblyLines. Configs are stored as XML and deployed to one or more ", " Servers; the CE and Server work in concert so developers can build locally and run remotely on a mainframe or in a container. |
| **Delta and change-only synchronization** | A Connector in Delta mode snapshots the source, compares it against the previous run's snapshot stored in the System Store, and feeds only added, modified, or deleted entries into the pipeline. Update mode additionally compares outgoing attributes against what already exists on the target — writing only what has actually changed. |
| **SCIM 2.0 service and connector** | A built-in SCIM service exposes SyncWeave over the SCIM protocol for standards-based provisioning and authentication. A companion SCIM Connector lets AssemblyLines read from and write to any SCIM-compliant server, making SyncWeave a first-class citizen in modern cloud and governance identity ecosystems. |
| **API and CLI access** | Solutions can be triggered, managed, and monitored programmatically via the SyncWeave REST API and command-line interface — not just through the CE. This makes SyncWeave straightforward to embed in CI/CD pipelines, orchestration tools, or scheduled automation without manual intervention. |
| **Unified logging, tracing, and error handling** | Hooks at the kernel level give every AssemblyLine consistent diagnostics and structured failure handling without needing to build error logic into each integration individually. All messages carry the `CTGDI` prefix, making log aggregation and alerting straightforward across a fleet of SyncWeave Servers. |
| **Adapter packaging** | An entire AssemblyLine can be packaged as a reusable Connector — called an Adapter — so a complex integration built once is exposed as a simple building block that other ALs can use like any other Connector. This compounds the value of each completed integration across future projects. |
| **Password synchronization plug-ins** | Plug-ins installed into Windows Active Directory, Linux/POSIX, and SyncWeave intercept password changes at the point they occur — before hashing — and propagate them to downstream systems in real time through the same SyncWeave pipeline used for all other identity data. |
| **Federated Directory Server** | A built-in LDAP V3 front-end that virtualises heterogeneous backends — LDAP directories, JDBC databases, flat files, and custom targets — behind a single endpoint. LDAP-speaking applications see a unified directory; flow hooks call SyncWeave AssemblyLines for filtering, auditing, and custom transformation at every operation. |
| **Event-driven and scheduled execution** | AssemblyLines can run as batch jobs on a schedule, as continuous listeners via Server Mode Connectors (HTTP, LDAP), in response to SyncWeave notification events emitted by other ALs, or triggered by Delta-detected changes — and these patterns can be freely combined in the same solution. |

## Documentation

For comprehensive documentation on SyncWeave please visit the [official IBM documentation](https://www.ibm.com/docs/en/vdi/11.0.0).

## Contributing

We welcome contributions to this repository! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to submit contributions.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Security

If you discover an issue, please follow the guidelines in [SECURITY.md](SECURITY.md).

## Code of Conduct

Please review our [Code of Conduct](CODE_OF_CONDUCT.md) before contributing to this project.
