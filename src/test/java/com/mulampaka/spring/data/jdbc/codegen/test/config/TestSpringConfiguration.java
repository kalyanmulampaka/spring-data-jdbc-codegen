package com.mulampaka.spring.data.jdbc.codegen.test.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan (basePackages = "com.generated.code.repository")
@EnableTransactionManagement
@Import (TestDataSourceConfiguration.class)
public class TestSpringConfiguration
{
    

    final Logger logger = LoggerFactory.getLogger (TestSpringConfiguration.class);

    public TestSpringConfiguration ()
    {
    }
    
    @Bean
    public PropertySourcesPlaceholderConfigurer propertyPlaceHolderConfigurer ()
    {
        return new PropertySourcesPlaceholderConfigurer ();
    }
    

    


}
