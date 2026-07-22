/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.atom;

/**
 * Enumeration of Atom text types.
 * Custom implementation to avoid OSGi/JAXB classloading issues with Apache Wink.
 */
public enum AtomTextType {
    text,
    html,
    xhtml
}