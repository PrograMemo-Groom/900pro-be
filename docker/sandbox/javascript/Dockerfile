FROM node:18-slim

# 필수 패키지 설치
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 작업 디렉토리 설정
WORKDIR /code

# package.json 파일 복사 및 의존성 설치
COPY package.json /code/
RUN npm install

# 빈 코드 파일 미리 생성
RUN touch /code/code.js && chmod 666 /code/code.js

# 실행 스크립트 복사 및 실행 권한 설정
COPY run.js /code/run.js
RUN chmod +x /code/run.js

# 낮은 권한의 사용자 생성
RUN useradd -m coderunner
# /code 디렉토리의 소유권 및 권한 설정
RUN chown -R coderunner:coderunner /code && chmod -R 755 /code
USER coderunner

# 컨테이너 실행 대기 명령
CMD ["sh", "-c", "echo '컨테이너 준비 완료' && sleep infinity"]
