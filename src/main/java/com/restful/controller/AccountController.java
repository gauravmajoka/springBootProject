package com.restful.controller;

import org.springframework.web.bind.annotation.RestController;


import org.springframework.web.bind.annotation.GetMapping;



@RestController
public class AccountController {
    
    @GetMapping("/")
    public String getAccount() {
       return "Hello WORLD";
    }

 
   
  
    
}
