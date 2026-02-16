package org.grails.plugin.db.multitenancy.vpd

import org.grails.plugin.db.multitenancy.vpd.config.VpdConfig
import org.grails.plugin.db.multitenancy.vpd.resolvers.TenantResolverChain


class VpdInterceptor {

    int order = 1

    private VpdConfig config
    private TenantResolverChain resolverChain

    VpdInterceptor(VpdConfig vpdConfig, TenantResolverChain tenantResolverChain) {
        this.config = vpdConfig
        this.resolverChain = tenantResolverChain
        matchAll()
    }

    boolean before() {

        if (!config.enabled) return true

        def uri = webRequest.currentRequest.requestURI

        if (isExcluded(uri)) {
            return true
        }

        def tenant = resolverChain.resolve(webRequest.currentRequest)

        if (!tenant && config.failIfMissingTenant) {
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
        config.excludeUris?.any { pattern ->
            uri ==~ patternToRegex(pattern)
        }
    }

    private String patternToRegex(String pattern) {
        pattern
                .replace("**", ".*")
                .replace("*", "[^/]*")
    }
}

