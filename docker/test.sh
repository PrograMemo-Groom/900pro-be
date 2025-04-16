#!/bin/bash

# 컬러 설정
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "코딩 테스트 플랫폼 도커 환경 테스트 시작..."

# 도커 컴포즈 실행 상태 확인
CONTAINERS=$(docker ps --filter "name=webide-code-executor" --format "{{.Names}}" | wc -l)

if [ "$CONTAINERS" -eq "1" ]; then
  echo -e "${GREEN}✅ 코드 실행 컨테이너가 정상적으로 실행 중입니다.${NC}"
else
  echo -e "${RED}❌ 코드 실행 컨테이너가 정상적으로 실행되지 않았습니다. ($CONTAINERS/1)${NC}"
  echo "docker compose up -d 명령어를 실행해주세요."
  exit 1
fi

# API 테스트
echo -e "\n파이썬 코드 실행 테스트..."
curl -s -X POST http://localhost:8080/api/code/execute/python \
  -H "Content-Type: application/json" \
  -d '{"code": "print(\"Hello, World!\")"}' | jq .

echo -e "\n테스트 완료!"
