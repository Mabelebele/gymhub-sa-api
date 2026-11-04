package za.co.gymhubsa.api.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import za.co.gymhubsa.api.common.dto.ErrorResponse;

import java.io.IOException;

/**
 * Returns a 401 JSON error (in our standard {@link ErrorResponse} envelope)
 * for requests that reach a protected endpoint with no/invalid credentials,
 * instead of Spring Security's default 403-for-anonymous behaviour.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = ErrorResponse.of("Authentication is required to access this resource",
                HttpServletResponse.SC_UNAUTHORIZED, request.getRequestURI());
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
