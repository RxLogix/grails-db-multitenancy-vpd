package org.grails.plugin.db.multitenancy.vpd.connection

import org.grails.plugin.db.multitenancy.vpd.TenantContext
import org.grails.plugin.db.multitenancy.vpd.TenantTransactionGuard
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.plugin.db.multitenancy.vpd.config.VpdConfig

import java.sql.Connection
import java.sql.Statement
import javax.sql.DataSource
import org.springframework.jdbc.datasource.AbstractDataSource

@CompileStatic
@Slf4j
class VpdDataSource extends AbstractDataSource {

    DataSource targetDataSource
    VpdConfig config

    VpdDataSource(DataSource targetDataSource, VpdConfig config) {
        this.targetDataSource = targetDataSource
        this.config = config
    }

    @Override
    Connection getConnection() {
        Connection conn = targetDataSource.getConnection()
        applyVpd(conn)
        return new VpdAwareConnection(conn, config)
    }

    @Override
    Connection getConnection(String username, String password) {
        Connection conn = targetDataSource.getConnection(username, password)
        applyVpd(conn)
        return new VpdAwareConnection(conn, config)
    }

    void applyVpd(Connection conn) {
        String tenant = TenantContext.get()
        if (!tenant) {
            if (config.systemTenantId && (isLiquibase() || isFrameworkCall())) {
                log.warn("Liquibase Tenant Id applied")
                tenant = config.systemTenantId
            } else if (config.failIfMissingTenant) {
                throw new IllegalStateException("Tenant not set for DB access")
            } else {
                return
            }
        }

        TenantTransactionGuard.check(tenant)
        TenantTransactionGuard.bind(tenant)

        String safeTenant = tenant.replace("'", "")

        Statement stmt = conn.createStatement()
        conn.clientInfo.put('TENANT_ID', tenant)
        try {

            if (config.devMode) {
                log.warn("DevMode enabled — tenant NOT applied")
                return
            }
            if (isOracle(conn)) {

                stmt.execute("""
                BEGIN
                    DBMS_SESSION.CLEAR_CONTEXT(
                        '${config.contextName}',
                        '${config.attributeName}'
                    );

                    DBMS_SESSION.SET_CONTEXT(
                        '${config.contextName}',
                        '${config.attributeName}',
                        '${safeTenant}'
                    );
                END;
            """)

            } else if (isPostgres(conn)) {

                // Use SET LOCAL if inside TX
                if (!conn.autoCommit) {
                    stmt.execute("SET LOCAL app.tenant_id = '${safeTenant}'")
                } else {
                    stmt.execute("SET app.tenant_id = '${safeTenant}'")
                }
            }
        } finally {
            stmt.close()
        }
    }

    boolean isLiquibase() {
        return Thread.currentThread().stackTrace.any {
            it.className.contains('liquibase')
        }
    }

    boolean isFrameworkCall() {
        return Thread.currentThread().stackTrace.any {
            it.className.startsWith("org.springframework.scheduling.quartz") ||
                    it.className.startsWith("org.quartz")
        }
    }

    boolean isOracle(Connection c) {
        c.metaData.databaseProductName.toLowerCase().contains("oracle")
    }

    boolean isPostgres(Connection c) {
        c.metaData.databaseProductName.toLowerCase().contains("postgres")
    }

}

