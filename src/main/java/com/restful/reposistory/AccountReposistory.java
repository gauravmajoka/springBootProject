package com.restful.reposistory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.restful.model.Account;

public interface AccountReposistory extends JpaRepository<Account, Long>{

    Optional<Account> findByEmail(String email);
    
}
