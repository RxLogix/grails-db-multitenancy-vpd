package org.grails.plugin.db.multitenancy.vpd.resolvers

import org.grails.plugin.db.multitenancy.vpd.config.VpdConfig

import javax.servlet.http.HttpServletRequest

class ApiTenantResolver implements TenantResolver {

    VpdConfig config

    @Override
    String resolve(HttpServletRequest request) {
        // Header first
        def tenant = request.getHeader(config.headerName)
        if (tenant) return tenant

//        // JWT fallback (if you use Authorization: Bearer)
//        def auth = request.getHeader("Authorization")
//        if (auth?.startsWith("Bearer ")) {
//            def token = auth.substring(7)
//            return JwtUtils.extractClaim(token, config.jwtClaim)
//        }
        return null
    }
}

