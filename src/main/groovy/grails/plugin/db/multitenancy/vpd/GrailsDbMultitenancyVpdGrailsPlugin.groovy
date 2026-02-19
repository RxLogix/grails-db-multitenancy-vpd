package grails.plugin.db.multitenancy.vpd

import grails.plugins.*
import org.grails.plugin.db.multitenancy.vpd.DataSourceWrapperPostProcessor
import org.grails.plugin.db.multitenancy.vpd.resolvers.ApiTenantResolver
import org.grails.plugin.db.multitenancy.vpd.resolvers.SessionTenantResolver
import org.grails.plugin.db.multitenancy.vpd.resolvers.TenantResolverChain
import org.grails.plugin.db.multitenancy.vpd.config.VpdConfig

class GrailsDbMultitenancyVpdGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "6.2.3  > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Grails -db -multitenancy -vpd" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
   Brief summary/description of the plugin.
   '''
    // URL to the plugin's documentation
    def documentation = "https://grails.github.io/GrailsOracleVpd/"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    //    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    //    def organization = [ name: "My Company", url: "https://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
    //    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
    //    def issueManagement = [ system: "GitHub", url: "https://github.com/grails/GrailsOracleVpd/issues" ]

    // Online location of the plugin's browseable source code.
    //    def scm = [ url: "https://github.com/grails/GrailsOracleVpd" ]

    Closure doWithSpring() {
        { ->
            vpdConfig(VpdConfig) {
                enabled = grailsApplication.config.getProperty("vpd.enabled", Boolean, true)
                contextName = grailsApplication.config.getProperty("vpd.contextName", String, "VPD_CTX")
                attributeName = grailsApplication.config.getProperty("vpd.attributeName", String, "TENANT_ID")
                headerName = grailsApplication.config.getProperty("vpd.headerName", String, "X-Tenant-ID")
                jwtClaim = grailsApplication.config.getProperty("vpd.jwtClaim", String, "tenant")
                sessionAttribute = grailsApplication.config.getProperty("vpd.sessionAttribute", String, "TENANT_ID")
                failIfMissingTenant = grailsApplication.config.getProperty("vpd.failIfMissingTenant", Boolean, true)
                systemTenantId = grailsApplication.config.getProperty("vpd.systemTenantId", String, '999')
                devMode = grailsApplication.config.getProperty("vpd.devMode", Boolean, false)
                supportedDBs = grailsApplication.config.getProperty("vpd.supportedDBs", List<String>, ['dataSource', 'dataSource_pva'])
                excludeUris = grailsApplication.config.getProperty("vpd.excludeUris", List<String>, ['/login/**'])
            }

            apiTenantResolver(ApiTenantResolver) {
                config = ref('vpdConfig')
            }

            sessionTenantResolver(SessionTenantResolver) {
                config = ref('vpdConfig')
            }

            tenantResolverChain(TenantResolverChain) {
                resolvers = [
                        ref('apiTenantResolver'),
                        ref('sessionTenantResolver')
                ]
            }

            dataSourceWrapperPostProcessor(DataSourceWrapperPostProcessor){
                config = ref('vpdConfig')
            }
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}