package com.koreanit.spring.security;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
public class CsrfController {

  @GetMapping("/csrf")
  public Map<String, Object> csrf(HttpServletRequest request, CsrfToken token) {

    Map<String, Object> result = new LinkedHashMap<>();

    /* =====================
     * 1. Cookie 정보
     * ===================== */
    Cookie[] cookies = request.getCookies();

    if (cookies == null) {
      result.put("cookies", "NO_COOKIES");
    } else {
      Map<String, Object> cookieInfo = new LinkedHashMap<>();
      for (Cookie c : cookies) {
        cookieInfo.put(c.getName(), c.getValue());
      }
      result.put("cookies", cookieInfo);
    }

    /* =====================
     * 2. Session 정보
     * ===================== */
    HttpSession session = request.getSession(false);

    if (session == null) {
      result.put("session", "NO_SESSION");
    } else {
      Map<String, Object> sessionInfo = new LinkedHashMap<>();
      sessionInfo.put("id", session.getId());

      Map<String, Object> attrs = new LinkedHashMap<>();
      session.getAttributeNames().asIterator()
          .forEachRemaining(name -> attrs.put(name, session.getAttribute(name)));

      sessionInfo.put("attributes", attrs);
      result.put("session", sessionInfo);
    }

    /* =====================
     * 3. CSRF Token 정보
     * ===================== */
    result.put("csrfToken", Map.of(
        "headerName", token != null ? token.getHeaderName() : null,
        "token", token != null ? token.getToken() : null
    ));

    return result;
  }
}
