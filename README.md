# SyncWeave

SyncWeave is the official open-source version of IBM Verify Directory Integrator. It makes VDI accessible to the community under an open-source model, enabling contributions to be submitted, reviewed, and accepted into future VDI releases — spanning both Community and Enterprise editions.

SyncWeave ships with IBM Java Community Edition. This is distinct from the JRE bundled with the Enterprise Edition. It is driven by IBM and active contributors.
SyncWeave is the recommended entry point for identity engineers who want to contribute, evaluate or extend VDI without an enterprise license. Source code and binaries are published to the IBM SyncWeave GitHub repository. Core VDI concepts such as AssemblyLines, connectors, scripting, and the Configuration Editor apply equally to both editions.

## Overview

SyncWeave is an enterprise-grade tool for the real-time synchronization and transformation of identity data across heterogeneous repositories. It includes LDAP directories, relational databases, and operating system account stores. As, identity schemas vary significantly between systems, SyncWeave not only move data but also transform records as they flow. It applies configurable attribute mapping rules and data transformations expressed through Java/JavaScript scripting to align data formats. The result is an authoritative, enterprise-spanning identity infrastructure with no limitation on the type of data or connected systems. SyncWeave includes a rich library of built-in connectors and an open-architecture Java development environment for building or extending connectors as needs evolve.

SyncWeave approaches integration as an incremental, verifiable engineering discipline. Solutions are built one AssemblyLine at a time, and each step can be run and tested immediately. This provides continuous feedback, makes project planning easier, and allows teams to demonstrate progress throughout the project. Effort can often be reduced to counting and costing individual data flows. Teams can demonstrate working progress to stakeholders at every stage rather than waiting for a finished product. VDI further simplifies integration by abstracting away the technical differences between data sources. As a result, architects and engineers can focus on business requirements rather than the technical details of each connected system.

The SyncWeave toolset is built on Eclipse and centres on two programs that work in concert: the **Configuration Editor (CE)**, where solutions are designed, built, tested, and debugged; and the Server, the **runtime engine** that executes deployed workflows. The CE and Server are designed to work seamlessly together — and across platforms — so a developer can build and test locally while running solutions on a remote mainframe or inside a container. Integration projects accumulate into libraries of reusable components and business logic that compound in value over time, making each new challenge faster to address.

## Features

| Feature | Description |
|---|---|
| **AssemblyLines** | Components are assembled into ordered pipelines called AssemblyLines. Each AL carries a single Entry — a schema-flexible bucket of Attribute-Value pairs — from input sources through transformation logic to one or more output targets. It processes one record at a time per cycle. |
| **Configuration Editor** | An Eclipse-based graphical IDE for writing, testing, and debugging AssemblyLines. Configs are stored as XML and deployed to one or more ", " Servers; the CE and Server work in concert so developers can build locally and run remotely on a mainframe or in a container. |
| **Delta and change-only synchronization** | A Connector in Delta mode snapshots the source and compares it against the previous run's snapshot stored in the System Store. It feeds only added, modified, or deleted entries into the pipeline. Update mode compares outgoing attributes against the existing and writes only the attributes that have changed, reducing unnecessary updates. |
| **SCIM 2.0 service and connector** | A built-in SCIM service exposes SyncWeave over the SCIM protocol for standards-based provisioning and authentication. A companion SCIM Connector lets AssemblyLines read from and write to any SCIM-compliant server, making SyncWeave a first-class citizen in modern cloud and governance identity ecosystems. |
| **API and CLI access** | Solutions can be triggered, managed, and monitored programmatically via the SyncWeave REST API and command-line interface — not just through the CE. This makes SyncWeave straightforward to embed in CI/CD pipelines, orchestration tools, or scheduled automation without manual intervention. |
| **Unified logging, tracing, and error handling** | Built-in hooks at the kernel level provide consistent logging, diagnostics, and error handling for every AssemblyLine. This eliminates the need to implement separate error logic in each integration. Standardized log messages with the CTGDI prefix make monitoring, log aggregation, and alerting easier across SyncWeave Servers. |
| **Adapter packaging** | An AssemblyLine can be packaged as a reusable Connector, called an Adapter. This allows a complex integration to be built once and reused as a standard Connector in other AssemblyLines. This compounds the value of each completed integration across future projects. |
| **Password synchronization plug-ins** | Plug-ins installed in Windows Active Directory, Linux/POSIX, and SyncWeave intercept password changes before they are hashed. The password updates are then propagated to downstream systems in real time through the same SyncWeave pipeline used for other identity data. |
| **Federated Directory Server** | A built-in LDAP V3 front-end that virtualises heterogeneous backends — LDAP directories, JDBC databases, flat files, and custom targets — behind a single endpoint. LDAP-speaking applications see a unified directory; flow hooks call SyncWeave AssemblyLines for filtering, auditing, and custom transformation at every operation. |
| **Event-driven and scheduled execution** | AssemblyLines can run as batch jobs on a schedule, as continuous listeners via Server Mode Connectors (HTTP, LDAP), respond to SyncWeave notification events emitted by other ALs, or triggered by Delta-detected changes. These execution methods can be used individually or combined within the same solution. |

## Platform Support

SyncWeave is available on the following platforms:

| Platform | Architecture |
|---|---|
| AIX | POWER (PPC64) |
| Linux | x86-64 |
| Linux | zSeries / s/390 |
| Windows | x86-64 |

## Documentation

For comprehensive documentation on SyncWeave please visit the [official IBM documentation](https://www.ibm.com/docs/en/vdi/11.0.0).
Note that SyncWeave currently supports on-premises deployments. While the documentation also includes information related to container-based deployments, those sections apply to IBM Verify Directory Integrator (VDI). They are not supported as part of the SyncWeave open-source project.

## Contributing

We welcome contributions to this repository! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to submit contributions.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Security

If you discover an issue, please follow the guidelines in [SECURITY.md](SECURITY.md).

## Code of Conduct

Please review our [Code of Conduct](CODE_OF_CONDUCT.md) before contributing to this project.
