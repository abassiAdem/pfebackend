package com.example.demo.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import com.example.demo.dto.LoginRequest;

import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.exception.ErrorResponse;
import com.example.demo.repository.AgentRepository;
import com.example.demo.service.KeycloakService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")

public class AuthController {


    private final KeycloakService kc;
    private final AgentRepository agentRepository;
    public AuthController(KeycloakService kc,AgentRepository agentRepository) {
        this.kc = kc;
        this.agentRepository=agentRepository;
    }

@PostMapping("/logout")
public ResponseEntity<?> logout(
    @RequestParam String refreshToken, 
    @RequestHeader("Authorization") String authorization
) {
    Map<String, Object> response = new HashMap<>();
    
    if (authorization == null || !authorization.startsWith("Bearer ")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "Invalid token"));
    }

    String token = authorization.substring(7);
    
    try {
        Map<String, Object> userInfo = kc.getUserInfo(token);
        Long userId = (userInfo.get("id") instanceof Integer) 
            ? ((Integer) userInfo.get("id")).longValue() 
            : (Long) userInfo.get("id");
        
        CompletableFuture<Map<String, Object>> logoutFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return kc.logout(refreshToken).getBody();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
        agentRepository.findById(userId).ifPresent(user -> {
            user.setIsActive(false);
            agentRepository.save(user);
        });

        Map<String, Object> result = logoutFuture.get(4, TimeUnit.SECONDS);
        
        response.put("status", "success");
        response.put("message", "Successfully logged out");
        response.put("details", result);
        return ResponseEntity.ok(response);
        
    } catch (TimeoutException e) {
        response.put("status", "success");
        response.put("message", "Logout processed but timed out on server side");
        return ResponseEntity.ok(response);
    } catch (HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
            response.put("status", "success");
            response.put("message", "Token might be already expired or invalid. User logged out.");
        } else {
            response.put("status", "error");
            response.put("message", "Logout failed");
            response.put("error", e.getMessage());
            response.put("statusCode", e.getStatusCode().value());
        }
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        response.put("status", "error");
        response.put("message", "Unexpected error during logout");
        response.put("error", e.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }
}

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {

            Map<String, Object> token = kc.login(loginRequest.getUsername(), loginRequest.getPassword());
            String accessToken = (String) token.get("access_token");

            Map<String, Object> userInfo = kc.getUserInfo(accessToken);

            token.put("user", userInfo);

            return ResponseEntity.ok(token);
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("E-mail ou mot de passe invalide", List.of(e.getMessage())));
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Erreur serveur. Veuillez réessayer ultérieurement.", List.of(e.getMessage())));
        }
    }



    
    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(@RequestHeader(value = "Authorization", required = false) String authorization) {
        try {

            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "error", "Invalid Authorization header",
                        "details", "Header must start with 'Bearer '"
                    ));
            }
            String token = authorization.substring(7); 
            
            try {

                Map<String, Object> userInfo = kc.getUserInfo(token);

                if (userInfo == null || userInfo.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(Map.of("error", "No user information available"));
                }
                
                return ResponseEntity.ok(userInfo);
            } catch (RestClientException e) {
               
                System.err.println("Keycloak User Info Error: " + e.getMessage());
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "error", "User Info Retrieval Failed",
                        "details", e.getMessage()
                    ));
            }
        } catch (Exception e) {

            System.err.println("Unexpected Error in getUserInfo: " + e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Unexpected Server Error",
                    "details", e.getMessage()
                ));
        }
    }



@PostMapping("/refresh")
public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {

    Map<String, Object> tokenResponse = kc.refreshToken(
        refreshTokenRequest.getRefreshToken()
    );
    return ResponseEntity.ok(tokenResponse);
}






}