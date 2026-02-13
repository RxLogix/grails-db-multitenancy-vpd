package org.grails.plugin.db.multitenancy.vpd.config

import groovy.transform.CompileStatic

@CompileStatic
class VpdConfig {
    boolean enabled = true
    String contextName = "VPD_CTX"
    String attributeName = "TENANT_ID"
    String headerName = "X-Tenant-ID"
    boolean failIfMissingTenant = true
    String jwtClaim
    String sessionAttribute = "TENANT_ID"
    List<String> excludeUris = []
    String systemTenantId
    boolean devMode = false
    List<String> supportedDBs = []
}
