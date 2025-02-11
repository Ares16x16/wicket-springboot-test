package com.tutorial.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
    
    // Main database property injections from application.properties
    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPassword;
    @Value("${spring.datasource.driver-class-name}")
    private String dbDriver;
    
    @Primary
    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
            .driverClassName(dbDriver)
            .url(dbUrl)
            .username(dbUser)
            .password(dbPassword)
            .build();
    }
    
    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.tutorial.entity");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(true);
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL8Dialect");
        
        em.setJpaVendorAdapter(vendorAdapter);
        return em;
    }
    
    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
    
    // auth database
    @Value("${spring.auth.datasource.jdbc-url}")
    private String authDbUrl;
    @Value("${spring.auth.datasource.username}")
    private String authDbUser;
    @Value("${spring.auth.datasource.password}")
    private String authDbPassword;
    @Value("${spring.auth.datasource.driver-class-name}")
    private String authDbDriver;
    
    @Bean(name = "authDataSource")
    public DataSource authDataSource() {
        return DataSourceBuilder.create()
            .driverClassName(authDbDriver)
            .url(authDbUrl)
            .username(authDbUser)
            .password(authDbPassword)
            .build();
    }
    
    @Bean(name = "authEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean authEntityManagerFactory(DataSource authDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(authDataSource);
        em.setPackagesToScan("com.tutorial.auth.entity");
        
        // Set a unique persistence unit name only
        em.setPersistenceUnitName("authPersistenceUnit");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(true);
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL8Dialect");
        
        em.setJpaVendorAdapter(vendorAdapter);
        return em;
    }
    
    @Bean(name = "authTransactionManager")
    public PlatformTransactionManager authTransactionManager(LocalContainerEntityManagerFactoryBean authEntityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(authEntityManagerFactory.getObject());
        return transactionManager;
    }
}
