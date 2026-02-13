package org.grails.plugin.db.multitenancy.vpd


import spock.lang.Specification

class TenantTransactionGuardSpec extends Specification {

    def cleanup() {
        TenantTransactionGuard.clear()
    }

    def "binds tenant on first call"() {
        when:
        TenantTransactionGuard.bind("tenantA")

        then:
        noExceptionThrown()
    }

    def "fails when tenant changes within transaction"() {
        given:
        TenantTransactionGuard.bind("tenantA")

        when:
        TenantTransactionGuard.check("tenantB")

        then:
        thrown(IllegalStateException)
    }

    def "allows same tenant"() {
        given:
        TenantTransactionGuard.bind("tenantA")

        when:
        TenantTransactionGuard.check("tenantA")

        then:
        noExceptionThrown()
    }
}

