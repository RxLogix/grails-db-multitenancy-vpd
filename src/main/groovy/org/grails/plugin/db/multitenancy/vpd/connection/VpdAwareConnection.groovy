package org.grails.plugin.db.multitenancy.vpd.connection

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.plugin.db.multitenancy.vpd.config.VpdConfig

import java.sql.Connection
import java.sql.Statement

@CompileStatic
@Slf4j
class VpdAwareConnection implements Connection {

    @Delegate(excludes = ['close'])
    private final Connection delegate

    private final VpdConfig config

    VpdAwareConnection(Connection delegate, VpdConfig config) {
        this.delegate = delegate
        this.config = config
    }

    @Override
    void close() {
        clearContext()
        delegate.close()
    }

    private void clearContext() {
        Statement stmt = null
        try {
            stmt = delegate.createStatement()
            if (config.devMode) {
                log.warn("DevMode on so no actual VPD policy applied on connection close")
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
            stmt?.close()
        }
    }
}


