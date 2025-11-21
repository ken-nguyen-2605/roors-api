package com.josephken.roors.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authenticationException) {

        var authException = request.getAttribute("authException");
        if (authException != null) {
            // Forward JWT exceptions to global exception handler
            handlerExceptionResolver.resolveException(request, response, null, (Exception) authException);

        } else {
            // Forward Authentication exceptions to global exception handler
            handlerExceptionResolver.resolveException(request, response, null, authenticationException);
        }

    }
}