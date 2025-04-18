#!/bin/bash

# 컬러 설정
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "코딩 테스트 플랫폼 도커 환경 테스트 시작..."

# 도커 컴포즈 실행 상태 확인 - 파이썬
PYTHON_CONTAINERS=$(docker ps --filter "name=webide-python-executor" --format "{{.Names}}" | wc -l)

if [ "$PYTHON_CONTAINERS" -eq "1" ]; then
  echo -e "${GREEN}✅ 파이썬 코드 실행 컨테이너가 정상적으로 실행 중입니다.${NC}"
else
  echo -e "${RED}❌ 파이썬 코드 실행 컨테이너가 정상적으로 실행되지 않았습니다. ($PYTHON_CONTAINERS/1)${NC}"
  echo "docker compose up -d 명령어를 실행해주세요."
  exit 1
fi

# 도커 컴포즈 실행 상태 확인 - 자바
JAVA_CONTAINERS=$(docker ps --filter "name=webide-java-executor" --format "{{.Names}}" | wc -l)

if [ "$JAVA_CONTAINERS" -eq "1" ]; then
  echo -e "${GREEN}✅ 자바 코드 실행 컨테이너가 정상적으로 실행 중입니다.${NC}"
else
  echo -e "${RED}❌ 자바 코드 실행 컨테이너가 정상적으로 실행되지 않았습니다. ($JAVA_CONTAINERS/1)${NC}"
  echo "docker compose up -d 명령어를 실행해주세요."
  exit 1
fi

# 도커 컴포즈 실행 상태 확인 - C/C++
C_CONTAINERS=$(docker ps --filter "name=webide-gcc-executor" --format "{{.Names}}" | wc -l)

if [ "$C_CONTAINERS" -eq "1" ]; then
  echo -e "${GREEN}✅ C/C++ 코드 실행 컨테이너가 정상적으로 실행 중입니다.${NC}"
else
  echo -e "${RED}❌ C/C++ 코드 실행 컨테이너가 정상적으로 실행되지 않았습니다. ($C_CONTAINERS/1)${NC}"
  echo "docker compose up -d 명령어를 실행해주세요."
  exit 1
fi

# API 테스트 - 파이썬
echo -e "\n파이썬 코드 실행 테스트..."
curl -s -X POST http://localhost:8080/api/code/execute/python \
  -H "Content-Type: application/json" \
  -d '{"code": "print(\"Hello, World!\")"}' | jq .

# API 테스트 - 자바
echo -e "\n자바 코드 실행 테스트..."
curl -s -X POST http://localhost:8080/api/code/execute/java \
  -H "Content-Type: application/json" \
  -d '{"code": "public class Main { public static void main(String[] args) { System.out.println(\"Hello, Java World!\"); } }"}' | jq .

# API 테스트 - C
echo -e "\nC 코드 실행 테스트..."
curl -s -X POST http://localhost:8080/api/code/execute/c \
  -H "Content-Type: application/json" \
  -d '{"code": "#include <stdio.h>\nint main() {\n    printf(\"Hello, C World!\");\n    return 0;\n}"}' | jq .

# API 테스트 - C++
echo -e "\nC++ 코드 실행 테스트..."
curl -s -X POST http://localhost:8080/api/code/execute/cpp \
  -H "Content-Type: application/json" \
  -d '{"code": "#include <iostream>\nint main() {\n    std::cout << \"Hello, C++ World!\" << std::endl;\n    return 0;\n}"}' | jq .

echo -e "\n테스트 완료!"
