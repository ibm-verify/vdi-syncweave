# Copyright Notices

Every source file must contain an open-source copyright notice at the top.

As part of IBM Security Directory Integrator's transition to open source, all
source files now carry an Apache 2.0 SPDX identifier. The project is licensed
under the [Apache License, Version 2.0](LICENSE).

Every developer is responsible for ensuring the copyright notice is present and
correct in any files they create or modify. This is checked during code review.

## Format

### Java / C / C++

```java
/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
```

### Shell Scripts / Makefiles / Python

```sh
# Copyright contributors to the SyncWeave project
#
# SPDX-License-Identifier: Apache-2.0
```

## Automation

The script [`tools/add_copyright_header.py`](tools/add_copyright_header.py)
can be used to bulk-apply or audit headers across the repository:

```sh
# Dry run — see what would change
python3 tools/add_copyright_header.py --dry-run

# Apply
python3 tools/add_copyright_header.py
```
