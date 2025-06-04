package com.ankit.userservice.repositories;

import com.ankit.userservice.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenValueAndDeletedAndExpireAtGreaterThan(String token, boolean deleted, Date date);
}
