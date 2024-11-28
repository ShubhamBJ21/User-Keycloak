package com.thinkitive.user_keycloak.repository;

import com.thinkitive.user_keycloak.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}
