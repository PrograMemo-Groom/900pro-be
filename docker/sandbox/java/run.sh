#!/bin/bash

# 시스템 리소스 제한 설정
ulimit -t 10      # CPU 시간 10초 제한
ulimit -v 2097152 # 가상 메모리 2GB로 제한 (증가됨)
ulimit -n 100     # 파일 디스크립터 100개로 제한

# 임시 디렉토리 설정
TEMP_DIR="/tmp/javarun"
mkdir -p $TEMP_DIR
cd $TEMP_DIR

# 표준 입력에서 코드 읽기
cat > Main.java

# 결과를 저장할 JSON 객체 초기화
result="{
  \"status\": \"success\",
  \"stdout\": \"\",
  \"stderr\": \"\",
  \"error\": null,
  \"exit_code\": 0
}"

# 자바 컴파일
javac Main.java 2> compile_error.txt
compile_status=$?

if [ $compile_status -ne 0 ]; then
  # 컴파일 오류 발생
  compile_error=$(cat compile_error.txt)
  result=$(jq --arg error "컴파일 오류" \
            --arg stderr "$compile_error" \
            --arg exit_code "$compile_status" \
            '.status = "error" | .error = $error | .stderr = $stderr | .exit_code = ($exit_code|tonumber)' \
            <<< "$result")
  echo "$result"
  rm -f compile_error.txt Main.java
  exit 0
fi

# 타임아웃 설정과 함께 자바 실행 (JVM 옵션 추가)
timeout 5s java -Xms64m -Xmx256m -XX:MaxMetaspaceSize=64m -XX:+UseSerialGC Main > output.txt 2> error.txt
run_status=$?

# 표준 출력 및 표준 오류 읽기
stdout=$(cat output.txt)
stderr=$(cat error.txt)

# 종료 상태에 따른 결과 처리
if [ $run_status -eq 124 ]; then
  # 실행 시간 초과
  result=$(jq --arg error "실행 시간 초과" \
            --arg stdout "$stdout" \
            --arg stderr "$stderr" \
            '.status = "timeout" | .error = $error | .stdout = $stdout | .stderr = $stderr | .exit_code = 124' \
            <<< "$result")
elif [ $run_status -ne 0 ]; then
  # 실행 중 오류 발생
  result=$(jq --arg error "실행 중 오류 발생" \
            --arg stdout "$stdout" \
            --arg stderr "$stderr" \
            --arg exit_code "$run_status" \
            '.status = "error" | .error = $error | .stdout = $stdout | .stderr = $stderr | .exit_code = ($exit_code|tonumber)' \
            <<< "$result")
else
  # 성공적으로 실행 완료
  result=$(jq --arg stdout "$stdout" \
            --arg stderr "$stderr" \
            '.status = "success" | .stdout = $stdout | .stderr = $stderr | .exit_code = 0' \
            <<< "$result")
fi

# JSON 결과 출력
echo "$result"

# 임시 파일 정리
rm -f Main.java Main.class output.txt error.txt compile_error.txt
