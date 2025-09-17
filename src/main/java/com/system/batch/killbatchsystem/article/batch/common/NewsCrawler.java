package com.system.batch.killbatchsystem.article.batch.common;

import com.system.batch.killbatchsystem.article.domain.Article;
import java.util.List;

public interface NewsCrawler {

  List<Article> getArticles(String url, int limit) throws Exception;

}
