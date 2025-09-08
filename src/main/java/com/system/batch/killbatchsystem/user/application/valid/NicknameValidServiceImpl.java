package com.system.batch.killbatchsystem.user.application.valid;

import com.system.batch.killbatchsystem.user.application.UserRepository;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NicknameValidServiceImpl implements NicknameValidService {

  // 중복 제거 + 빠른 조회를 위해 Set 사용
  private Set<String> forbiddenWords;

  private final UserRepository userRepository;

  // 성능을 위해 미리 컴파일
  private static final Pattern VALID_CHARS = Pattern.compile("^[가-힣a-zA-Z0-9]+$");
  private static final Pattern REPEATED_CHARS = Pattern.compile(".*(.)\\1{2,}.*");

  @PostConstruct
  public void init() {
    loadForbiddenWords();
  }

  @Override
  public boolean validateNickname(String nickname) {
    if (nickname == null) return false;

    nickname = nickname.trim();
    if (nickname.length() < 2 || nickname.length() > 20) return false;

    if (!VALID_CHARS.matcher(nickname).matches()) return false;

    if (REPEATED_CHARS.matcher(nickname).matches()) return false;

    String lower = nickname.toLowerCase(Locale.ROOT);
    for (String bad : forbiddenWords) {
      if (bad.isEmpty()) continue;
      if (lower.contains(bad)) return false;
    }

    return true;
  }

  @Override
  public boolean existsNickname(String nickname) {
    return userRepository.existsByNickname(nickname);
  }

  private void loadForbiddenWords() {
    try {
      ClassPathResource resource = new ClassPathResource("slang.csv");
      try (BufferedReader br = new BufferedReader(
          new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

        // 줄 단위 → 콤마 분할 → 트림/정리 → 소문자 → Set
        Set<String> words = new HashSet<>();
        String line;
        while ((line = br.readLine()) != null) {
          // BOM 제거
          line = removeBom(line).trim();
          if (line.isEmpty()) continue;

          // CSV 한 줄에 여러 단어가 있을 수 있음
          Stream.of(line.split(","))
              .map(String::trim)
              .map(w -> w.replaceAll("^[\"']|[\"']$", "")) // 양끝 따옴표 제거
              .map(w -> w.replaceAll(",$", ""))            // 끝 콤마 제거
              .map(String::toLowerCase)
              .map(this::normalizeWord)
              .filter(w -> !w.isEmpty())
              .filter(w -> !w.equals("slang")) // 헤더 제거
              .forEach(words::add);
        }
        this.forbiddenWords = words;
      }
    } catch (IOException e) {
      throw new RuntimeException("csv 파일 로드 실패", e);
    }
  }

  private String normalizeWord(String w) {
    if (w == null) return "";
    String n = w.toLowerCase(Locale.ROOT);

    n = n.replace("0", "o")
        .replace("1", "i")
        .replace("3", "e")
        .replace("4", "a")
        .replace("5", "s")
        .replace("7", "t")
        .replace("$", "s")
        .replace("@", "a");

    n = n.replaceAll("[^a-z0-9가-힣]", "");

    n = n.replaceAll("(.)\\1{2,}", "$1$1");

    return n;
  }

  private String removeBom(String s) {
    if (!s.isEmpty() && s.charAt(0) == '\uFEFF') {
      return s.substring(1);
    }
    return s;
  }
}
