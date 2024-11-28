package com.thinkitive.user_keycloak.controller;

import com.thinkitive.user_keycloak.dto.UserDto;
import com.thinkitive.user_keycloak.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getUsers(){
        System.out.println("At controller");
        return new ResponseEntity<>(userService.getUsers(), HttpStatus.OK);
    }

    @PostMapping("/admin/{role}")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto, @PathVariable String role) {
        return userService.createUser(userDto, role);
    }

}
