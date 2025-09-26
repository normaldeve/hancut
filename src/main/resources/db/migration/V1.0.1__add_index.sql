-- 기본 최신순 조회
create index if not exists idx_ai_summary_published_at
    on ai_summary (published_at desc);

-- 소스별 필터링 + 최신순
CREATE INDEX IF NOT EXISTS idx_ai_summary_source_published
    ON ai_summary (source_name, published_at DESC);

-- 키워드 검색 최적화
CREATE INDEX IF NOT EXISTS idx_keywords_summary_published
    ON ai_summary_keywords (keyword, ai_summary_id);

-- 특정 요약의 키워드 조회 (관리 기능)
CREATE INDEX IF NOT EXISTS idx_ai_article_keywords_article
    ON ai_summary_keywords (ai_summary_id);

-- 커서 페이징 최적화
CREATE INDEX IF NOT EXISTS idx_comments_ai_created_id_desc
    ON comments (ai_summary_id, created_at DESC, id DESC);

--반응 통계 조회 최적화
CREATE INDEX IF NOT EXISTS idx_reactions_summary_type
    ON reactions (ai_summary_id, type);

-- 사용자별 반응 조회
CREATE INDEX IF NOT EXISTS idx_reactions_user_summary
    ON reactions (user_id, ai_summary_id);

-- 배치 작업 최적화
CREATE INDEX IF NOT EXISTS idx_articles_pending_status
    ON articles (summarize_status, id)
    WHERE summarize_status = 'PENDING';

-- 토큰 조회 최적화
CREATE INDEX IF NOT EXISTS idx_refresh_token_nickname
    ON refresh_token (nickname);