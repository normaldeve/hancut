-- H2에는 jsonb 타입이 없으므로 json으로 도메인 별칭을 만든다
CREATE DOMAIN IF NOT EXISTS jsonb AS JSON;