package com.ankit.userservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class AppUser extends Base {
    private String name;
    private String email;
    private String password;

    @ManyToMany
    private List<Role> roles;
}
