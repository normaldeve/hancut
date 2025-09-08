package com.system.batch.killbatchsystem.user.infrastructure;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ImageUploader implements ImageUploader {

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  @Value("${file.upload.profiles.allowed-extensions}")
  private String allowedExtensions;

  @Value("${file.upload.profiles.max-size}")
  private long maxFileSize;

  @Value("${file.upload.profiles.path}")
  private String uploadPath;

  private final S3Client s3Client;

  @Override
  public String upload(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }

    validateImageFile(file);

    String fileName = generateFileName(file.getOriginalFilename());
    String key = uploadPath + fileName;

    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .contentType(file.getContentType())
          .contentLength(file.getSize())
          .build();

      s3Client.putObject(putObjectRequest,
          RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

      // 업로드된 파일의 URL 생성
      String imageUrl = generateImageUrl(key);
      log.info("이미지를 성공적으로 업로드 했습니다: {}", imageUrl);

      return imageUrl;
    } catch (IOException e) {
      log.error("S3 이미지 업로드 중 오류가 발생했습니다", e);
      throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다");
    } catch (S3Exception e) {
      log.error("S3 업로드 중 오류가 발생했습니다", e);
      throw new RuntimeException("S3 업로드 중 오류가 발생했습니다");
    }
  }

  @Override
  public void deleteImage(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) {
      return;
    }

    try {
      String key = extractKeyFromUrl(imageUrl);

      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();

      s3Client.deleteObject(deleteObjectRequest);
      log.info("Image deleted successfully: {}", imageUrl);

    } catch (S3Exception e) {
      log.error("Failed to delete image from S3: {}", imageUrl, e);
      throw new RuntimeException("이미지 삭제 중 오류가 발생했습니다.");
    }
  }

  /**
   * 이미지 파일 검증
   */
  private void validateImageFile(MultipartFile file) {
    // 파일 크기 검증
    if (file.getSize() > maxFileSize) {
      throw new RuntimeException(String.format("파일 크기가 제한을 초과했습니다. 최대 %dMB까지 업로드 가능합니다.", maxFileSize / (1024 * 1024)));
    }

    // 파일 확장자 검증
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null) {
      throw new RuntimeException("파일명이 유효하지 않습니다.");
    }

    String extension = getFileExtension(originalFilename);
    List<String> allowedExtensionList = Arrays.asList(allowedExtensions.toLowerCase().split(","));

    if (!allowedExtensionList.contains(extension.toLowerCase())) {
      throw new RuntimeException(String.format("지원하지 않는 파일 형식입니다. 지원 형식: %s", allowedExtensions));
    }

    // Content-Type 검증
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new RuntimeException("이미지 파일만 업로드 가능합니다.");
    }
  }

  /**
   * 고유한 파일명 생성
   */
  private String generateFileName(String originalFilename) {
    String extension = getFileExtension(originalFilename);
    return UUID.randomUUID() + "." + extension;
  }

  /**
   * 파일 확장자 추출
   */
  private String getFileExtension(String filename) {
    int lastDotIndex = filename.lastIndexOf(".");
    if (lastDotIndex == -1) {
      throw new RuntimeException("파일 확장자가 없습니다.");
    }
    return filename.substring(lastDotIndex + 1);
  }

  /**
   * S3 URL에서 키 추출
   */
  private String extractKeyFromUrl(String imageUrl) {
    // S3 URL 형식: https://bucket-name.s3.region.amazonaws.com/key
    try {
      if (imageUrl.contains(bucketName)) {
        // S3 직접 URL인 경우
        int keyStartIndex = imageUrl.indexOf(uploadPath);
        if (keyStartIndex != -1) {
          return imageUrl.substring(keyStartIndex);
        }
      }
      // 다른 형태의 URL인 경우 마지막 경로 부분을 clothesImagePath와 결합
      String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
      return uploadPath + fileName;
    } catch (Exception e) {
      log.warn("Failed to extract key from URL: {}", imageUrl);
      return imageUrl;
    }
  }

  /**
   * 이미지 URL 생성
   */
  private String generateImageUrl(String key) {
    return String.format("https://%s.s3.%s.amazonaws.com/%s",
        bucketName,
        s3Client.serviceClientConfiguration().region().id(),
        key);
  }
}
