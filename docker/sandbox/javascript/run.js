#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { VM, VMScript } = require('vm2');
const util = require('util');
const childProcess = require('child_process');
const exec = util.promisify(childProcess.exec);

// 환경변수에서 타임아웃 값 가져오기 (초 단위)
const EXECUTION_TIMEOUT = parseInt(process.env.CODE_EXECUTION_TIMEOUT || '10', 10);
// 리소스 제한 상수
const EXECUTION_TIMEOUT_MS = EXECUTION_TIMEOUT * 1000; // 밀리초로 변환
const MAX_MEMORY_MB = 500; // 500MB

/**
 * JavaScript 코드를 실행하는 메인 함수
 */
async function executeJavaScriptCode(code) {
    const result = {
        status: 'success',
        stdout: '',
        stderr: '',
        error: null,
        exit_code: 0
    };

    try {
        // 코드 파일 경로 설정
        const codeFile = '/tmp/user_code.js';

        // 코드를 임시 파일에 저장
        fs.writeFileSync(codeFile, code);

        // 샌드박스 환경 설정
        const vm = new VM({
            timeout: EXECUTION_TIMEOUT_MS,
            sandbox: {
                console: {
                    log: (...args) => {
                        result.stdout += args.map(arg =>
                            typeof arg === 'object' ? JSON.stringify(arg) : String(arg)
                        ).join(' ') + '\n';
                    },
                    error: (...args) => {
                        result.stderr += args.map(arg =>
                            typeof arg === 'object' ? JSON.stringify(arg) : String(arg)
                        ).join(' ') + '\n';
                    }
                },
                process: {
                    stdout: {
                        write: (data) => {
                            result.stdout += data;
                            return true;
                        }
                    },
                    stderr: {
                        write: (data) => {
                            result.stderr += data;
                            return true;
                        }
                    }
                },
                Buffer: Buffer,
                setTimeout: setTimeout,
                clearTimeout: clearTimeout,
                setImmediate: setImmediate,
                clearImmediate: clearImmediate
            }
        });

        // 코드 실행
        const script = new VMScript(code, codeFile);
        vm.run(script);

        // 임시 파일 정리
        fs.unlinkSync(codeFile);
    } catch (error) {
        result.status = 'error';

        if (error.message.includes('Script execution timed out')) {
            result.error = `코드 실행 시간이 초과되었습니다 (${EXECUTION_TIMEOUT}초)`;
        } else {
            result.error = error.message;
            result.stderr = error.stack;
        }

        result.exit_code = 1;
    }

    return result;
}

// 메인 실행 코드
async function main() {
    // 입력 스트림에서 코드 읽기
    let code = '';

    process.stdin.on('data', (chunk) => {
        code += chunk.toString();
    });

    process.stdin.on('end', async () => {
        const result = await executeJavaScriptCode(code);
        console.log(JSON.stringify(result));
    });
}

main().catch(error => {
    console.error(JSON.stringify({
        status: 'error',
        error: '시스템 오류: ' + error.message,
        stdout: '',
        stderr: error.stack,
        exit_code: 1
    }));
});
