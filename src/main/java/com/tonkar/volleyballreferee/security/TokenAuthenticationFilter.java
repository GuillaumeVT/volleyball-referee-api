package com.tonkar.volleyballreferee.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;

    public TokenAuthenticationFilter(TokenAuthenticationProvider tokenAuthenticationProvider) {
        this.authenticationManager = new ProviderManager(tokenAuthenticationProvider);
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        Optional<String> optionalToken = Optional
                .ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(value -> value.replace("Bearer", ""))
                .map(String::trim);

        if (optionalToken.isPresent()) {
            try {
                String token = optionalToken.get();
                Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("", token));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            } catch (AuthenticationException e) {
                log.error(e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            log.error("Could not find the authentication token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

    }
}
