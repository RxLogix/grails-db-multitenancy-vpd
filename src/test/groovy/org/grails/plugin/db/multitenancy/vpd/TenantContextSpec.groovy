package org.grails.plugin.db.multitenancy.vpd


import spock.lang.Specification

class TenantContextSpec extends Specification {

    def "set and get tenant"() {
        when:
        TenantContext.set("tenantA")

        then:
        TenantContext.get() == "tenantA"

        cleanup:
        TenantContext.clear()
    }

    def "clear removes tenant"() {
        given:
        TenantContext.set("tenantA")

        when:
        TenantContext.clear()

        then:
        TenantContext.get() == null
    }
}
