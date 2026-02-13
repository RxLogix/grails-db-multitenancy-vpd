package org.grails.plugin.db.multitenancy.vpd


import org.grails.plugin.db.multitenancy.vpd.util.Vpd
import spock.lang.Specification

class VpdSpec extends Specification {

    def "runWithTenant sets and restores tenant"() {
        given:
        TenantContext.set("outer")

        when:
        def result = Vpd.runWithTenant("inner") {
            assert TenantContext.get() == "inner"
            return "ok"
        }

        then:
        result == "ok"
        TenantContext.get() == "outer"

        cleanup:
        TenantContext.clear()
    }

    def "nested runWithTenant restores correctly"() {
        when:
        Vpd.runWithTenant("A") {
            assert TenantContext.get() == "A"

            Vpd.runWithTenant("B") {
                assert TenantContext.get() == "B"
            }

            assert TenantContext.get() == "A"
        }

        then:
        TenantContext.get() == null
    }
}

