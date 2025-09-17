package com.system.batch.killbatchsystem.user.api;

import com.system.batch.killbatchsystem.summary.domain.UserSearchCond;
import com.system.batch.killbatchsystem.user.application.UserService;
import com.system.batch.killbatchsystem.user.domain.CreateUser;
import com.system.batch.killbatchsystem.user.domain.Role;
import com.system.batch.killbatchsystem.user.domain.User;
import com.system.batch.killbatchsystem.user.domain.UserResponse;
import com.system.batch.killbatchsystem.user.security.domain.UpdateUserRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping
  public ResponseEntity<User> createUser(@Validated @RequestBody CreateUser request) {
    User user = userService.createUser(request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(user);
  }

  @GetMapping
  public ResponseEntity<Page<UserResponse>> search(
      @RequestParam(required = false) String nickname,
      @RequestParam(required = false) Role role,
      @PageableDefault(size = 20, sort = "createdAt", direction = Direction.DESC)
      Pageable pageable
  ) {
    UserSearchCond cond = new UserSearchCond(nickname, role);
    Page<UserResponse> users = userService.search(cond, pageable).map(UserResponse::from);

    return ResponseEntity.ok(users);
  }

  @PatchMapping(
      path = "{nickname}",
      consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}
  )
  public ResponseEntity<UserResponse> update(
      @PathVariable String nickname,
      @RequestPart(required = false) UpdateUserRequest request,
      @RequestPart(required = false) MultipartFile profileImage
  ) {
    User updatedUser = userService.update(nickname, request, Optional.ofNullable(profileImage));

    return ResponseEntity.ok(UserResponse.from(updatedUser));
  }

}
