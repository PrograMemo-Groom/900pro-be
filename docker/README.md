# 코딩테스트 플랫폼 도커 환경 설정

이 디렉토리는 코딩테스트 플랫폼의 도커 환경 설정을 포함합니다. 설정은 두 개의 파일로 구성됩니다:
- `compose.yaml`: MariaDB 데이터베이스와 Redis 캐시 등 핵심 인프라 서비스를 정의합니다.
- `compose.override.yaml`: Python, Java, C/C++, JavaScript 코드 실행을 위한 샌드박스 환경(Executor)들을 정의합니다.

## 빠른 시작

**프로젝트 루트 디렉토리** (이 `docker` 폴더의 상위 디렉토리)에서 단 한 줄의 명령어로 모든 환경을 구축할 수 있습니다:

```bash
docker compose up -d
```

이 명령어는 `compose.yaml`과 `compose.override.yaml` 파일을 자동으로 병합하여 다음 작업을 수행합니다:
1. 필요한 모든 이미지를 빌드하거나 다운로드합니다.
2. 데이터베이스, 캐시, 코드 실행 컨테이너를 포함한 모든 서비스를 구성하고 시작합니다.
3. 환경 설정 상태를 확인 후 안내 메시지를 출력합니다.

## 주요 기능

Docker Compose 설정을 통해 다음 서비스들이 관리됩니다:

- **MariaDB**: 데이터베이스 서버 (포트 3307) - `compose.yaml`
- **Redis**: 캐시 및 메시지 큐 (포트 6379) - `compose.yaml`
- **Python 코드 실행 엔진**: 파이썬 코드 실행 컨테이너 - `compose.override.yaml`
- **JavaScript 코드 실행 엔진**: 자바스크립트 코드 실행 컨테이너 - `compose.override.yaml`
- **Java 코드 실행 엔진**: 자바 코드 실행 컨테이너 - `compose.override.yaml`
- **GCC 코드 실행 엔진**: C/C++ 코드 실행 컨테이너 - `compose.override.yaml`

## 환경 관리

- **컨테이너 상태 확인**: `docker ps`
- **로그 확인**: `docker logs [컨테이너ID]`
- **환경 중지**: `docker compose down`
- **환경 재시작**: `docker compose up -d` (변경사항 적용 시 필요)

## 코드 실행 시간 관리

코드 실행 시간(타임아웃) 및 관련 컨테이너 설정은 `.env` 파일을 통해 중앙에서 관리됩니다.

1. `.env` 파일에서 타임아웃 및 설정값 정의:
```dotenv
# 각 언어별 코드 실행 최대 시간 (초)
# (compose.override.yaml 에서 컨테이너 환경변수로 설정되고, 백엔드 서비스 로직에서도 사용됨)
PYTHON_EXECUTION_TIMEOUT=10
JAVASCRIPT_EXECUTION_TIMEOUT=10
JAVA_EXECUTION_TIMEOUT=5
C_EXECUTION_TIMEOUT=3

# 애플리케이션에서 코드 실행기 컨테이너 상태 확인 시 타임아웃 (초)
# (백엔드 서비스 로직에서 사용됨)
CONTAINER_CHECK_TIMEOUT=5
```

2. 타임아웃 값을 변경하려면:
   - `.env` 파일의 값을 수정합니다.
   - **주의:** 여기에 정의된 모든 값(`*_EXECUTION_TIMEOUT`, `CONTAINER_CHECK_TIMEOUT`)은 백엔드 서비스 로직에서도 사용됩니다.
   - 따라서 `.env` 파일 수정 후에는 다음 두 가지 작업을 모두 수행해야 합니다:
     1. `docker compose down` 후 `docker compose up -d` 명령어로 Docker 환경을 재시작하여 컨테이너 설정을 업데이트합니다 (`*_EXECUTION_TIMEOUT` 적용).
     2. 주 애플리케이션(백엔드)을 재시작하여 서비스 로직이 새로운 설정값을 읽도록 합니다 (모든 타임아웃 값 적용).

코드 실행 시간 제한(`*_EXECUTION_TIMEOUT`)은 `compose.override.yaml`을 통해 각 코드 실행 샌드박스 컨테이너의 환경 변수로 전달되며, 동시에 백엔드 서비스 로직에서도 참조될 수 있습니다.
`CONTAINER_CHECK_TIMEOUT`은 주로 백엔드 애플리케이션의 로직 내에서 사용됩니다.

## API 사용 예시

아래 예시는 로컬 머신(예: `localhost:8080`)에서 실행 중인 주 애플리케이션(백엔드)을 통해 Docker 컨테이너 내의 코드 실행 엔진 API를 호출하는 방법을 보여줍니다. 이 Docker 환경은 주로 백엔드 개발 시 필요한 DB, Redis 및 코드 실행기들을 제공하는 데 사용됩니다.

Python 코드 실행:
```bash
curl -X POST http://localhost:8080/api/code/execute/python \
  -H "Content-Type: application/json" \
  -d '{"code": "print(\\"Hello, Python World!\\")"}'
```

JavaScript 코드 실행:
```bash
curl -X POST http://localhost:8080/api/code/execute/javascript \
  -H "Content-Type: application/json" \
  -d '{"code": "console.log(\\"Hello, JavaScript World!\\");"}'
```

Java 코드 실행:
```bash
curl -X POST http://localhost:8080/api/code/execute/java \
  -H "Content-Type: application/json" \
  -d '{"code": "public class Main { public static void main(String[] args) { System.out.println(\\"Hello, Java World!\\"); } }"}'
```

C/C++ 코드 실행 (GCC):
```bash
curl -X POST http://localhost:8080/api/code/execute/gcc \
  -H "Content-Type: application/json" \
  -d '{"code": "#include <stdio.h>\nint main() { printf(\\\"Hello, C World!\\\\n\\"); return 0; }"}'
```

## 주의사항

- 이 환경은 개발 및 테스트 목적으로 설계되었습니다.
- 모든 컨테이너는 `restart: unless-stopped` 정책을 가지고 있어 Docker 데몬이나 서버 재시작 시에도 자동으로 재시작됩니다.
