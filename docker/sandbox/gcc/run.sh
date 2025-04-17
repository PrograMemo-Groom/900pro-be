#!/bin/bash

# 이 스크립트는 도커 컨테이너 내에서 C/C++ 코드를 실행합니다.
# 실행 방법:
# - C 코드 실행: 표준 입력으로 C 코드를 받아 "c" 인자를 전달
# - C++ 코드 실행: 표준 입력으로 C++ 코드를 받아 "cpp" 인자를 전달

# 환경변수에서 실행 시간 제한 가져오기 (초 단위)
EXECUTION_TIMEOUT=${CODE_EXECUTION_TIMEOUT:-3}

# 실행 언어 확인
LANG="${1:-c}"  # 기본값은 C

# 파일명 설정
if [ "$LANG" = "cpp" ]; then
    SRC_FILE="main.cpp"
    COMPILE_CMD="g++ -std=c++17 -o main $SRC_FILE"
else
    SRC_FILE="main.c"
    COMPILE_CMD="gcc -o main $SRC_FILE"
fi

# 표준 입력에서 코드 읽어오기
cat > "/code/$SRC_FILE"
if [ $? -ne 0 ]; then
    echo "{ \"status\": \"error\", \"message\": \"코드 파일 생성 실패\" }" >&2
    exit 1
fi

# 코드 컴파일
echo "{ \"status\": \"compiling\" }" >&2
$COMPILE_CMD 2> compile_err.txt
if [ $? -ne 0 ]; then
    ERROR=$(cat compile_err.txt)
    echo "{ \"status\": \"error\", \"message\": \"컴파일 오류\", \"error\": \"$ERROR\" }" >&2
    exit 1
fi

# 실행 파일 권한 설정 및 실행
chmod +x main
echo "{ \"status\": \"running\" }" >&2

# 실행 및 결과 출력 (표준 출력 및 표준 오류를 모두 캡처)
timeout ${EXECUTION_TIMEOUT}s ./main 2> run_err.txt
EXIT_CODE=$?

# 실행 결과 처리
if [ $EXIT_CODE -eq 124 ] || [ $EXIT_CODE -eq 137 ]; then
    echo "{ \"status\": \"error\", \"message\": \"실행 시간 초과 (${EXECUTION_TIMEOUT}초)\" }" >&2
    exit 2
elif [ $EXIT_CODE -ne 0 ]; then
    ERROR=$(cat run_err.txt)
    echo "{ \"status\": \"error\", \"message\": \"실행 오류\", \"error\": \"$ERROR\", \"exit_code\": $EXIT_CODE }" >&2
    exit $EXIT_CODE
fi

echo "{ \"status\": \"success\" }" >&2
exit 0
