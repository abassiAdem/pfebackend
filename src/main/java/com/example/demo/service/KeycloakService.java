package com.example.demo.service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.http.auth.InvalidCredentialsException;
import org.aspectj.weaver.loadtime.Agent;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.example.demo.entities.Realisateur;
import com.example.demo.entities.ResponsableFonctionnel;
import com.example.demo.dto.DepartementDTO;
import com.example.demo.dto.UserDto;
import com.example.demo.dto.UserRoleDTO;
import com.example.demo.entities.Admin;
import com.example.demo.entities.AppUser;
import com.example.demo.entities.Chef;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.AgentRepository;
import com.example.demo.repository.ChefRepository;
import com.example.demo.repository.DepartementRepository;
import com.example.demo.repository.RealisateurRepository;
import com.example.demo.repository.ResponsableRepository;

import jakarta.ws.rs.NotFoundException;

import com.example.demo.entities.AppUser;

import com.example.demo.repository.AgentRepository;



import lombok.RequiredArgsConstructor;

@Service
public class KeycloakService {
    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;



    @Value("${keycloak.credentials.secret}")
    private String clientSecret;
   @Autowired
   private final Keycloak keycloakClient;
    private final AgentRepository agentRepository;
    private final ChefRepository chefRepository;
    private final AdminRepository adminRepository;
    private final ResponsableRepository responsableRepository;
    private final RealisateurRepository realisateurRepository;
    private final RestTemplate restTemplate;
    private final DepartementRepository departementRepository;
    @Autowired
    public KeycloakService
    (Keycloak keycloakClient,AgentRepository agentRepository,ChefRepository chefRepository,DepartementRepository departementRepository
     ,AdminRepository adminRepository,ResponsableRepository responsableRepository,RealisateurRepository realisateurRepository,RestTemplateBuilder restTemplateBuilder) {
       
    	this.keycloakClient = keycloakClient;
        this.agentRepository = agentRepository;
        this.chefRepository =chefRepository;
        this.adminRepository=adminRepository;
        this.responsableRepository=responsableRepository;
        this.realisateurRepository=realisateurRepository;
        this.restTemplate = restTemplateBuilder.build();
        this.departementRepository=departementRepository;
    }
    
    @Value("${keycloak.resource-id}")
    private String clientId;
    
    public UserRepresentation getUserById(String userId) {
        try {
            UsersResource usersResource = keycloakClient.realm(realm).users();
            return usersResource.get(userId).toRepresentation();
        } catch (NotFoundException e) {
            throw new RuntimeException("User not found with ID: " + userId, e);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user from Keycloak: " + e.getMessage(), e);
        }
    }
    
    


    private void updateRoleEntities(List<String> realmRoles, List<String> clientRoles, AppUser agent) {

        adminRepository.findByAgent(agent).ifPresent(adminRepository::delete);
        chefRepository.findByAgent(agent).ifPresent(chefRepository::delete);
        responsableRepository.findByAgent(agent).ifPresent(responsableRepository::delete);
        realisateurRepository.findByAgent(agent).ifPresent(realisateurRepository::delete);

        if (realmRoles != null && realmRoles.contains("admin")) {
            Admin admin = new Admin();
            admin.setAgent(agent);
            adminRepository.save(admin);
        }

        if (clientRoles != null) {
            clientRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "chef":
                        Chef chef = new Chef();
                        chef.setAgent(agent);
                        chefRepository.save(chef);
                        break;
                    case "responsable":
                        ResponsableFonctionnel responsable = new ResponsableFonctionnel();
                        responsable.setAgent(agent);
                        responsableRepository.save(responsable);
                        break;
                    case "realisateur":
                        Realisateur realisateur = new Realisateur();
                        realisateur.setAgent(agent);
                        realisateurRepository.save(realisateur);
                        break;
                }
            });
        }
    }


    private List<String> extractClientRoles(Jwt jwt) {
        List<String> clientRoles = new ArrayList<>();
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        
        if (resourceAccess != null && resourceAccess.get(clientId) != null) {
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
            if (clientAccess != null && clientAccess.get("roles") != null) {
                clientRoles.addAll((List<String>) clientAccess.get("roles"));
            }
        }
        return clientRoles;
    }


    @Transactional(readOnly = true)
    public AppUser getCurrentAgent(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("Invalid token");
        }

        AppUser agent = agentRepository.findByKeycloakId(jwt.getSubject())
            .orElseThrow(() -> new RuntimeException("Agent not found"));

        return agent;
    }
    @Transactional
    public Map<String, Object> login(String username, String password) throws InvalidCredentialsException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("username", username);
        map.add("password", password);
        map.add("scope", "openid profile email");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

            try {
                Optional<AppUser> userOpt = agentRepository.findByEmail(username);
                if (userOpt.isPresent()) {
                    AppUser user = userOpt.get();
                    user.setLastLogin(LocalDateTime.now());
                    user.setIsActive(true);
                    agentRepository.save(user);
                } else {
                	 throw new InvalidCredentialsException("Invalid username or password");
                }
            } catch (Exception e) {
            	 throw new InvalidCredentialsException("Invalid username or password");
            }
            
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new InvalidCredentialsException("Invalid username or password");
            }
            throw new RuntimeException("Login failed: " + e.getMessage(), e);
        } catch (RestClientException e) {
            throw new RuntimeException("Server error: Unable to reach authentication server", e);
        }
    }
    
    public Map<String, Object> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);
        String userInfoUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/userinfo";
        
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> userInfo = response.getBody();


        String keycloakId = (String) userInfo.get("sub");

        Optional<AppUser> app = agentRepository.findByKeycloakId(keycloakId);
        if(app.isPresent()) {userInfo.put("id", app.get().getId());}
        

        return userInfo;
    }
        
    public Map<String, Object> refreshToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
                tokenUrl,
                request,
                Map.class
        );

        return response.getBody();
    }

    

    @Transactional
    public ResponseEntity<Map<String, Object>> logout(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", refreshToken);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        String logoutUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
        
        
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                logoutUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {}
            );

            // 2) Mark the user inactive in your database
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                agentRepository.findByEmail(auth.getName())
                    .ifPresent(user -> {
                        user.setIsActive(false);
                        agentRepository.save(user);
                    });
            }

            return response;
    }


    @Transactional
    public void syncUserFromKeycloak(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("Invalid token");
        }

        String keycloakId = jwt.getSubject();
        AppUser agent = agentRepository.findByKeycloakId(keycloakId)
            .orElse(new AppUser());

        agent.setKeycloakId(keycloakId);
        agent.setEmail(jwt.getClaimAsString("email"));
        agent.setFirstName(jwt.getClaimAsString("given_name"));
        agent.setLastName(jwt.getClaimAsString("family_name"));

        agent = agentRepository.save(agent);

        List<String> realmRoles = jwt.getClaimAsStringList("realm_access.roles");
        List<String> clientRoles = extractClientRoles(jwt);
        updateRoleEntities(realmRoles, clientRoles, agent);
    }

    private void createRoleEntities(List<String> realmRoles, List<String> clientRoles, AppUser agent) {
        if (realmRoles != null && realmRoles.contains("admin")) {
            Admin admin = new Admin();
            admin.setAgent(agent);
            adminRepository.save(admin);
        }

        if (clientRoles != null) {
            clientRoles.forEach(role -> {
                switch (role.toLowerCase()) {
                    case "chef":
                        Chef chef = new Chef();
                        chef.setAgent(agent);
                        chefRepository.save(chef);
                        break;
                    case "responsable":
                    	ResponsableFonctionnel responsable = new ResponsableFonctionnel();
                        responsable.setAgent(agent);
                        responsableRepository.save(responsable);
                        break;
                    case "realisateur":
                        Realisateur realisateur = new Realisateur();
                        realisateur.setAgent(agent);
                        realisateurRepository.save(realisateur);
                        break;
                }
            });
        }
    }
    

         public UserRoleDTO mapToUserWithRoleDTO(AppUser user) {
         	UserRoleDTO dto = new UserRoleDTO();
             dto.setId(user.getId());
             dto.setFirstName(user.getFirstName());
             dto.setLastName(user.getLastName());
             dto.setEmail(user.getEmail());
             dto.setKeycloakId(user.getKeycloakId());
             dto.setIsActive(user.getIsActive());
             dto.setRole(user.getRole());
             dto.setDepartement(user.getDepartement().getName());
             if (user.getRole().isEmpty()) {
                 dto.setRole("NO_ROLE");
             }
             if(user.getRole().equals("realisateur")) {
                 Optional<Realisateur> rerOptional = realisateurRepository.findById(user.getId());
                 if(rerOptional.isPresent()) {
                     Realisateur rer = rerOptional.get();
                     dto.setCompetence(rer.getCompetences());
                     dto.setMetier(rer.getMetier());
                 }
          }
             
             return dto;
         }

    

    @Transactional
    public void deleteUser(String keycloakId) {
        AppUser agent = agentRepository.findByKeycloakId(keycloakId)
            .orElseThrow(() -> new RuntimeException("User not found"));
 
        adminRepository.findByAgent(agent).ifPresent(adminRepository::delete);
        chefRepository.findByAgent(agent).ifPresent(chefRepository::delete);
        responsableRepository.findByAgent(agent).ifPresent(responsableRepository::delete);
        realisateurRepository.findByAgent(agent).ifPresent(realisateurRepository::delete);
 
        agentRepository.delete(agent);
    }


         

    
    @Transactional
    public List<UserRoleDTO> getAllUsersWithRoles() {
        List<AppUser> users = agentRepository.findAll();
        return users.stream()
                .map(this::mapToUserWithRoleDTO)
                .collect(Collectors.toList());
    }
    
    public void updateUserActiveStatus(Long userId, boolean isActive) {
        AppUser user = agentRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(isActive);
        agentRepository.save(user);
    }
    public List<AppUser> getAllResponsables() {
        return agentRepository.findByRole("responsable");
    }
    public List<Realisateur> getAllRealisateur(){
    	return realisateurRepository.findAll();
    }
    @Transactional
    public List<UserRoleDTO> getUsersForChef(Long chefId) {
        List<AppUser> users = agentRepository.findEmployeesByChefId(chefId);
        return users.stream()
                .map(this::mapToUserWithRoleDTO)
                .collect(Collectors.toList());
    }    public List<AppUser> getAllChefs() {
        return agentRepository.findAllChefs();
    }
 
    
    public List<DepartementDTO> getAllDepartements() {
        return departementRepository.findAll()
                .stream()
                .map(dept -> new DepartementDTO(dept.getId(), dept.getName()))
                .collect(Collectors.toList());
    }
    
}
