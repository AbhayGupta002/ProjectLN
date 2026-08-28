package com.example.personalassistant.security;

import java.util.List;
import org.slf4j.Logger;
import java.io.IOException;
import java.util.Collection;
import org.slf4j.LoggerFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;



@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = null;
            try {
                username = jwtUtil.extractUsername(token);
            } catch (Exception e) {
                log.warn("JWT parse failed: {}", e.getMessage());
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                boolean valid = jwtUtil.validateToken(token);
                log.debug("JWT valid: {}", valid);
                if (valid) {
                    UserDetails userDetails = null;
                    try {
                        userDetails = userDetailsService.loadUserByUsername(username);
                    } catch (UsernameNotFoundException ex) {
                        log.debug("UserDetails not found for {} : {}", username, ex.getMessage());
                    }

                    // build authorities: prefer signed token role claim to enforce cryptographic scope
                    Collection<? extends GrantedAuthority> authorities = List.of();
                    String role = jwtUtil.extractRole(token);
                    if (role != null && !role.trim().isEmpty()) {
                        authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                    } else if (userDetails != null) {
                        authorities = userDetails.getAuthorities();
                    }

                    // set Authentication if we have any authority or userDetails
                    if ((userDetails != null || !authorities.isEmpty())) {
                        Object principal = userDetails != null ? userDetails : username;
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(principal, null, authorities);
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        log.debug("Authentication set for {} with authorities {}", username, authorities);
                    } else {
                        log.debug("No authorities found for {}, skipping auth set", username);
                    }
                }
            }
        }
        if (request.getRequestURI().startsWith("/api/public")) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }


}
