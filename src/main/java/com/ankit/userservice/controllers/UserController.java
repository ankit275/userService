package com.ankit.userservice.controllers;

import com.ankit.userservice.Services.UserService;
import com.ankit.userservice.Services.UserServiceImpl;
import com.ankit.userservice.dtos.*;
import com.ankit.userservice.models.AppUser;
import com.ankit.userservice.models.Token;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserServiceImpl userServiceImpl;
    UserService userService;

    UserController(UserService userService, UserServiceImpl userServiceImpl) {
        this.userService = userService;
        this.userServiceImpl = userServiceImpl;
    }

    @PostMapping("/signup")
    public UserDto signup(@RequestBody SignUpRequestDto requestDto){
        AppUser user = userService.signup(requestDto.getName(), requestDto.getEmail(), requestDto.getPassword());
        return UserDto.from(user);
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto requestDto) {
        Token token = userService.login(requestDto.getEmail(), requestDto.getPassword());
        LoginResponseDto responseDto = new LoginResponseDto();
        responseDto.setToken(token.getTokenValue());
        return responseDto;
    }

    @GetMapping("/validate/{token}")
    public ResponseEntity<Boolean> validate(@PathVariable("token") String token) {
        AppUser user = userService.validateToken(token);
        ResponseEntity<Boolean> responseEntity;
        if(user == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }else{
            responseEntity = new ResponseEntity<>(true, HttpStatus.OK);
        }
        return responseEntity;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequestDto requestDto) {
        AppUser user = userService.logout(requestDto.getToken());
        ResponseEntity<Void> responseEntity;
        if(user == null) {
            responseEntity = new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }else{
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        }
        return responseEntity;
    }
}
