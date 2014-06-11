package com.mulampaka.spring.data.jdbc.codegen.test.config;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import com.jolbox.bonecp.BoneCPDataSource;

@Configuration
@PropertySource ("classpath:database.properties")
public class TestDataSourceConfiguration
{
    
    @Value ("${jdbc.driverClassName}")
    private String driverClass;
    @Value ("${jdbc.url}")
    private String jdbcUrl;
    @Value ("${jdbc.username}")
    private String user;
    @Value ("${jdbc.password}")
    private String password;
    @Value ("${jdbc.initialPoolSize}")
    private int initialPoolSize;
    @Value ("${jdbc.minPoolSize}")
    private int minPoolSize;
    @Value ("${jdbc.maxPoolSize}")
    private int maxPoolSize;

    final Logger logger = LoggerFactory.getLogger (TestDataSourceConfiguration.class);

    public TestDataSourceConfiguration ()
    {
    }
    
    @Bean
    public DataSource dataSource ()
    {
        this.logger.debug (
                "Creating datasource using: DriverClass:{}, JDBC Url:{}, User:{}, Password:{}"
                , new Object[] { driverClass, this.jdbcUrl, this.user,
                        this.password });
        BoneCPDataSource dataSource = new
                BoneCPDataSource ();
        dataSource.setDriverClass (driverClass);
        dataSource.setJdbcUrl (jdbcUrl);
        dataSource.setUser (user);
        dataSource.setPassword (password);
        return dataSource;
    }
    
}
