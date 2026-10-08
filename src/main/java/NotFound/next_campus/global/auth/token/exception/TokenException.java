package NotFound.next_campus.global.auth.token.exception;


import NotFound.next_campus.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class TokenException extends RuntimeException {

    private final ErrorCode errorCode;

    public TokenException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}