# Copyright Notices

Every source file must contain an open-source copyright notice at the top.

As part of IBM Security Directory Integrator's transition to open source, all
source files now carry an Apache 2.0 SPDX identifier. The project is licensed
under the [Apache License, Version 2.0](LICENSE).

Every developer is responsible for ensuring the copyright notice is present and
correct in any files they create or modify. This is checked during code review.

## Format

The first listed year is the year the file was created. When modifying an
existing file, update the second year to the current year.

### Java / C / C++

```java
/*
 * Copyright IBM Corp. YYYY, 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
```

For new files created in the current year, omit the second year:

```java
/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
```

### Shell Scripts / Makefiles / Python

```sh
# Copyright IBM Corp. YYYY, 2025
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
