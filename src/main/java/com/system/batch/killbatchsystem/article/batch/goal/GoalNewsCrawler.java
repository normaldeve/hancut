package com.system.batch.killbatchsystem.article.batch.goal;

import com.system.batch.killbatchsystem.article.batch.common.NewsCrawler;
import com.system.batch.killbatchsystem.article.domain.Article;
import com.system.batch.killbatchsystem.model.ArticleSource;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Slf4j
@Component(value = "goalNewsCrawler")
public class GoalNewsCrawler implements NewsCrawler {

  @Override
  public List<Article> getArticles(String url, int limit) throws Exception {

    Document rss = Jsoup.connect(url)
        .userAgent("Mozilla/5.0")
        .parser(Parser.xmlParser())
        .timeout(100000)
        .get();

    List<Article> articles = new ArrayList<>();
    Elements items = rss.select("url");

    WebDriverManager.chromedriver().setup();
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--disable-gpu");
    options.addArguments("--disable-logging");
    options.addArguments("--log-level=3");

    WebDriver driver = new ChromeDriver(options);

    try {
      int count = 0;

      for (Element element : items) {
        if (count >= limit) {
          break;
        }

        String link = text(element.selectFirst("loc"));

        String articleId = null;
        if (link != null && !link.isBlank()) {
          articleId = link.substring(link.lastIndexOf("/") + 1);
        }

        String pubDateStr = text(element.selectFirst("news|publication_date"));
        LocalDateTime publishedAt = parsePublishedAt(pubDateStr);

        String thumbnailUrl = text(element.selectFirst("image|loc"));

        if (link == null || link.isBlank())
          continue;

        Document doc = Jsoup.connect(link)
            .userAgent("Mozilla/5.0")
            .referrer("https://www.google.com")
            .timeout(15000)
            .get();

        if (!link.contains("lists"))
          continue;

        String content = fetchAllBodiesWithSelenium(driver, link);

        Article article = Article.createArticle(
            articleId,
            link,
            content,
            thumbnailUrl,
            ArticleSource.GOAL,
            publishedAt
        );

        articles.add(article);
        count++;
      }
    } finally {
      if (driver != null) {
        driver.quit();
        log.info("WebDriver가 정리되었습니다.");
      }
    }

    return articles;
  }

  private String text(Element element) {
    return element == null ? null : element.text();
  }

  // String -> LocalDateTime 변환
  private LocalDateTime parsePublishedAt(String pubDateStr) {
    LocalDateTime publishedAt = LocalDateTime.now();
    if (pubDateStr != null && !pubDateStr.isBlank()) {
      try {
        ZonedDateTime zdt = ZonedDateTime.parse(pubDateStr, DateTimeFormatter.ISO_DATE_TIME);
        publishedAt = zdt.withZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
      } catch (Exception e) {
        log.warn("날짜 파싱 실패: {}", pubDateStr, e);
      }
    }
    return publishedAt;
  }

  private String fetchAllBodiesWithSelenium(WebDriver driver, String link) {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

    try {
      driver.get(link);

      StringBuilder contentBuilder = new StringBuilder();

      List<WebElement> teaserElements = driver.findElements(
          By.cssSelector("[data-testid='article-teaser']")
      );

      for (WebElement element : teaserElements) {
        String text = element.getText().trim();
        if (!text.isEmpty()) {
          contentBuilder.append(text).append("\n\n");
        }
      }

      // article body 가져오기
      List<WebElement> bodyElements = wait.until(
          ExpectedConditions.presenceOfAllElementsLocatedBy(
              By.cssSelector("[data-testid='article-body']")
          )
      );

      for (WebElement element : bodyElements) {
        String text = element.getText().trim();
        if (!text.isEmpty()) {
          contentBuilder.append(text).append("\n\n");
        }
      }

      return contentBuilder.toString().trim();

    } catch (Exception e) {
      log.warn("Article body 크롤링 실패: {}", link, e);
      return "";
    }
  }
}