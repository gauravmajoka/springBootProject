package com.restful.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.restful.model.Account;
import com.restful.service.AccountService;
import com.restful.util.constants.Authority;

@Component
public class SeedData implements CommandLineRunner{
    @Autowired
    private AccountService accountService;


    @Override
    public void run(String... args) throws Exception {
       Account account01 = new Account();
       Account account02 = new Account();

       account01.setEmail("gaurav10@gmail.com");
       account01.setPassword("123456");
       account01.setAuthorites(Authority.USER.toString());
       accountService.save(account01);

        account02.setEmail("gaurav0@gmail.com");
       account02.setPassword("123456");
       account02.setAuthorites(Authority.ADMIN.toString() +" "+ Authority.USER.toString());
       accountService.save(account02);

    }
    
}
