package com.koreanit.spring.security;

import java.util.List;

public interface UserRoleRepository {

  // 예: ["ROLE_USER", "ROLE_ADMIN"]
  List<String> findRolesByUserId(Long userId);

  // 권장: 가입 시 기본 권한 부여
  void addRole(Long userId, String role);
}