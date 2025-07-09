-- spec_certificates 테이블에 organization 컬럼 추가
ALTER TABLE spec_certificates 
ADD COLUMN IF NOT EXISTS organization VARCHAR(200) AFTER name;

-- 기존 데이터가 있을 경우를 위한 기본값 설정 (선택사항)
-- UPDATE spec_certificates SET organization = '미입력' WHERE organization IS NULL;
