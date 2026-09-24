package NotFound.next_campus.global.common;

import NotFound.next_campus.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 JSON에서 제외
public record CommonResponse<T> (boolean success, String code, String message, T data) {

    /* 데이터가 없는 성공 응답 */
    public static <T> CommonResponse<T> ok() {
        return new CommonResponse<>(true, null, null, null);
    }

    public static <T> CommonResponse<T> ok(T data) {
        return new CommonResponse<>(true, null, null, data);
    }

    public static CommonResponse<Void> fail(ErrorCode errorCode) {
        return new CommonResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }
}
