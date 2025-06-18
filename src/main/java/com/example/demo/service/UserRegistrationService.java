package com.example.demo.service;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import com.example.demo.dto.UserActiveStatusDTO;
import com.example.demo.dto.UserActivityDTO;
import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.dto.UserUpdateDto;
import com.example.demo.entities.Admin;
import com.example.demo.entities.AppUser;
import com.example.demo.entities.Chef;
import com.example.demo.entities.Demande;
import com.example.demo.entities.Departement;
import com.example.demo.entities.Employee;
import com.example.demo.entities.Realisateur;
import com.example.demo.entities.ResponsableFonctionnel;
import com.example.demo.exception.InvalidUserTypeException;
import com.example.demo.exception.KeycloakException;
import com.example.demo.exception.UserAlreadyExistsException;
import com.example.demo.repository.AdminRepository;
import com.example.demo.repository.AgentRepository;
import com.example.demo.repository.ChefRepository;
import com.example.demo.repository.DemandRepository;
import com.example.demo.repository.DepartementRepository;
import com.example.demo.repository.EmpolyeRepository;
import com.example.demo.repository.RealisateurRepository;
import com.example.demo.repository.ResponsableRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class UserRegistrationService {
    private final AgentRepository appUserRepository;
    private final ResponsableRepository rep;
    private final ChefRepository chefrep;
    private final AdminRepository adminRepository;
    private final Keycloak keycloakClient;
    private final EmpolyeRepository emprep;
    private final RealisateurRepository realisateurRepository;
    private final DepartementRepository departementRepository;
    private final DemandRepository demandeRepository;
    @PersistenceContext
    private EntityManager entityManager;
    public UserRegistrationService(
            AgentRepository appUserRepository,
            ResponsableRepository rep,
            ChefRepository chefrep,
            Keycloak keycloakClient,EmpolyeRepository emprep,AdminRepository adminRepository,RealisateurRepository realisateurRepository,DepartementRepository departementRepository,DemandRepository demandeRepository) {
        this.appUserRepository = appUserRepository;
        this.rep = rep;
        this.chefrep = chefrep;
        this.keycloakClient = keycloakClient;
        this.emprep=emprep;
        this.adminRepository=adminRepository;
        this.realisateurRepository=realisateurRepository;
        this.departementRepository=departementRepository;
        this.demandeRepository=demandeRepository;
    }

		@Value("${keycloak.auth-server-url}")
		private String authServerUrl;
		
		@Value("${keycloak.realm}")
		private String realm;
		
		@Value("${keycloak.client-id}")
		private String clientId;

    @Transactional
    public AppUser registerUser(String firstName, String lastName, String email, String password, String userType, String chefId, String responsableId, String competence,String metier,String departementId) {

        Optional<AppUser> existingUser = appUserRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("A user with this email already exists");
        }
        Departement dept = departementRepository.findById(Long.parseLong(departementId))
                .orElseThrow(() -> new EntityNotFoundException(
                    "Departement not found with ID: " + departementId
                ));
        System.out.print("departement is nigga : "+ dept.toString());
        
        String keycloakId;
        try {
            keycloakId = createUserInKeycloak(firstName, lastName, email, password, userType);
        } catch (Exception e) {
            throw new KeycloakException("Failed to create user in Keycloak: " + e.getMessage());
        }
        AppUser user = new AppUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setDepartement(dept);
        user.setKeycloakId(keycloakId);

        AppUser savedUser = appUserRepository.save(user);

        assignUserRole(savedUser, userType, chefId, responsableId, competence,firstName, lastName,  email ,metier);

        return savedUser;
    }
    private String createUserInKeycloak(String firstName, String lastName, String email, String password, String userType) {
	    RealmResource realmResource = keycloakClient.realm(realm);
	    UsersResource usersResource = realmResource.users();
	
	    List<UserRepresentation> existingUsers = usersResource.search(email);
	    
	    if (!existingUsers.isEmpty()) {
	        String userId = existingUsers.get(0).getId();
	        System.out.println("Existing user found with ID: " + userId);
	        assignKeycloakRoles(userId, userType);
	        return userId;
	    }
	
	    UserRepresentation user = new UserRepresentation();
	    user.setEnabled(true);
	    user.setUsername(email);
	    user.setEmail(email);
	    user.setFirstName(firstName);
	    user.setLastName(lastName);
	
	    Map<String, List<String>> attributes = new HashMap<>();
	    attributes.put("origin", Collections.singletonList("app-registration"));
	    attributes.put("userType", Collections.singletonList(userType));
	    user.setAttributes(attributes);
	
	    Response response = usersResource.create(user);
	    System.out.println("User creation response status: " + response.getStatus());
	
	    if (response.getStatus() != 201) {
	        List<UserRepresentation> searchResults = usersResource.search(email);
	        System.out.println("Search results after failed creation: " + searchResults.size());
	        
	        if (!searchResults.isEmpty()) {
	            String userId = searchResults.get(0).getId();
	            System.out.println("Found user despite creation failure, ID: " + userId);
	            assignKeycloakRoles(userId, userType);
	            return userId;
	        }
	        throw new RuntimeException("Failed to create user in Keycloak. Response: " + response.getStatus());
	    }
	
	    String userId = extractCreatedId(response);
	    System.out.println("Successfully created user with ID: " + userId);
	
	    CredentialRepresentation credential = new CredentialRepresentation();
	    credential.setType(CredentialRepresentation.PASSWORD);
	    credential.setValue(password);
	    credential.setTemporary(false);
	    usersResource.get(userId).resetPassword(credential); 
	
	    assignKeycloakRoles(userId, userType);
	    
	    return userId;
	}	
			
			
		private String extractCreatedId(Response response) {
        String locationHeader = response.getHeaderString("Location");
        if (locationHeader != null) {
            String[] parts = locationHeader.split("/");
            return parts[parts.length - 1];
        }
        throw new RuntimeException("Could not extract user ID from Keycloak response");
    }
		
private void assignUserRole(AppUser user, String userType, String chefId, String responsableId, String competence,String firstName, String lastName, String email,String metier) {
    if (userType == null || userType.trim().isEmpty()) {
        throw new IllegalArgumentException("Role cannot be null or empty");
    }
  
    userType = userType.trim().toLowerCase();

    switch (userType) {
	    case"superuser":
	    	user.setRole("superuser");
	    	break;
        case "responsable":
            ResponsableFonctionnel responsable = new ResponsableFonctionnel();
            responsable.setAgent(user);
            user.setResponsable(responsable);
            user.setRole("responsable");
            break;

        case "chef":
            Chef chef = new Chef();
            chef.setAgent(user);
            user.setChef(chef);
            user.setRole("chef");
            break;

        case "admin":
            Admin admin = new Admin();
            admin.setAgent(user);
            user.setAdmin(admin);
            user.setRole("admin");
            break;

        case "employe":
            if (chefId == null || chefId.trim().isEmpty()) {
                throw new InvalidUserTypeException("Chef is required for demandeur.");
            }
            Chef assignedChef = chefrep.findById(Long.parseLong(chefId))
                    .orElseThrow(() -> new EntityNotFoundException("Chef not found with ID: " + chefId));
            Employee em=new Employee();
            em.setAgent(user);
            em.setChef(assignedChef); 

            user.setEmployee(em);
            user.setChef(assignedChef);
            user.setRole("employe");
            break;

        case "realisateur":
            if (responsableId == null || responsableId.trim().isEmpty()) {
                throw new  InvalidUserTypeException("Responsable ID is required for realisateur.");
            }

            ResponsableFonctionnel assignedResponsable = rep.findById(Long.parseLong(responsableId))
                    .orElseThrow(() -> new EntityNotFoundException("Responsable not found with ID: " + responsableId));

            Realisateur realisateur = new Realisateur();
            realisateur.setAgent(user);
            realisateur.setEmail(email);
            realisateur.setFirstName(firstName);
            realisateur.setLastName(lastName);
            realisateur.setCompetences(competence);
            realisateur.setMetier(metier);
            realisateur.setDisponibilites("disponible"); 
            realisateur.setResponsable(assignedResponsable);
            user.setRealisateur(realisateur);
            user.setRole("realisateur");
            break;
            
        default:
            throw new InvalidUserTypeException("Invalid Role: " + userType);
    }

    appUserRepository.save(user);
}
    	private void assignKeycloakRoles(String userId, String userType) {
    try {
        RealmResource realmResource = keycloakClient.realm(realm);
        UserResource userResource = realmResource.users().get(userId);
        String normalizedUserType = userType.toLowerCase().trim();
 
        try {
            assignClientRole(realmResource, userResource, normalizedUserType);
            System.out.println("Successfully assigned client role: " + normalizedUserType);
            return;
        } catch (Exception e) {
            System.out.println("Failed to assign client role, trying realm role: " + e.getMessage());
        }
 
        try {
            assignRealmRole(realmResource, userResource, normalizedUserType);
            System.out.println("Successfully assigned realm role: " + normalizedUserType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign both client and realm roles for: " + normalizedUserType, e);
        }
    } catch (Exception e) {
        throw new RuntimeException("Error assigning role: " + e.getMessage(), e);
    }
}

    	private void assignClientRole(RealmResource realmResource, UserResource userResource, String roleName) {
    	    ClientsResource clientsResource = realmResource.clients();
    	    List<ClientRepresentation> clients = clientsResource.findByClientId(this.clientId);  
    	    
    	    if (clients.isEmpty()) {
    	        throw new RuntimeException("pfe-rest-api client not found");
    	    }

    	    String keycloakClientId = clients.get(0).getId();  
    	    RoleScopeResource clientRoles = userResource.roles().clientLevel(keycloakClientId);

    	    List<RoleRepresentation> availableRoles = realmResource.clients().get(keycloakClientId).roles().list();
    	    System.out.println("Available client roles: " + availableRoles.stream()
    	        .map(RoleRepresentation::getName)
    	        .collect(Collectors.joining(", ")));

    	    RoleRepresentation targetRole = availableRoles.stream()
    	        .filter(r -> r.getName().equalsIgnoreCase(roleName))
    	        .findFirst()
    	        .orElseThrow(() -> new RuntimeException("Client role not found: " + roleName));

    	    clientRoles.add(Collections.singletonList(targetRole));
    	}
	
private void assignRealmRole(RealmResource realmResource, UserResource userResource, String roleName) {
	    RoleScopeResource realmRoles = userResource.roles().realmLevel();
	    List<RoleRepresentation> availableRoles = realmResource.roles().list();
	    
	    System.out.println("Available realm roles: " + availableRoles.stream()
	        .map(RoleRepresentation::getName)
	        .collect(Collectors.joining(", ")));
	
	    RoleRepresentation targetRole = availableRoles.stream()
	        .filter(r -> r.getName().equalsIgnoreCase(roleName))
	        .findFirst()
	        .orElseThrow(() -> new RuntimeException("Realm role not found: " + roleName));
	
	    realmRoles.add(Collections.singletonList(targetRole));
	}
@Transactional
	private void cleanUpExistingRelationships(AppUser user) {
	   
	    if (user == null || user.getId() == null) {
	        throw new IllegalArgumentException("User cannot be null or have null ID");
	    }
 
	    user = appUserRepository.findById(user.getId())
	            .orElseThrow(() -> new EntityNotFoundException("User not found during cleanup"));
 
	    if (user.getAdmin() != null) {
	        Admin admin = user.getAdmin();
	        admin.setAgent(null);  
	        user.setAdmin(null);
	        adminRepository.delete(admin);
	    }

	    if (user.getChef() != null) {
	        Chef chef = user.getChef();
	        new ArrayList<>(chef.getEmployees()).forEach(emp -> {
	            emp.setChef(null);
	            emprep.save(emp);  
	        });
	        chef.setAgent(null);
	        user.setChef(null);
	        chefrep.delete(chef);
	    }

	    if (user.getEmployee() != null) {
	        Employee employee = user.getEmployee();
	        if (employee.getChef() != null) {
	            employee.getChef().getEmployees().remove(employee);
	            chefrep.save(employee.getChef());
	        }
	        employee.setAgent(null);
	        user.setEmployee(null);
	        emprep.delete(employee);
	    }

	    if (user.getRealisateur() != null) {
	        Realisateur oldRealisateur = user.getRealisateur();
	        List<Demande> demandes = demandeRepository.findDemandesByRealisateur(oldRealisateur);
	        if(!demandes.isEmpty()) {
		        demandes.forEach(demande -> {
		            demande.setRealisateur(null); 
		            demandeRepository.save(demande);
		        });
	        }
 
	        if (oldRealisateur.getResponsable() != null) {
	            oldRealisateur.getResponsable().getRealisateurs().remove(oldRealisateur);
	            rep.save(oldRealisateur.getResponsable());
	        }
	         
	        oldRealisateur.setAgent(null);
	        user.setRealisateur(null);
	        realisateurRepository.delete(oldRealisateur);
	    }

	    if (user.getResponsable() != null) {
	        ResponsableFonctionnel responsable = user.getResponsable();
	        
	        new ArrayList<>(responsable.getRealisateurs()).forEach(real -> {
	            real.setResponsable(null);
	            realisateurRepository.save(real);  
	        });
	        responsable.setAgent(null);
	        user.setResponsable(null);
	        rep.delete(responsable);
	    }
	    user.setRole(null);
	    appUserRepository.saveAndFlush(user);

	    if (!appUserRepository.existsById(user.getId())) {
	        throw new IllegalStateException("User was unexpectedly deleted during cleanup");
	    }
	}
		
@Transactional
public AppUser updateUser(UserRegistrationDto updateDto) {

AppUser originalUser = appUserRepository.findById(updateDto.getId())
        .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + updateDto.getId()));

String originalEmail = originalUser.getEmail();

AppUser updatedUser = updateUserInDatabase(updateDto, originalUser);

try {

    updateUserInKeycloak(updatedUser, updateDto, originalEmail);
} catch (Exception e) {
    System.err.println("Keycloak update failed: " + e.getMessage());
    throw new RuntimeException("Database updated successfully but Keycloak update failed", e);
}

return updatedUser;
}


private void assignUserRoleUpdate(AppUser user, String userType, String chefId, String responsableId, String competence,String firstName, String lastName, String email,String metier) {
    if (userType == null || userType.trim().isEmpty()) {
        throw new IllegalArgumentException("Role cannot be null or empty");
    }
  
    userType = userType.trim().toLowerCase();

    switch (userType) {
	    case"superuser":
	    	user.setRole("superuser");
	    	break;
        case "responsable":
            ResponsableFonctionnel responsable = new ResponsableFonctionnel();
            responsable.setAgent(user);
            user.setResponsable(responsable);
            user.setRole("responsable");
            break;

        case "chef":
            Chef chef = new Chef();
            chef.setAgent(user);
            user.setChef(chef);
            user.setRole("chef");
            break;

        case "admin":
            Admin admin = new Admin();
            admin.setAgent(user);
            user.setAdmin(admin);
            user.setRole("admin");
            break;

        case "employe":
            if (chefId == null || chefId.trim().isEmpty()) {
                throw new InvalidUserTypeException("Chef is required for demandeur.");
            }
            Chef assignedChef = chefrep.findById(Long.parseLong(chefId))
                    .orElseThrow(() -> new EntityNotFoundException("Chef not found with ID: " + chefId));
            Employee em=new Employee();
            em.setAgent(user);
            em.setChef(assignedChef); 

            user.setEmployee(em);
            user.setChef(assignedChef);
            user.setRole("employe");
            break;

        case "realisateur":
            if (responsableId == null || responsableId.trim().isEmpty()) {
                throw new  InvalidUserTypeException("Responsable ID is required for realisateur.");
            }

            ResponsableFonctionnel assignedResponsable = rep.findById(Long.parseLong(responsableId))
                    .orElseThrow(() -> new EntityNotFoundException("Responsable not found with ID: " + responsableId));

            Realisateur realisateur = new Realisateur();
            realisateur.setAgent(user);
            realisateur.setEmail(email);
            realisateur.setFirstName(firstName);
            realisateur.setLastName(lastName);
            realisateur.setCompetences(competence);
            realisateur.setMetier(metier);
            realisateur.setDisponibilites("disponible"); 
            realisateur.setResponsable(assignedResponsable);
            user.setRealisateur(realisateur);
            user.setRole("realisateur");
            List<Demande> demandes = demandeRepository.findDemandesByAgent(user);
            if(!demandes.isEmpty()) {        
            demandes.forEach(demande -> {
                demande.setRealisateur(realisateur);
                demandeRepository.save(demande);
            });}
 
            break;
            
        default:
            throw new InvalidUserTypeException("Invalid Role: " + userType);
    }

    appUserRepository.save(user);
}

@Transactional
	private AppUser updateUserInDatabase(UserRegistrationDto updateDto, AppUser user) {
	    user.setFirstName(updateDto.getFirstName());
	    user.setLastName(updateDto.getLastName());
	    user.setEmail(updateDto.getEmail());

	    String newUserType = updateDto.getUserType();
	    if (newUserType != null && !newUserType.isEmpty()) {
	        String role = user.getRole();
	        String resId = updateDto.getResponsableId();
	        String chefId = updateDto.getChefId();

	        if (newUserType.equals("employe")) {
	            Employee em = emprep.findById(updateDto.getId())
	                    .orElseThrow(() -> new RuntimeException("User has 'employe' role but no Employee record exists."));
	            chefId = em.getChef() != null ? em.getChef().getId().toString() : null;
	            
	        } else if (newUserType.equals("realisateur")) {
	            Realisateur rer = realisateurRepository.findById(updateDto.getId())
	                    .orElseThrow(() -> new RuntimeException("User has 'realisateur' role but no Realisateur record exists."));
	            resId = rer.getResponsable() != null ? rer.getResponsable().getId().toString() : null;
	            
	            if(updateDto.getCompetence().isEmpty() || updateDto.getCompetence()==null) {
                    updateDto.setCompetence(rer.getCompetences());
                }
                if(updateDto.getMetier().isEmpty() || updateDto.getMetier()==null) {
                    updateDto.setMetier(rer.getMetier());
                }
	        }
	        

	        updateDto.setChefId(chefId);
	        updateDto.setResponsableId(resId);
	        cleanUpExistingRelationships(user);

	        user = appUserRepository.findById(updateDto.getId())
	                .orElseThrow(() -> new EntityNotFoundException("User not found after cleanup"));
	        

	        
	        assignUserRoleUpdate(user, newUserType, chefId, resId, updateDto.getCompetence(),
	                updateDto.getFirstName(), updateDto.getLastName(),
	                updateDto.getEmail(), updateDto.getMetier());
	    } else {
	        updateRoleProperties(user, updateDto);
	    }
	    
	    return appUserRepository.save(user);
	}
private void updateUserInKeycloak(AppUser updatedUser, UserRegistrationDto updateDto, String originalEmail) {
 
  
    String newUserType = updateDto.getUserType();
    boolean hasNewUserType = newUserType != null && !newUserType.isEmpty();
    boolean hasNewPassword = updateDto.getPassword() != null && !updateDto.getPassword().isEmpty();
    
    boolean forceEmailUpdate = true;
                        
 
 
  
    boolean needsKeycloakUpdate = forceEmailUpdate || hasNewUserType || hasNewPassword;
    
    if (needsKeycloakUpdate) {
        if (updatedUser.getKeycloakId() == null || updatedUser.getKeycloakId().isEmpty()) {
            throw new IllegalArgumentException("L'ID Keycloak ne peut pas être nul ou vide");
        }

        try {
            
            RealmResource realmResource = keycloakClient.realm(realm);
            
            UserResource userResource;
            try {
                userResource = realmResource.users().get(updatedUser.getKeycloakId());
                 
                UserRepresentation currentKeycloakUser = userResource.toRepresentation();
                System.out.println("Found Keycloak user: " + currentKeycloakUser.getUsername() + 
                         " | " + currentKeycloakUser.getFirstName() + " " + currentKeycloakUser.getLastName());
                 
                if (forceEmailUpdate) {
                    System.out.println("Email update requested. Current in Keycloak: " + currentKeycloakUser.getEmail() + 
                                      ", New value: " + updatedUser.getEmail());
 
                    if (!updatedUser.getEmail().equals(currentKeycloakUser.getEmail())) {
             
                        List<UserRepresentation> existingUsers = realmResource.users()
                            .searchByEmail(updatedUser.getEmail(), true);
                        
                        boolean emailExists = existingUsers.stream()
                            .anyMatch(user -> !user.getId().equals(updatedUser.getKeycloakId()));
                        
                        if (emailExists) {
                            System.err.println("Email conflict detected! Email already exists for another user.");
                            throw new IllegalArgumentException("Email already exists in Keycloak for another user");
                        }
                        
                        System.out.println("Updating email in Keycloak...");
                        
                        try { 
                            currentKeycloakUser.setEmail(updatedUser.getEmail());
                            currentKeycloakUser.setUsername(updatedUser.getEmail());
                            currentKeycloakUser.setFirstName(updateDto.getFirstName());
                            currentKeycloakUser.setLastName(updateDto.getLastName());
                            currentKeycloakUser.setEmailVerified(false);
                            try { Thread.sleep(200); } catch (InterruptedException ie) { /* ignore */ }
                            
                            userResource.update(currentKeycloakUser);
                            System.out.println("Email updated successfully in Keycloak");
                        } catch (Exception e) {
                            System.err.println("Error updating email in Keycloak: " + e.getMessage());
                          
                            try {
                                System.out.println("Trying to update email and username separately...");
                                
                                UserRepresentation emailUpdate = new UserRepresentation();
                                emailUpdate.setId(currentKeycloakUser.getId());
                                emailUpdate.setEmail(updatedUser.getEmail());
                                emailUpdate.setEmailVerified(false);
                                emailUpdate.setEnabled(currentKeycloakUser.isEnabled());
                                emailUpdate.setFirstName(currentKeycloakUser.getFirstName());
                                emailUpdate.setLastName(currentKeycloakUser.getLastName());
                        
                                emailUpdate.setUsername(currentKeycloakUser.getUsername());
                                
                                userResource.update(emailUpdate);
                                System.out.println("Email updated separately");
                                
                                try { Thread.sleep(500); } catch (InterruptedException ie) { /* ignore */ }
                                
                                UserRepresentation usernameUpdate = new UserRepresentation();
                                usernameUpdate.setId(currentKeycloakUser.getId());
                                usernameUpdate.setUsername(updatedUser.getEmail());
                                usernameUpdate.setEmail(updatedUser.getEmail());
                                usernameUpdate.setEmailVerified(false);
                                usernameUpdate.setEnabled(currentKeycloakUser.isEnabled());
                                usernameUpdate.setFirstName(currentKeycloakUser.getFirstName());
                                usernameUpdate.setLastName(currentKeycloakUser.getLastName());
                                
                                userResource.update(usernameUpdate);
                                System.out.println("Username updated separately");
                                
                            } catch (Exception e2) {
                                System.err.println("Separate updates also failed: " + e2.getMessage());
                                throw new RuntimeException("Failed to update email in Keycloak: " + e2.getMessage(), e);
                            }
                        }
                    } else {
                        System.out.println("Email unchanged, skipping email update in Keycloak");
                    }
                }
                 
                if (hasNewPassword) {
                    System.out.println("Password update requested");
                    
                    CredentialRepresentation credential = new CredentialRepresentation();
                    credential.setType(CredentialRepresentation.PASSWORD);
                    credential.setValue(updateDto.getPassword());
                    credential.setTemporary(false);
                    
                    try {
                        userResource.resetPassword(credential);
                        System.out.println("Password updated successfully");
                    } catch (Exception e) {
                        System.err.println("Error during password update: " + e.getMessage());
                        
                        try {
                            System.out.println("Trying alternative password update method...");
                          
                            List<CredentialRepresentation> credentials = currentKeycloakUser.getCredentials();
                            if (credentials != null && !credentials.isEmpty()) {
                                userResource.removeCredential(credentials.get(0).getId());
                            }
                            userResource.resetPassword(credential);
                            System.out.println("Password updated successfully using alternative method");
                        } catch (Exception e2) {
                            System.err.println("Alternative password update also failed: " + e2.getMessage());
                            throw new RuntimeException("Failed to update password: " + e2.getMessage());
                        }
                    }
                }
                 
             // Replace your role assignment section with this code for client roles:

                if (hasNewUserType) {
                    String targetRole = newUserType.toLowerCase();
                    System.out.println("Target role based on userType: " + targetRole);
                     
                    if (!Arrays.asList("admin", "chef", "employe", "realisateur", "responsable").contains(targetRole)) {
                        System.err.println("Invalid user type: " + targetRole);
                        throw new IllegalArgumentException("Invalid user type: " + targetRole);
                    }
                    
                    try {
                        // Get the client (adjust client name based on your setup)
                        String clientName = "pfe-rest-api"; // This should match your client ID in Keycloak
                        List<ClientRepresentation> clients = realmResource.clients().findByClientId(clientName);
                        
                        if (clients.isEmpty()) {
                            throw new IllegalArgumentException("Client " + clientName + " not found in realm");
                        }
                        
                        ClientRepresentation client = clients.get(0);
                        ClientResource clientResource = realmResource.clients().get(client.getId());
                         
                        RoleMappingResource roleMapping = userResource.roles();
                        RoleScopeResource clientRoles = roleMapping.clientLevel(client.getId());
                        
                        // Get current client roles for this user
                        List<RoleRepresentation> currentClientRoles = clientRoles.listAll();
                        System.out.println("Current client roles: " + 
                            currentClientRoles.stream().map(RoleRepresentation::getName).collect(Collectors.joining(", ")));
                        
                        // Check if user already has the target role
                        boolean hasTargetRole = currentClientRoles.stream()
                            .anyMatch(role -> role.getName().equalsIgnoreCase(targetRole));
                        
                        // Remove existing roles (admin, chef, employe, realisateur, responsable) except the target role
                        List<RoleRepresentation> rolesToRemove = currentClientRoles.stream()
                            .filter(role -> Arrays.asList("admin", "chef", "employe", "realisateur", "responsable")
                                              .contains(role.getName().toLowerCase()) && 
                                           !role.getName().equalsIgnoreCase(targetRole))
                            .collect(Collectors.toList());
                        
                        if (!rolesToRemove.isEmpty()) {
                            System.out.println("Removing client roles: " + 
                                rolesToRemove.stream().map(RoleRepresentation::getName).collect(Collectors.joining(", ")));
                            try {
                                clientRoles.remove(rolesToRemove);
                                System.out.println("Client roles removed successfully");
                            } catch (Exception e) {
                                System.err.println("Failed to remove client roles: " + e.getMessage());
                                throw e;
                            }
                        }
                        
                        // Add target role if not already present
                        if (!hasTargetRole) {
                            System.out.println("Adding target client role: " + targetRole);
                            
                            // Check if the role exists in the client
                            List<RoleRepresentation> availableClientRoles = clientResource.roles().list();
                            boolean roleExists = availableClientRoles.stream()
                                .anyMatch(role -> role.getName().equalsIgnoreCase(targetRole));
                            
                            if (!roleExists) {
                                System.err.println("Target client role does not exist!");
                                System.out.println("Available client roles: " + 
                                    availableClientRoles.stream().map(RoleRepresentation::getName)
                                        .collect(Collectors.joining(", ")));
                                throw new IllegalArgumentException("Client role " + targetRole + " does not exist in client " + clientName);
                            }
                            
                            // Get the role and assign it
                            RoleRepresentation targetRoleRep = clientResource.roles().get(targetRole).toRepresentation();
                            clientRoles.add(Collections.singletonList(targetRoleRep));
                            System.out.println("Client role " + targetRole + " added successfully");
                        } else {
                            System.out.println("User already has target client role: " + targetRole);
                        }
                        
                    } catch (Exception e) {
                        System.err.println("Failed to update client roles: " + e.getMessage());
                        e.printStackTrace();
                        throw new RuntimeException("Failed to update client roles: " + e.getMessage(), e);
                    }
                }
                
            } catch (NotFoundException nfe) {
                System.err.println("User with ID " + updatedUser.getKeycloakId() + " not found in Keycloak");
                throw new EntityNotFoundException("User not found in Keycloak with ID: " + updatedUser.getKeycloakId());
            }
        } catch (Exception e) {
            System.err.println("!!! KEYCLOAK UPDATE FAILED !!!");
            System.err.println("Error at: " + e.getStackTrace()[0]);
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update user in Keycloak: " + e.getMessage(), e);
        }
    }
}

private void updateRoleProperties(AppUser user, UserRegistrationDto updateDto) {

	    if (user.getRealisateur() != null) {
	        Realisateur realisateur = user.getRealisateur();
	        
	        if (updateDto.getCompetence() != null) {
	            realisateur.setCompetences(updateDto.getCompetence());
	        }
	        
	        if (updateDto.getMetier() != null) {
	            realisateur.setMetier(updateDto.getMetier());
	        }
	        if (updateDto.getResponsableId() != null && !updateDto.getResponsableId().isEmpty()) {
	            ResponsableFonctionnel newResponsable = rep.findById(Long.parseLong(updateDto.getResponsableId()))
	                    .orElseThrow(() -> new EntityNotFoundException("Responsable not found"));
	            
	            if (realisateur.getResponsable() != null) {
	                realisateur.getResponsable().getRealisateurs().remove(realisateur);
	            }
	            
	            realisateur.setResponsable(newResponsable);
	            newResponsable.getRealisateurs().add(realisateur);
	        }
	    } 
	    else if (user.getEmployee() != null) {  
	 if (updateDto.getChefId() != null && !updateDto.getChefId().isEmpty()) {
	            Chef newChef = chefrep.findById(Long.parseLong(updateDto.getChefId()))
	                    .orElseThrow(() -> new EntityNotFoundException("Chef not found"));
	            
	            if (user.getEmployee().getChef() != null) {
	                user.getEmployee().getChef().getEmployees().remove(user.getEmployee());
	            }
	            
	            user.getEmployee().setChef(newChef);
	            newChef.getEmployees().add(user.getEmployee());
	        }
	    }
	}		
		private String mapRoleToKeycloak(String userType) {
			   
		    if ("NO_ROLE".equalsIgnoreCase(userType)) {
		        return "admin";
		    }
		    return userType.toLowerCase(); 
		}
		


public List<UserActivityDTO> getUserActivityForLast7Days() {
    LocalDateTime endDate = LocalDateTime.now();
    LocalDateTime startDate = endDate.minusDays(7); 
 
      List<Object[]> rawStats = appUserRepository.findUserLoginStats(startDate, endDate);
 
      List<UserActivityDTO> result = new ArrayList<>();
      for (Object[] row : rawStats) { 
          LocalDateTime date = ((java.sql.Date) row[0]).toLocalDate().atStartOfDay();
          Long count = (Long) row[1];
          result.add(new UserActivityDTO(date, count));
      }

      return result;
  }


 
	        public List<UserActiveStatusDTO> getActiveUserStatus() {
	            List<Object[]> results = appUserRepository.countUsersByActiveStatus();

	            long totalUsers = results.stream()
	                .mapToLong(row -> (Long) row[1])
	                .sum();

	            List<UserActiveStatusDTO> statusList = new ArrayList<>();
	            for (Object[] row : results) {
	                Boolean isActive = (Boolean) row[0];
	                Long count = (Long) row[1];

	                double percentage = (totalUsers > 0) ? (count * 100.0 / totalUsers) : 0.0;

	                String status = Boolean.TRUE.equals(isActive) ? "Active" : "Inactive";

	                statusList.add(new UserActiveStatusDTO(status, count, percentage));
	            }
	            return statusList;
	        }
			
		@Transactional
		public void deleteUser(String id) {
			    Long userId;
			    try {
			        userId = Long.parseLong(id);
			    } catch (NumberFormatException e) {
			        throw new IllegalArgumentException("Invalid user ID format: " + id);
			    }
			    
			    Optional<AppUser> userOpt = appUserRepository.findById(userId);
			    if (userOpt.isEmpty()) {
			        throw new EntityNotFoundException("User not found with ID: " + id);
			    }

			    AppUser user = userOpt.get();
			     

			    boolean keycloakDeleted = false;
			    if (user.getKeycloakId() != null && !user.getKeycloakId().trim().isEmpty()) {
			        try {
			            deleteUserFromKeycloak(user.getKeycloakId());
			            keycloakDeleted = true;
			        } catch (Exception e) {
			            System.err.println("Warning: Could not delete user from Keycloak: " + e.getMessage());

			        }
			    }
			    
			    try {

			        appUserRepository.delete(user);
			        
			        appUserRepository.flush();
			        
			        System.out.println("User with ID " + id + " deleted from database. Keycloak deletion: " + keycloakDeleted);
			    } catch (Exception e) {
			        throw new RuntimeException("Failed to delete user from database: " + e.getMessage(), e);
			    }
			}
	@Transactional
	private void deleteUserFromKeycloak(String keycloakId) {
	    if (keycloakId == null || keycloakId.trim().isEmpty()) {
	        throw new IllegalArgumentException("Invalid Keycloak ID for deletion.");
	    }
	    RealmResource realmResource = keycloakClient.realm(realm);
	    UsersResource usersResource = realmResource.users();
	    
	    try {
	        UserResource userResource = usersResource.get(keycloakId);
	      
	        try {
	            userResource.toRepresentation();   
	            userResource.remove();
	        } catch ( NotFoundException e) {
	            throw new RuntimeException("User with ID " + keycloakId + " not found in Keycloak");
	        }
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to delete user from Keycloak: " + e.getMessage());
	    }
	}
	public Departement createDepartement(Departement departement) {
        return departementRepository.save(departement);
    }
			
}
