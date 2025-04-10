package programo._pro.global.filter;

import programo._pro.service.RedisService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class WaitingFilter extends OncePerRequestFilter {
    private final RedisService redisService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String waitingNumber = request.getHeader("WaitingNumber");
            redisService.contains("processingQueue", waitingNumber);
            String encodedRedirectURL = response.encodeRedirectURL(
                    request.getContextPath() + "/");
            response.setStatus(HttpStatus.TEMPORARY_REDIRECT.value());
            response.setHeader("Location", encodedRedirectURL);
        } catch (Exception e) {
            String encodedRedirectURL = response.encodeRedirectURL(
                    request.getContextPath() + "/waiting");

            response.setStatus(HttpStatus.TEMPORARY_REDIRECT.value());
            response.setHeader("Location", encodedRedirectURL);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
