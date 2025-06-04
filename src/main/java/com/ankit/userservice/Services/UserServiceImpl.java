package com.ankit.userservice.Services;

import com.ankit.userservice.models.AppUser;
import com.ankit.userservice.models.Token;
import com.ankit.userservice.repositories.TokenRepository;
import com.ankit.userservice.repositories.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    TokenRepository tokenRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder;

    UserServiceImpl(UserRepository userRepository, TokenRepository tokenRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
    @Override
    public AppUser signup(String name, String email, String password) {

        if(userRepository.findByEmail(email).isPresent()) {
//            throw new RuntimeException("User with this email already exists");
            return null;
        }
        AppUser appUser = new AppUser();
        appUser.setName(name);
        appUser.setEmail(email);
        appUser.setPassword(bCryptPasswordEncoder.encode(password));

        return userRepository.save(appUser);
    }

    @Override
    public Token login(String email, String password) {
        Optional<AppUser> findUser = userRepository.findByEmail(email);
        if(findUser.isEmpty()){
            return null;
        }
        AppUser user = findUser.get();
        if(!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            return null; // Password does not match
        }
        Token token = new Token();
        token.setUser(user);
        token.setTokenValue(RandomStringUtils.randomAlphanumeric(128));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        Date expiryDate = calendar.getTime();
        token.setExpireAt(expiryDate);

        return tokenRepository.save(token);
    }

    @Override
    public AppUser logout(String tokenValue) {
        Optional<Token> optionalToken = tokenRepository.findByTokenValueAndDeletedAndExpireAtGreaterThan(tokenValue, false, new Date());
        if(optionalToken.isEmpty()) {
            return null;
        }
        Token token = optionalToken.get();
        token.setDeleted(true);
        tokenRepository.save(token);
        return token.getUser();
    }

    @Override
    public AppUser validateToken(String tokenValue) {
        Optional<Token> optionalToken = tokenRepository.findByTokenValueAndDeletedAndExpireAtGreaterThan(tokenValue, false, new Date());
        if(optionalToken.isEmpty()) {
            return null;
        }
        Token token = optionalToken.get();

        return token.getUser();
    }
}
