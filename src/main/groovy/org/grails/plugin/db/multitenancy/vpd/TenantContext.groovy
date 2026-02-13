package org.grails.plugin.db.multitenancy.vpd

import groovy.transform.CompileStatic

@CompileStatic
class TenantContext {
    private static final ThreadLocal<String> tenant = new ThreadLocal<>()

    static void set(String t) { tenant.set(t) }

    static String get() { tenant.get() }

    static void clear() { tenant.remove() }
}