package org.grails.plugin.db.multitenancy.vpd

import groovy.transform.CompileStatic

@CompileStatic
class TenantTransactionGuard {

    private static final ThreadLocal<String> txTenant = new ThreadLocal<>()

    static void bind(String tenant) {
        if (!txTenant.get()) {
            txTenant.set(tenant)
        }
    }

    static void clear() {
        txTenant.remove()
    }

    static void check(String tenant) {
        def bound = txTenant.get()
        if (bound && bound != tenant) {
            throw new IllegalStateException(
                    "Tenant change within active transaction not allowed. " +
                            "Bound=[$bound], Attempted=[$tenant]"
            )
        }
    }
}

