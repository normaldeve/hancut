package com.system.batch.killbatchsystem.article.infrastructure.batch.goal;

import java.time.OffsetDateTime;

public class GoalSitemapItem {
  private final String url;
  private final String title;          // <news:title>
  private final String imageUrl;       // <image:image><image:loc>
  private final OffsetDateTime publishedAt; // <news:publication_date>

  public GoalSitemapItem(String url, String title, String imageUrl, OffsetDateTime publishedAt) {
    this.url = url;
    this.title = title;
    this.imageUrl = imageUrl;
    this.publishedAt = publishedAt;
  }

  public String getUrl() { return url; }
  public String getTitle() { return title; }
  public String getImageUrl() { return imageUrl; }
  public OffsetDateTime getPublishedAt() { return publishedAt; }
}
