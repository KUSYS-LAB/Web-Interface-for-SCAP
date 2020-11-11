package ac.kr.korea.cdm.filter;

import ac.kr.korea.cdm.constants.Constants;
import ac.kr.korea.cdm.dto.AuthenticationDto;
import ac.kr.korea.cdm.service.VerificationService;
import ac.kr.korea.cdm.util.CryptoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.PublicKey;
import java.util.Map;

public class TokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(TokenFilter.class);
    @Autowired
    private VerificationService verificationService;
    @Autowired
    private CryptoHelper cryptoHelper;

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        Cookie token = null;

        for (Cookie cookie: httpServletRequest.getCookies()) {
            if (cookie.getName().toLowerCase().equals("token"))  {
                token = cookie;
                break;
            }
        }

        if (token == null) {
            httpServletResponse.sendError(HttpStatus.SC_FORBIDDEN);
            return;
        }

        String tokenStr = URLDecoder.decode(token.getValue(), "UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> tokenMap = objectMapper.readValue(tokenStr, Map.class);
        Map<String, Object> body = (Map<String, Object>) tokenMap.get(Constants.CDM_WEB_BODY);
        String cname = (String) body.get(Constants.CDM_WEB_CNAME);
        String ticket = (String) body.get(Constants.CDM_WEB_TICKET);
        AuthenticationDto sessionAuth = (AuthenticationDto) httpServletRequest.getSession()
                .getAttribute(Constants.CDM_WEB_USER);

        if (sessionAuth != null && !(sessionAuth.getCname().equals(cname) && sessionAuth.getTicket().equals(ticket))) {
            httpServletResponse.sendError(HttpStatus.SC_FORBIDDEN);
            return;
        }

        if (!this.verificationService.verifyUserSignature(tokenMap)) {
            httpServletResponse.sendError(HttpStatus.SC_FORBIDDEN);
            return;
        }

        if (!this.verificationService.verifyAcTicket(tokenMap)) {
            httpServletResponse.sendError(HttpStatus.SC_FORBIDDEN);
            return;
        }

        // ticket 만료되면, 다시 받아오게 만들어야 하는데....
        if (sessionAuth == null || !sessionAuth.getTicket().equals(ticket)) {
            AuthenticationDto auth = new AuthenticationDto(cname, ticket);
            httpServletRequest.getSession().setAttribute(Constants.CDM_WEB_USER, auth);
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String removeBaseUrl = request.getRequestURI().replace(request.getContextPath(), "");

        return removeBaseUrl.contains("/sign") || removeBaseUrl.equals("/")
                || removeBaseUrl.startsWith("/vendor") || removeBaseUrl.startsWith("/js")
                || removeBaseUrl.startsWith("/css") || removeBaseUrl.startsWith("/favicon");
    }
}
