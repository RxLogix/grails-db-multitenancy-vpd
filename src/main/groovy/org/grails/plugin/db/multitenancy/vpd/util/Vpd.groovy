package org.grails.plugin.db.multitenancy.vpd.util

import org.grails.plugin.db.multitenancy.vpd.TenantContext

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class Vpd {

    /**
     * Run a block of code with a specific tenant context
     * Automatically restores previous tenant afterward
     */
    static <T> T runWithTenant(String tenant, Closure<T> work) {
        def previous = TenantContext.get()
        try {
            TenantContext.set(tenant)
            return work.call()
        } finally {
            if (previous) {
                TenantContext.set(previous)
            } else {
                TenantContext.clear()
            }
        }
    }

    /**
     * Java-friendly version
     */
    static <T> T callWithTenant(String tenant, Callable<T> work) {
        def previous = TenantContext.get()
        try {
            TenantContext.set(tenant)
            return work.call()
        } finally {
            if (previous) {
                TenantContext.set(previous)
            } else {
                TenantContext.clear()
            }
        }
    }

    /**
     * Wrap a Runnable so it propagates tenant context
     */
    static Runnable wrap(String tenant, Runnable task) {
        return {
            runWithTenant(tenant) {
                task.run()
            }
        } as Runnable
    }

    /**
     * Submit work to an ExecutorService with tenant context
     */
    static <T> Future<T> submit(
            ExecutorService executor,
            String tenant,
            Callable<T> task) {

        return executor.submit {
            callWithTenant(tenant, task)
        } as Callable<T>
    }

    /**
     * Fire-and-forget async
     */
    static void execute(
            ExecutorService executor,
            String tenant,
            Runnable task) {

        executor.execute(wrap(tenant, task))
    }
}
