package programo._pro.service.executor;

/**
 * 코드 실행 과정에서 발생할 수 있는 다양한 에러 타입을 정의합니다.
 */
public enum ErrorType {
    // 컨테이너 관련 에러
    CONTAINER_NOT_AVAILABLE("CONTAINER_001", "실행 환경을 사용할 수 없습니다"),
    CONTAINER_TIMEOUT("CONTAINER_002", "실행 시간이 초과되었습니다"),

    // 코드 실행 관련 에러
    EXECUTION_SYNTAX_ERROR("EXEC_001", "코드 구문에 오류가 있습니다"),
    EXECUTION_RUNTIME_ERROR("EXEC_002", "코드 실행 중 오류가 발생했습니다"),
    EXECUTION_COMPILE_ERROR("EXEC_003", "코드 컴파일 중 오류가 발생했습니다"),
    CODE_SIZE_EXCEEDED("EXEC_004", "코드 크기가 제한을 초과했습니다"),

    // 시스템 관련 에러
    SYSTEM_IO_ERROR("SYS_001", "입출력 오류가 발생했습니다"),
    SYSTEM_INTERRUPTED("SYS_002", "코드 실행이 중단되었습니다"),
    SYSTEM_UNKNOWN_ERROR("SYS_999", "알 수 없는 오류가 발생했습니다");

    private final String code;
    private final String defaultMessage;

    ErrorType(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
