package com.tonkar.volleyballreferee.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

final class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    TokenAuthenticationFilter(final RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response) {
        final String tokenParam = Optional.ofNullable(request.getHeader("Authorization")).orElse(request.getParameter("not-found"));

        final String token = Optional.ofNullable(tokenParam)
                .map(value -> value.replace("Bearer", ""))
                .map(String::trim)
                .orElseThrow(() -> new BadCredentialsException("Could not find the authentication token"));

        final Authentication authentication = new UsernamePasswordAuthenticationToken("", token);
        return getAuthenticationManager().authenticate(authentication);
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain,
                                            final Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }
}
