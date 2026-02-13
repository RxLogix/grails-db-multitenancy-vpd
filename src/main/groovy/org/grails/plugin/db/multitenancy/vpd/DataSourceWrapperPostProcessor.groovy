package org.grails.plugin.db.multitenancy.vpd

import groovy.transform.CompileStatic
import org.grails.plugin.db.multitenancy.vpd.config.VpdConfig
import org.grails.plugin.db.multitenancy.vpd.connection.VpdDataSource

import javax.sql.DataSource;
import org.springframework.beans.factory.config.BeanPostProcessor;

@CompileStatic
public class DataSourceWrapperPostProcessor implements BeanPostProcessor {

    VpdConfig config

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource && beanName in config.supportedDBs) {
            return new VpdDataSource((DataSource) bean, config);
        }
        return bean;
    }
}
