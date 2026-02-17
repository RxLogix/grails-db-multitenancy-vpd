package org.grails.plugin.db.multitenancy.vpd

import org.grails.plugin.db.multitenancy.vpd.config.VpdConfig
import org.grails.plugin.db.multitenancy.vpd.resolvers.TenantResolverChain


class VpdInterceptor {

    int order = 1

    def vpdConfig
    def tenantResolverChain

    VpdInterceptor() {
        matchAll()
    }

    boolean before() {

        if (!vpdConfig.enabled) return true

        def uri = webRequest.currentRequest.requestURI

        if (isExcluded(uri)) {
            return true
        }

        def tenant = tenantResolverChain.resolve(webRequest.currentRequest)

        if (!tenant && vpdConfig.failIfMissingTenant) {
            render status: 401, text: "Tenant not resolved"
            return false
        }

        TenantContext.set(tenant)
        return true
    }

    boolean after() {
        TenantContext.clear()
        return true
    }

    boolean isExcluded(String uri) {
        vpdConfig.excludeUris?.any { pattern ->
            uri ==~ patternToRegex(pattern)
        }
    }

    private String patternToRegex(String pattern) {
        def p = pattern
                .replace("**", "__DOUBLE_STAR__")   // protect
                .replace("*", "[^/]*")              // single segment
                .replace("__DOUBLE_STAR__", ".*")   // multi segment

        return "^${p}\$"
    }
}

