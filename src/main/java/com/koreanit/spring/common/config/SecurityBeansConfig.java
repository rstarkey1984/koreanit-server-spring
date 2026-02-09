package com.koreanit.spring.common.config;

import java.util.HashMap;
import java.util.Map;

// PasswordEncoder를 전역 Bean으로 등록한다.
// 비밀번호 해시 정책(BCrypt)을 한 곳에서 고정하기 위한 설정이다.

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityBeansConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    String defaultId = "bcrypt";

    Map<String, PasswordEncoder> encoders = new HashMap<>();
    encoders.put("bcrypt", new BCryptPasswordEncoder());
    encoders.put("argon2",
        new Argon2PasswordEncoder(
            16, 32, 1, 65536, 3));

    DelegatingPasswordEncoder delegating = new DelegatingPasswordEncoder(defaultId, encoders);

    // 접두사 없는 기존 해시도 bcrypt로 검증
    delegating.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());

    return delegating;
  }
}