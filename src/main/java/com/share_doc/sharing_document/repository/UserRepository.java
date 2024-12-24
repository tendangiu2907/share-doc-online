package com.share_doc.sharing_document.repository;

import com.share_doc.sharing_document.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByUsername(String username);

}
