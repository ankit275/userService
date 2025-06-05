package com.ankit.userservice.Services;

import com.ankit.userservice.models.AppUser;
import com.ankit.userservice.models.Token;
import com.ankit.userservice.repositories.TokenRepository;
import com.ankit.userservice.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final SecretKey secretKey;
    private final long EXPIRATION_TIME_IN_MS = 10 * 60 * 60 * 1000;

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    UserServiceImpl(UserRepository userRepository, TokenRepository tokenRepository, BCryptPasswordEncoder bCryptPasswordEncoder, SecretKey secretKey) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.secretKey = secretKey;
    }
    @Override
    public AppUser signup(String name, String email, String password) {

        if(userRepository.findByEmail(email).isPresent()) {
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

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME_IN_MS);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());

        String jsonString = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        Token token = new Token();
        token.setUser(user);
        token.setTokenValue(jsonString);


////        Alternate way to generate expiry date
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.MINUTE, 5);
//        Date expiryDate = calendar.getTime();

        token.setExpireAt(expiryDate);

        return token;
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

        Claims claims;
        try{
            claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(tokenValue)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("Expired JWT token " + e.getMessage());
            return null;
        } catch (io.jsonwebtoken.JwtException e) {
            System.out.println("Invalid jwt token" + e.getMessage());
            return null;
        }

        String email = claims.getSubject();
        if(email == null) {
            System.out.println("Email is null in JWT claims");
            return null;
        }

        Optional<AppUser> optionalUser = userRepository.findByEmail(email);

        if(optionalUser.isEmpty() || optionalUser.get().isDeleted()) {
            System.out.println("User with email in token does not exist: ");
            return null;
        }

        return optionalUser.get();
    }

    private AppUser validateNonJwtToken(String tokenValue) {
        Optional<Token> optionalToken = tokenRepository.findByTokenValueAndDeletedAndExpireAtGreaterThan(tokenValue, false, new Date());
        if(optionalToken.isEmpty()) {
            return null;
        }
        Token token = optionalToken.get();
        return token.getUser();
    }
}
