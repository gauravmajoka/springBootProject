package com.restful.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.restful.model.Account;
import com.restful.payload.auth.AccountDTO;
import com.restful.payload.auth.AccountViewDTO;
import com.restful.payload.auth.AuthorityDTO;
import com.restful.payload.auth.PasswordDTO;
import com.restful.payload.auth.TokenDTO;
import com.restful.payload.auth.UserLoginDTO;
import com.restful.service.AccountService;
import com.restful.service.TokenService;
import com.restful.util.constants.AccountError;
import com.restful.util.constants.AccountSucces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/auth")
@Tag(name = "Auth Controller", description = "Controller for Account management")
@Slf4j
public class AuthController {

   @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private TokenService tokenService;

    @Autowired
    private AccountService accountService;

     @PostMapping("/token")
     @ResponseStatus(HttpStatus.OK)
     public ResponseEntity<TokenDTO> token (@Valid @RequestBody UserLoginDTO userLogin) throws AuthenticationException{
        try {

            Authentication authentication = authenticationManager
            .authenticate(new UsernamePasswordAuthenticationToken(userLogin.getEmail(), userLogin.getPassword()));
               return ResponseEntity.ok(new TokenDTO(tokenService.generateToken(authentication)));
            
        } catch (Exception e) {
          log.debug(AccountError.TOKEN_GENERATION_ERROR.toString() + ": "+e.getMessage());
           return new ResponseEntity<>(new TokenDTO(null), HttpStatus.BAD_REQUEST);
        }
       

     } 
     
     @PostMapping(value = "/users/add", produces = "application/json")
     @ResponseStatus(HttpStatus.CREATED)
     @ApiResponse(responseCode = "400", description = "Please enter a valid email and Password length between 6 to 20 characters")
     @ApiResponse(responseCode = "200", description = "Account added")
     @Operation(summary = "Add a new User")
     public ResponseEntity<String> addUser(@Valid @RequestBody AccountDTO accountDTO){
        try {
            Account account = new Account();
            account.setEmail(accountDTO.getEmail());
            account.setPassword(accountDTO.getPassword());
            accountService.save(account);
            return ResponseEntity.ok(AccountSucces.ACCOUNT_ADDED.toString());
            
        } catch (Exception e) {
            log.debug(AccountError.ADD_ACCOUNT_ERROR.toString()+": "+e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
      }

        @GetMapping(value = "/users", produces = "application/json")
         @SecurityRequirement(name = "demo-api")
        @ApiResponse(responseCode = "200", description = "List of users")
         @ApiResponse(responseCode = "401", description = "Please check access token")
        @Operation(summary = "List user api")
        public List<AccountViewDTO> Users(){
            List<AccountViewDTO> accounts = new ArrayList<>();
            for(Account account : accountService.findAll()){
               accounts.add(new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorites()));
            }
            return accounts;
        }

         @GetMapping(value = "/profile", produces = "application/json")
         @SecurityRequirement(name = "demo-api")
        @ApiResponse(responseCode = "200", description = "List of users")
         @ApiResponse(responseCode = "401", description = "Please check access token")
        @Operation(summary = "Profile View")
        public AccountViewDTO profile(Authentication authentication){
         String email = authentication.getName();
         Optional<Account> optionalAOptional = accountService.findByEmail(email);
         if(optionalAOptional.isPresent()){
            Account account = optionalAOptional.get();
            AccountViewDTO accountDTO = new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorites());
            return accountDTO;
         }

         return null;
          
        }

         @PutMapping(value = "/update_password", produces = "application/json")
         @SecurityRequirement(name = "demo-api")
        @ApiResponse(responseCode = "200", description = "Update password")
         @ApiResponse(responseCode = "401", description = "Please check access token")
        @Operation(summary = "Update Password")
        public AccountViewDTO updatePassword(@Valid @RequestBody PasswordDTO passwordDTO,Authentication authentication){
         String email = authentication.getName();
         Optional<Account> optionalAOptional = accountService.findByEmail(email);
         if(optionalAOptional.isPresent()){
            Account account = optionalAOptional.get();
            account.setPassword(passwordDTO.getPassword());
            accountService.save(account);
            AccountViewDTO accountDTO = new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorites());
            return accountDTO;
         }

         return null;
          
        }

          @PutMapping(value = "/users/{user_id}/authority", produces = "application/json")
         @SecurityRequirement(name = "demo-api")
        @ApiResponse(responseCode = "200", description = "Update password")
         @ApiResponse(responseCode = "401", description = "Please check access token")
        @Operation(summary = "Update Auth")
        public ResponseEntity<AccountViewDTO> updateAuthority(@Valid @RequestBody AuthorityDTO authorityDTO, @PathVariable long user_id ){
         Optional<Account> optionalAccount = accountService.findByID(user_id);
         if(optionalAccount.isPresent()){
            Account account = optionalAccount.get();
            account.setAuthorites(authorityDTO.getAuthorities());
            accountService.save(account);
            AccountViewDTO accountViewDTO = new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorites());
            return ResponseEntity.ok(accountViewDTO);
         }
         return new ResponseEntity<AccountViewDTO>(new AccountViewDTO(), HttpStatus.BAD_REQUEST);
      }

        @DeleteMapping(value = "/profile/delete")
        @SecurityRequirement(name = "demo-api")
        @ApiResponse(responseCode = "200", description = "User deleted")
        @ApiResponse(responseCode = "401", description = "Please check access token")
        @Operation(summary = "Delete User")
        public ResponseEntity<String> delete_profile(Authentication authentication){
         String email = authentication.getName();
         Optional<Account> optionalAccount = accountService.findByEmail(email);
         if(optionalAccount.isPresent()){
            accountService.deleteById(optionalAccount.get().getId());
            return ResponseEntity.ok("User Deleted");
         }

         return new ResponseEntity<String>("Bad request", HttpStatus.BAD_REQUEST);

        }


     }



