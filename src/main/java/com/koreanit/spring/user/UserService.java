// Users 도메인의 비즈니스 흐름을 담당하는 Service
// Repository 결과를 Domain으로 변환하고, 암호화/검증을 수행한다.
package com.koreanit.spring.user;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.koreanit.spring.common.error.ApiException;
import com.koreanit.spring.common.error.ErrorCode;
import com.koreanit.spring.security.SecurityUtils;

@Service
public class UserService {

  private static final int MAX_LIMIT = 1000;

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  private int normalizeLimit(int limit) {
    if (limit <= 0) {
      throw new ApiException(ErrorCode.INVALID_REQUEST, "limit 값이 유효하지 않습니다");
    }
    return Math.min(limit, MAX_LIMIT);
  }

  private String toDuplicateMessage(DuplicateKeyException e) {
    String m = (e.getMessage() == null) ? "" : e.getMessage();

    // MySQL 기준: "Duplicate entry ... for key '...'"
    // DB/드라이버에 따라 메시지 포맷은 달라질 수 있으므로
    // key 이름 기반으로만 판단한다.
    if (m.contains("for key") && (m.contains("users.username") || m.contains("'username'") || m.contains("username"))) {
      return "이미 존재하는 username입니다";
    }
    if (m.contains("for key") && (m.contains("users.email") || m.contains("'email'") || m.contains("email"))) {
      return "이미 존재하는 email입니다";
    }

    return "이미 존재하는 값입니다";
  }

  // 정상 흐름: 회원가입 → PK 반환
  public Long create(String username, String password, String nickname, String email) {
    username = username.trim().toLowerCase();
    nickname = nickname.trim().toLowerCase();
    email = email.trim().toLowerCase();
    String hash = passwordEncoder.encode(password);

    try {
      Long userId = userRepository.save(username, hash, nickname, email);
      return userId;
    } catch (DuplicateKeyException e) {
      throw new ApiException(
          ErrorCode.DUPLICATE_RESOURCE,
          toDuplicateMessage(e));
    }
  }

  public boolean isSelf(Long userId) {
    Long currentUserId = SecurityUtils.currentUserId();
    return currentUserId != null && userId != null && currentUserId.equals(userId);
  }

  // 정상 흐름: 단건 조회 → Domain 반환
  @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(#id)")
  public User get(Long id) {
    try {
      UserEntity e = userRepository.findById(id);
      return UserMapper.toDomain(e);

    } catch (EmptyResultDataAccessException e) {
      throw new ApiException(
          ErrorCode.NOT_FOUND_RESOURCE,
          "존재하지 않는 사용자입니다. id=" + id);
    }
  }

  // 정상 흐름: 목록 조회 → Domain 리스트 반환
  @PreAuthorize("hasRole('ADMIN')")
  public List<User> list(int limit) {
    int safeLimit = normalizeLimit(limit);
    return UserMapper.toDomainList(userRepository.findAll(safeLimit));
  }

  // 정상 흐름: 닉네임 변경
  @PreAuthorize("hasRole('ADMIN') or #id == @userService.isSelf(#id)")
  public void changeNickname(Long id, String nickname) {
    nickname = nickname.trim().toLowerCase();

    // 1) 대상 존재 여부 확인 (없으면 여기서 404)
    User user = get(id);

    String newNickname = nickname;

    // 2) 값이 동일하면 변경 없음 → 정상 처리
    if (user.getNickname().equals(newNickname)) {
      return;
    }

    // 3) 실제 변경
    int updated = userRepository.updateNickname(id, newNickname);

    if (updated == 0) {
      throw new ApiException(
          ErrorCode.NOT_FOUND_RESOURCE,
          "존재하지 않는 사용자입니다. id=" + id);
    }
  }

  // 정상 흐름: 비밀번호 변경 (해시 저장)
  @PreAuthorize("hasRole('ADMIN') or #id == @userService.isSelf(#id)")
  public void changePassword(Long id, String password) {
    String hash = passwordEncoder.encode(password);

    int updated = userRepository.updatePassword(id, hash);

    if (updated == 0) {
      throw new ApiException(
          ErrorCode.NOT_FOUND_RESOURCE,
          "존재하지 않는 사용자입니다. id=" + id);
    }
  }

  // 정상 흐름: 삭제
  @PreAuthorize("hasRole('ADMIN') or #id == T(com.koreanit.spring.security.SecurityUtils).currentUserId()")
  public void delete(Long id) {
    int deleted = userRepository.deleteById(id);

    if (deleted == 0) {
      throw new ApiException(
          ErrorCode.NOT_FOUND_RESOURCE,
          "존재하지 않는 사용자입니다. id=" + id);
    }
  }

  // 정상 흐름: 로그인 자격 검증 → userId 반환 (세션 저장은 Controller 책임)
  public Long login(String username, String password) {
    try {
      UserEntity en = userRepository.findByUsername(username);
      boolean ok = passwordEncoder.matches(password, en.getPassword());
      if (!ok) {
        throw new ApiException(ErrorCode.INTERNAL_ERROR, "비밀번호 검증 실패");
      }

      return en.getId();
    } catch (EmptyResultDataAccessException e) {
      throw new ApiException(ErrorCode.NOT_FOUND_RESOURCE, "존재하지 않는 사용자입니다. username=" + username);
    }

  }

  public void changeEmail(Long id, String email) {
    String normalized = (email == null) ? null : email.toLowerCase();
    userRepository.updateEmail(id, normalized);
  }
}