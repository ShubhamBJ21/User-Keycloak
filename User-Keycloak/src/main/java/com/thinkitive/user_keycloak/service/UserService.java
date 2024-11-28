package com.thinkitive.user_keycloak.service;

import com.thinkitive.user_keycloak.dto.UserDto;
import com.thinkitive.user_keycloak.entity.User;
import com.thinkitive.user_keycloak.repository.UserRepository;
import com.thinkitive.user_keycloak.security.KeycloakSecurityUtil;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.common.util.CollectionUtil;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakSecurityUtil keycloakSecurityUtil;

    @Value("${realm}")
    private String realm;

    @Autowired
    public UserService(UserRepository userRepository, KeycloakSecurityUtil keycloakSecurityUtil) {
        this.userRepository = userRepository;
        this.keycloakSecurityUtil = keycloakSecurityUtil;
    }

    //get list of users from realm
    public List<UserDto> getUsers() {
          Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();
          List<UserRepresentation> userRepresentations = keycloak.realm(realm).users().list();
       // System.out.println("At service layer");
          return mapUserDtos(userRepresentations);
      //  return Arrays.asList("nikhil","shubam");
    }

    //create user
    public ResponseEntity<UserDto> createUser(UserDto userDto, String role){
        Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();
        RoleRepresentation roleRepresentation = getRoleByName(keycloak, role);

        if (roleRepresentation == null) {
            System.out.println("Role " + role + " not found in realm.");
            return new ResponseEntity<>(userDto, HttpStatus.BAD_REQUEST);
        }

        // Check email already exists
        List<UserRepresentation> existingUsers = keycloak.realm(realm).users().search(null, null, null, userDto.getEmail(), 0, 1);
        if (!existingUsers.isEmpty()) {
            System.out.println("User with email " + userDto.getEmail() + " already exists.");
            return new ResponseEntity<>(userDto, HttpStatus.CONFLICT);
        }

        UserRepresentation userRepresentation = mapUserRepresentation(userDto);
        Response response = keycloak.realm(realm).users().create(userRepresentation);

        if (response.getStatus()==201){
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            userDto.setId(userId);

            assignRoleToUser(keycloak, userId, role);
            User user = mapUsedtoToUser(userDto, userId, role);

            userRepository.save(user);
            return new ResponseEntity<>(userDto, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(userDto, HttpStatus.BAD_REQUEST);
    }

    private List<UserDto> mapUserDtos(List<UserRepresentation> userRepresentations){
        List<UserDto> userDtos = new ArrayList<>();

        if(CollectionUtil.isNotEmpty(userRepresentations)){
            userRepresentations.forEach(u->{
                userDtos.add(mapUserDto(u));
            });
        }
        System.out.println("returning dtos");
        return userDtos;
    }

    private UserDto mapUserDto(UserRepresentation userRepresentation) {
        UserDto userDto = new UserDto();
        userDto.setId(userRepresentation.getId());
        userDto.setFirtName(userRepresentation.getFirstName());
        userDto.setLastName(userRepresentation.getLastName());
        userDto.setEmail(userRepresentation.getEmail());
        userDto.setUserName(userRepresentation.getUsername());
        return userDto;
    }

    private RoleRepresentation getRoleByName(Keycloak keycloak, String role){
        List<RoleRepresentation> roles = keycloak.realm(realm).roles().list();
        for (RoleRepresentation roleRepresentation : roles){
            if (roleRepresentation.getName().equals(role)){
                return roleRepresentation;
            }
        }
        return null;
    }

    private UserRepresentation mapUserRepresentation(UserDto userDto) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId(userDto.getId());
        userRepresentation.setFirstName(userDto.getFirtName());
        userRepresentation.setLastName(userDto.getLastName());
        userRepresentation.setEmail(userDto.getEmail());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);

        List<CredentialRepresentation> credentialRepresentations = new ArrayList<>();
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();

        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setValue(userDto.getPassword());
        credentialRepresentations.add(credentialRepresentation);

        userRepresentation.setCredentials(credentialRepresentations);
        return userRepresentation;
    }

    private void assignRoleToUser(Keycloak keycloak, String userId, String roleName) {
        RoleRepresentation role = getRoleByName(keycloak, roleName);
        if (role != null) {
            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(List.of(role));
        } else {
            System.out.println("Role " + roleName + " not found in realm.");
            throw new RuntimeException("Role not found");
        }
    }

    private User mapUsedtoToUser(UserDto userDto, String userId, String role) {
        User user = new User();

        user.setId(Integer.valueOf(userDto.getId()));
        user.setUserName(userDto.getUserName());
        user.setFirstName(userDto.getFirtName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setRole(role);

        return user;
    }
}
