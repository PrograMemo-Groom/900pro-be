#!/usr/bin/env python
import sys
import os
import json
import resource
import traceback
import subprocess
from io import StringIO
from contextlib import redirect_stdout, redirect_stderr

def set_resource_limits():
    """시스템 리소스 제한 설정"""
    try:
        # 실행 시간 제한 (초)
        resource.setrlimit(resource.RLIMIT_CPU, (10, 10))
        # 메모리 제한 (바이트)
        resource.setrlimit(resource.RLIMIT_AS, (1024 * 1024 * 1024, 1024 * 1024 * 1024))  # 1GB
        # 프로세스 수 제한
        resource.setrlimit(resource.RLIMIT_NPROC, (100, 100))
        # 파일 디스크립터 수 제한
        resource.setrlimit(resource.RLIMIT_NOFILE, (100, 100))
    except Exception as e:
        print(f"리소스 제한 설정 오류: {e}")
        # 현재 리소스 제한 값 출력
        for res in [resource.RLIMIT_CPU, resource.RLIMIT_AS, resource.RLIMIT_NPROC, resource.RLIMIT_NOFILE]:
            try:
                limit = resource.getrlimit(res)
                print(f"Resource {res}: soft={limit[0]}, hard={limit[1]}")
            except:
                pass

def execute_python_code(code_str):
    """Python 코드 실행"""
    result = {
        "status": "success",
        "stdout": "",
        "stderr": "",
        "error": None,
        "exit_code": 0
    }

    # 코드 파일 경로 설정
    code_file = "/tmp/user_code.py"

    # 오류 해결: /code/code.py 파일이 필요한 경우를 위해 빈 파일 생성
    try:
        # /code 디렉토리가 있는지 확인하고, 없으면 생성
        if not os.path.exists("/code"):
            os.makedirs("/code")

        # /code/code.py 파일이 없으면 빈 파일 생성
        if not os.path.exists("/code/code.py"):
            with open("/code/code.py", "w") as f:
                f.write("# 자동 생성된 파일")
    except Exception as e:
        print(f"파일 생성 오류: {e}")

    # 임시 파일에 코드 저장
    with open(code_file, "w") as f:
        f.write(code_str)

    try:
        # 리소스 제한 설정
        set_resource_limits()

        # 코드 실행 - 분리된 프로세스에서 실행
        proc = subprocess.run(
            ["python3", code_file],
            capture_output=True,
            text=True,
            timeout=10,  # 시간 제한 증가
            env=os.environ.copy()  # 환경 변수 전달
        )

        result["stdout"] = proc.stdout
        result["stderr"] = proc.stderr
        result["exit_code"] = proc.returncode

        if proc.returncode != 0:
            result["status"] = "error"
            result["error"] = "코드 실행 중 오류 발생"

    except subprocess.TimeoutExpired:
        result["status"] = "timeout"
        result["error"] = "코드 실행 시간이 초과되었습니다 (10초)"
    except Exception as e:
        result["status"] = "error"
        result["error"] = str(e)
        result["stderr"] = traceback.format_exc()
        result["exit_code"] = 1
    finally:
        # 임시 파일 삭제
        try:
            os.remove(code_file)
        except:
            pass

    return result

if __name__ == "__main__":
    # 명령행에서 실행 시 코드 파일 읽기
    if len(sys.argv) > 1:
        with open(sys.argv[1], 'r') as f:
            code = f.read()
    else:
        # 표준 입력에서 코드 읽기
        code = sys.stdin.read()

    # 코드 실행
    result = execute_python_code(code)

    # JSON 형식으로 결과 출력
    print(json.dumps(result))
