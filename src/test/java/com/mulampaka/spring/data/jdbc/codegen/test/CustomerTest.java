package com.mulampaka.spring.data.jdbc.codegen.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import com.mulampaka.spring.data.jdbc.codegen.test.config.SpringJdbcBaseTest;

public class CustomerTest extends SpringJdbcBaseTest
{
    final static Logger logger = LoggerFactory.getLogger (CustomerTest.class);
    
    /* 
     @Autowired
     private CustomerRepository customerRepository;
    */
    public CustomerTest ()
    {
    }
    
    @Test
    public void getCustomerById () throws Exception
    {
        //   Customer c = customerRepository.findOne (1L);
        //  logger.debug ("customer:{}", c);
    }
}
