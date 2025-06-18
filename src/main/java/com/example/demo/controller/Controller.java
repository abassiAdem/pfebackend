package com.example.demo.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import lombok.RequiredArgsConstructor;

import com.example.demo.dto.DepartementDTO;
import com.example.demo.dto.UserDto;
import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.dto.UserRoleDTO;
import com.example.demo.entities.AppUser;
import com.example.demo.entities.Chef;
import com.example.demo.entities.Departement;
import com.example.demo.entities.Realisateur;
import com.example.demo.entities.Type;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UserAlreadyExistsException;
import com.example.demo.repository.AgentRepository;
import com.example.demo.repository.ChefRepository;
import com.example.demo.service.KeycloakService;
import com.example.demo.service.UserRegistrationService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.PUT;

@RestController
@RequestMapping("/api/admin")

public class Controller {

	@Autowired
	private final KeycloakService kc;
	private final UserRegistrationService userservice;
	private final AgentRepository agentRepository;
	private final ChefRepository chefRepository;
	public  Controller( KeycloakService kc,UserRegistrationService userRegistrationService,AgentRepository agentRepository,ChefRepository chefRepository) {
		this.kc=kc;
		this.userservice=userRegistrationService;
		this.agentRepository=agentRepository;
		this.chefRepository=chefRepository;
	}
	
	
    
    @GetMapping("/{chefId}/users")
    @Transactional
    public ResponseEntity<?> getUsersForChef(@PathVariable Long chefId) {
        try {

            Chef chef = chefRepository.findById(chefId)
                    .orElseThrow(() -> new ResourceNotFoundException("Chef not found with id: " + chefId));

            List<UserRoleDTO> usersForChef = kc.getUsersForChef(chefId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Users retrieved successfully for chef with id: " + chefId);
            response.put("data", usersForChef);
            response.put("count", usersForChef.size());
            
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error retrieving users for chef");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    
	
	
	
    @GetMapping("/realisateurs")
    public ResponseEntity<?> getAllRealisateurs() {
    	List<Realisateur> responsables = kc.getAllRealisateur();
        
        if (responsables.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(Map.of("message", "No Realisateurs found"));
        }
        
        return ResponseEntity.ok(responsables);
    }
	
	
	
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserRoleDTO> usersWithRoles = kc.getAllUsersWithRoles();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Users retrieved successfully");
            response.put("data", usersWithRoles);
            response.put("count", usersWithRoles.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {

            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error retrieving users");
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    

    
	 @PostMapping("/sync")
	  public ResponseEntity<?> syncUser(@AuthenticationPrincipal Jwt jwt) {
	        try {
	        	kc.syncUserFromKeycloak(jwt);
	            return ResponseEntity.ok().body(Map.of(
	                "message", "User synchronized successfully",
	                "status", "success"
	            ));
	        } catch (IllegalArgumentException e) {
	            return ResponseEntity.badRequest().body(Map.of(
	                "message", e.getMessage(),
	                "status", "error"
	            ));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
	                "message", "Error synchronizing user: " + e.getMessage(),
	                "status", "error"
	            ));
	        }
	    }
	    @GetMapping("/current")
	    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
	        try {
	        	AppUser agent = kc.getCurrentAgent(jwt);
	        	UserDto us=convertToDTO(agent);
	            return ResponseEntity.ok(us);
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
	                "message", "Error fetching current user: " + e.getMessage(),
	                "status", "error"
	            ));
	        }
	    }



    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "message", "An unexpected error occurred: " + e.getMessage(),
            "status", "error"
        ));
    }
    private UserDto convertToDTO(AppUser agent) {
        UserDto dto = new UserDto();
        dto.setId(agent.getId());
        dto.setKeycloakId(agent.getKeycloakId());
        dto.setEmail(agent.getEmail());
        dto.setFirstName(agent.getFirstName());
        dto.setLastName(agent.getLastName());
        


        return dto;
    }
    @GetMapping("/chefs")
    public ResponseEntity<?> getAllChefs() {
        List<AppUser> chefs = kc.getAllChefs();
        
        if (chefs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(Map.of("message", "No chefs found"));
        }
        
        return ResponseEntity.ok(chefs);
    }

    @GetMapping("/responsables")
    public ResponseEntity<?> getAllResponsables() {
        List<AppUser> responsables = kc.getAllResponsables();
        
        if (responsables.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(Map.of("message", "No responsables found"));
        }
        
        return ResponseEntity.ok(responsables);
    }

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable String id) {
	    try {
	    	userservice.deleteUser(id);
	        return ResponseEntity.ok(Collections.singletonMap("message", "User deleted successfully."));
	    } catch (EntityNotFoundException e) {
	        System.err.println("User not found error: " + e.getMessage());
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	            .body(Collections.singletonMap("error", "User not found: " + e.getMessage()));
	    } catch (RuntimeException e) {
	        System.err.println("Runtime error during user deletion: " + e.getMessage());
	        if (e.getCause() != null) {
	            System.err.println("Caused by: " + e.getCause().getMessage());
	        }
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(Collections.singletonMap("error", "Failed to delete user: " + e.getMessage()));
	    } catch (Exception e) {
	        System.err.println("Unexpected error during user deletion: " + e.getMessage());
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(Collections.singletonMap("error", "Unexpected error occurred while deleting user."));
	    }
	    }

    
@PostMapping("/register")
public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDto request) {
    try {
    	AppUser user = userservice.registerUser(
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getPassword(),
            request.getUserType(),
            request.getChefId(),
            request.getResponsableId(),
            request.getCompetence(),
            request.getMetier(),
            request.getDepartementId()
        );
    	UserRoleDTO dto = kc.mapToUserWithRoleDTO(user);
        return ResponseEntity.ok(dto);
    } catch (UserAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Collections.singletonMap("error", e.getMessage()));
    } catch (EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("error", e.getMessage()));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("error", e.getMessage()));
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Unexpected error occurred: " + e.getMessage()));
    }
}


	
	@PutMapping("/update")
	public ResponseEntity<?> UpdateUser(@RequestBody UserRegistrationDto request){
 
	    try {
	    	
	    	AppUser user = userservice.updateUser(request);
	        return ResponseEntity.ok(user);
	    } catch (UserAlreadyExistsException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT)
	                .body(Collections.singletonMap("error", e.getMessage()));
	    } catch (EntityNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Collections.singletonMap("error", e.getMessage()));
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body(Collections.singletonMap("error", e.getMessage()));
	    } catch (RuntimeException e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Collections.singletonMap("error", "Unexpected error occurred: " + e.getMessage()));
	    }
	}

	@GetMapping("/users/{id}/is-active")
	public ResponseEntity<?> checkUserActiveStatus(@PathVariable Long id) {
	    return agentRepository.findById(id)
	        .map(user -> ResponseEntity.ok(
	            Collections.singletonMap("isActive", (Object) user.getIsActive())
	        ))
	        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
	            .body(Collections.singletonMap("error", (Object) "User not found")));
	}
	
	@GetMapping
    public ResponseEntity<Map<String, Object>> getAll() {
        List<DepartementDTO> dtos = kc.getAllDepartements();
        return ResponseEntity.ok(Map.of("data", dtos));
    }
	
	@PostMapping("/departement")
    @Transactional
    public ResponseEntity<Departement> createDepartement(@RequestBody Departement departement) {
        Departement saved = userservice.createDepartement(departement);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
}