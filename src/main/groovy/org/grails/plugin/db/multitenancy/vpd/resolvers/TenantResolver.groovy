package org.grails.plugin.db.multitenancy.vpd.resolvers

import javax.servlet.http.HttpServletRequest

interface TenantResolver {
    String resolve(HttpServletRequest request)
}

