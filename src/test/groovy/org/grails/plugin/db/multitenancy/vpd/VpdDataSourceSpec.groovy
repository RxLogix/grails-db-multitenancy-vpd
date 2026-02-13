package org.grails.plugin.db.multitenancy.vpd


import org.grails.plugin.db.multitenancy.vpd.config.VpdConfig
import org.grails.plugin.db.multitenancy.vpd.connection.VpdDataSource
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.Connection
import java.sql.Statement

class VpdDataSourceSpec extends Specification {

    def "fails when connection tenant and context tenant mismatch"() {
        given:
        def stmt = Mock(Statement)
        def conn = Mock(Connection) {
            getClientInfo("VPD_TENANT") >> "tenantA"
            getClientInfo() >> [:]
            createStatement() >> stmt
        }

        def realDs = Stub(DataSource) {
            getConnection() >> conn
        }

        def config = new VpdConfig(
                enabled: true,
                contextName: "VPD_CTX",
                attributeName: "TENANT_ID",
                failIfMissingTenant: true
        )

        def ds = new VpdDataSource(
                realDs,
                config
        )

        TenantContext.set("tenantA")
        ds.getConnection()

        TenantContext.set("tenantB")

        when:
        ds.getConnection()

        then:
        thrown(IllegalStateException)

        cleanup:
        TenantContext.clear()
    }
}
