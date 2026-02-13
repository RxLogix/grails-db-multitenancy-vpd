package org.grails.plugin.db.multitenancy.vpd.resolvers

import groovy.transform.CompileStatic
import javax.servlet.http.HttpServletRequest

@CompileStatic
class TenantResolverChain {

    List<TenantResolver> resolvers

    String resolve(HttpServletRequest request) {
        for (r in resolvers) {
            def tenant = r.resolve(request)
            if (tenant) return tenant
        }
        return null
    }
}
