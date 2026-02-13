package org.grails.plugin.db.multitenancy.vpd.resolvers

import org.grails.plugin.db.multitenancy.vpd.config.VpdConfig
import groovy.transform.CompileStatic
import javax.servlet.http.HttpServletRequest

@CompileStatic
class SessionTenantResolver implements TenantResolver {

    VpdConfig config

    @Override
    String resolve(HttpServletRequest request) {
        return request.getSession(false)
                ?.getAttribute(config.sessionAttribute)
    }
}
