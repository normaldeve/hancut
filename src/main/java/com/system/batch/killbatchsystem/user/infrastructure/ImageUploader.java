package com.system.batch.killbatchsystem.user.infrastructure;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploader {

  String upload(MultipartFile file);

  void deleteImage(String imageUrl);

}
