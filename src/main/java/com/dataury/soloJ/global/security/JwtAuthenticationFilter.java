package com.dataury.soloJ.global.security;

import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private TokenProvider tokenProvider;

    private static final List<String> WHITELIST = List.of(
            "/api/reports/reasons",
            "/api/inquiries/categories"
    );

    private boolean isWhitelisted(HttpServletRequest req) {
        String uri = req.getRequestURI();
        return WHITELIST.stream().anyMatch(uri::equals);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = parseBearerToken(request);

            if (!StringUtils.hasText(token) || token.equalsIgnoreCase("null")) {
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = tokenProvider.extractUserIdFromToken(token);
            String role = tokenProvider.extractUserRoleFromToken(token);

            // 권한 세팅
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            // Principal을 DTO로 세팅
            JwtUserPrincipal principal = new JwtUserPrincipal(userId, role);

            AbstractAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);

            log.info("➡️ 최종 Authentication={}", SecurityContextHolder.getContext().getAuthentication());

            filterChain.doFilter(request, response);

        } catch (GeneralException e) {
            handleException(response, e);
        } catch (Exception e) {
            handleException(response, new GeneralException(ErrorStatus.JWT_MALFORMED));
        }
    }

    private String parseBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void handleException(HttpServletResponse response, GeneralException ex) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(ex.getErrorReasonHttpStatus().getHttpStatus().value());
        response.getWriter().write(new ObjectMapper().writeValueAsString(Map.of(
                "status", ex.getErrorReasonHttpStatus().getCode(),
                "message", ex.getErrorReasonHttpStatus().getMessage()
        )));
    }
}
