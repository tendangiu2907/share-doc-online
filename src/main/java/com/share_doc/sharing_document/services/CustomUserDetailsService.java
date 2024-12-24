package com.share_doc.sharing_document.services;

import com.share_doc.sharing_document.entity.User;
import com.share_doc.sharing_document.repository.UserRepository;
import com.share_doc.sharing_document.util.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
  private final UserRepository userRepository;

  @Autowired
  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository
            .findUserByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("Could not found user"));
    return new CustomUserDetails(user);
  }
}
