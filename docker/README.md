# 코딩테스트 플랫폼 도커 환경 설정

이 디렉토리는 코딩테스트 플랫폼의 도커 환경 설정을 포함합니다.

## 빠른 시작

단 한 줄의 명령어로 모든 환경을 구축할 수 있습니다:

```bash
docker compose up -d
```

이 명령어는:
1. 필요한 모든 이미지를 빌드하고
2. 코드 실행 컨테이너를 구성하며
3. 환경 설정 상태를 확인 후 안내 메시지를 출력합니다

## 주요 기능

- **MariaDB**: 데이터베이스 서버 (포트 3307)
- **Redis**: 캐시 및 메시지 큐 (포트 6379)
- **Python 코드 실행 엔진**: 파이썬 코드 실행 컨테이너
- **Java 코드 실행 엔진**: 자바 코드 실행 컨테이너

## 환경 관리

- **컨테이너 상태 확인**: `docker ps`
- **로그 확인**: `docker logs [컨테이너ID]`
- **환경 중지**: `docker compose down`
- **환경 재시작**: `docker compose up -d`

## API 사용 예시

Python 코드 실행:
```bash
curl -X POST http://localhost:8080/api/code/execute/python \
  -H "Content-Type: application/json" \
  -d '{"code": "print(\"Hello, World!\")"}'
```

Java 코드 실행:
```bash
curl -X POST http://localhost:8080/api/code/execute/java \
  -H "Content-Type: application/json" \
  -d '{"code": "public class Main { public static void main(String[] args) { System.out.println(\"Hello, Java World!\"); } }"}'
```

## 주의사항

- 이 환경은 24시간 연속 운영을 위해 설계되었습니다.
- 모든 컨테이너는 `restart: unless-stopped` 정책을 가지고 있어 서버 재시작 시에도 자동으로 시작됩니다.
