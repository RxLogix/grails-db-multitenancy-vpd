    package org.grails.plugin.db.multitenancy.vpd.connection

    import org.grails.plugin.db.multitenancy.vpd.TenantContext
    import org.grails.plugin.db.multitenancy.vpd.TenantTransactionGuard
    import groovy.transform.CompileStatic
    import groovy.util.logging.Slf4j
    import org.grails.plugin.db.multitenancy.vpd.config.VpdConfig
    import org.springframework.transaction.support.TransactionSynchronizationAdapter
    import org.springframework.transaction.support.TransactionSynchronizationManager

    import java.sql.Connection
    import java.sql.Statement
    import javax.sql.DataSource
    import org.springframework.jdbc.datasource.AbstractDataSource

    @CompileStatic
    @Slf4j
    class VpdDataSource extends AbstractDataSource {

        DataSource targetDataSource
        VpdConfig config

        VpdDataSource(DataSource targetDataSource,  VpdConfig config){
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

            Statement stmt = null
            try {
                stmt = conn.createStatement()
                conn.clientInfo.put('TENANT_ID', tenant)
                if(config.devMode){
                    log.warn("DevMode on so no actual VPD policy applied")
                } else {
                    stmt.execute("""
                    BEGIN
                        DBMS_SESSION.CLEAR_CONTEXT(
                            '${config.contextName}',
                            '${config.attributeName}'
                        );
    
                        DBMS_SESSION.SET_CONTEXT(
                            '${config.contextName}',
                            '${config.attributeName}',
                            '${tenant}'
                        );
                    END;
                """)
                }

            } finally {
                stmt?.close()
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

        void clearOracleContext(Connection conn) {
            Statement stmt = conn.createStatement()
            try {
                if (config.devMode) {
                    log.warn("DevMode on so no actual VPD policy clean applied")
                } else {
                    stmt.execute("""
                BEGIN
                    DBMS_SESSION.CLEAR_CONTEXT(
                        '${config.contextName}',
                        '${config.attributeName}'
                    );
                END;
            """)
                }
            } finally {
                stmt.close()
            }
        }
    }

