package com.share_doc.sharing_document.config;

import com.share_doc.sharing_document.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {
  private final CustomUserDetailsService customUserDetailService;
  private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
  private final String[] publicUrl = {
    "/",
    "/global-search/**",
    "/register",
    "/register/**",
    "/webjars/**",
    "/resources/**",
    "/assets/**",
    "/css/**",
    "/summernote/**",
    "/js/**",
    "/*.css",
    "/*.js",
    "/*.js.map",
    "/fonts**",
    "/favicon.ico",
    "/resources/**",
    "/error",
    "/upload-to-storage-account"
  };

  @Autowired
  public WebSecurityConfig(
      CustomUserDetailsService customUserDetailService,
      CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
    this.customUserDetailService = customUserDetailService;
    this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
  }

  @Bean
  protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authenticationProvider(authenticationProvider());

    http.authorizeHttpRequests(
        auth -> {
          auth.requestMatchers(publicUrl).permitAll(); // allow publicUrl
          auth.anyRequest().authenticated(); // secure others urls
        });

    http.formLogin(
            form ->
                form.loginPage("/login")
                    .permitAll()
                    .successHandler(customAuthenticationSuccessHandler))
        .logout(
            logout -> {
              logout.logoutUrl("/logout");
              logout.logoutSuccessUrl("/");
            })
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }

  /**
   * custom authentication provider, tell spring security know how to find users and authenticate
   * passwords
   *
   * @return void
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setPasswordEncoder(passwordEncoder());
    authenticationProvider.setUserDetailsService(
        customUserDetailService); // tell Spring Security how to retrieve users from DB
    return authenticationProvider;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
