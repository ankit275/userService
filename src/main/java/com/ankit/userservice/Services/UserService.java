package com.ankit.userservice.Services;


import com.ankit.userservice.models.AppUser;
import com.ankit.userservice.models.Token;

public interface UserService {

    AppUser signup(String name, String email, String password);

    Token login(String email, String password);

    AppUser logout(String token);

    AppUser validateToken(String token);
}
