package programo._pro.global;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private String path;
    private String error;

    private ErrorResponse(int status, String message, String path, String error) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.error = error;
    }

    public static ErrorResponse of(int status, String message, String path, String error) {
        return new ErrorResponse(status, message, path, error);
    }
}
