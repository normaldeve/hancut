package com.system.batch.killbatchsystem.user.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.batch.killbatchsystem.user.security.application.provider.JwtProvider;
import com.system.batch.killbatchsystem.user.security.application.refresh.RefreshTokenService;
import com.system.batch.killbatchsystem.user.security.domain.SecurityMatchers;
import com.system.batch.killbatchsystem.user.security.filter.CustomAuthenticationProvider;
import com.system.batch.killbatchsystem.user.security.filter.JwtAuthenticationFilter;
import com.system.batch.killbatchsystem.user.security.filter.JwtLogoutHandler;
import com.system.batch.killbatchsystem.user.security.filter.LoginFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtProvider jwtProvider;
  private final RefreshTokenService refreshTokenService;
  private final ObjectMapper objectMapper;
  private final UserDetailsService userDetailsService;

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      LoginFilter loginFilter,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      JwtLogoutHandler jwtLogoutHandler,
      CustomAuthenticationProvider customAuthenticationProvider
  ) throws Exception {
    http
        .csrf(cs -> cs.ignoringRequestMatchers("/actuator/**"))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(HttpMethod.GET, SecurityMatchers.PROMETHEUS).permitAll()
            .requestMatchers(HttpMethod.GET, SecurityMatchers.ACTUATOR).permitAll()
            .requestMatchers(HttpMethod.GET, SecurityMatchers.SSE).permitAll()
            .requestMatchers(HttpMethod.POST, SecurityMatchers.VALIDATE_NICKNAME).permitAll()
            .requestMatchers(HttpMethod.POST, SecurityMatchers.EXISTS_NICKNAME).permitAll()
            .requestMatchers(HttpMethod.POST, SecurityMatchers.SIGN_IN).permitAll()
            .requestMatchers(HttpMethod.POST, SecurityMatchers.REFRESH).permitAll()
            .requestMatchers(HttpMethod.POST, SecurityMatchers.LOGIN).permitAll()
            .requestMatchers(HttpMethod.POST, SecurityMatchers.LOGOUT).permitAll()
            .requestMatchers(SecurityMatchers.SUMMARY).permitAll()
            .anyRequest().authenticated()
        )
//        .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
//          res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//          res.setContentType(MediaType.APPLICATION_JSON_VALUE);
//          res.getWriter().write("{\"error\":\"UNAUTHORIZED\"}");
//        }))
        .logout(logout -> logout
            .logoutRequestMatcher(new AntPathRequestMatcher(SecurityMatchers.LOGOUT, HttpMethod.POST.name()))
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
            .addLogoutHandler(jwtLogoutHandler))
        .authenticationProvider(customAuthenticationProvider)
        .addFilterBefore(jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter.class)
        .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(CustomAuthenticationProvider custom) {
    return new ProviderManager(custom);
  }

  @Bean
  public LoginFilter loginFilter(AuthenticationManager authenticationManager) {
    LoginFilter filter = new LoginFilter(authenticationManager, jwtProvider, refreshTokenService, objectMapper);
    filter.setAuthenticationManager(authenticationManager);
    filter.setFilterProcessesUrl(SecurityMatchers.LOGIN);
    return filter;
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter(jwtProvider);
  }

  @Bean
  public JwtLogoutHandler logoutHandler() {
    return new JwtLogoutHandler(refreshTokenService);
  }

  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder());
    provider.setHideUserNotFoundExceptions(false);
    return provider;
  }
}
