package org.grails.plugin.db.multitenancy.vpd


import org.grails.plugin.db.multitenancy.vpd.config.VpdConfig
import org.grails.plugin.db.multitenancy.vpd.resolvers.TenantResolverChain
import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class VpdInterceptorSpec extends Specification
        implements InterceptorUnitTest<VpdInterceptor> {

    def setup() {
        interceptor.vpdConfig = new VpdConfig(
                enabled: true,
                failIfMissingTenant: true,
                excludeUris: ["/login", "/health", "/console/**"]
        )

        interceptor.tenantResolverChain = Stub(TenantResolverChain) {
            resolve(_) >> "tenantA"
        }
    }

    def "excluded URI bypasses tenant logic"() {
        when:
        withRequest(uri: "/login")
        def result = interceptor.before()

        then:
        result == true
        TenantContext.get() == null
    }

    def "excluded URI with additional uri pattern bypasses tenant logic"() {
        when:
        withRequest(uri: "/console/execute")
        def result = interceptor.before()

        then:
        result == true
        TenantContext.get() == null
    }

    def "non-excluded URI sets tenant"() {
        when:
        withRequest(uri: "/api/orders")
        def result = interceptor.before()

        then:
        result == true
        TenantContext.get() == "tenantA"

        cleanup:
        interceptor.after()
    }

    def "missing tenant fails request"() {
        given:
        interceptor.tenantResolverChain = Stub(TenantResolverChain) {
            resolve(_) >> null
        }

        when:
        withRequest(uri: "/api/orders")
        def result = interceptor.before()

        then:
        result == false
    }
}
