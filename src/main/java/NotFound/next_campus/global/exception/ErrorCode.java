package NotFound.next_campus.global.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON001", "잘못된 입력입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON002", "권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON003", "서버 오류가 발생했습니다."),

    // Auth / Token
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH001", "Invalid refresh token"),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH002", "Refresh token not found or mismatched"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH003", "Refresh token이 만료되었습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER001", "존재하지 않는 사용자입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER002", "이미 사용 중인 이메일입니다."),

    // Post
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST001", "존재하지 않는 게시물입니다."),
    POST_STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "POST002", "해당 학번의 학생을 찾을 수 없습니다."),
    POST_STATUS_REGISTER_FORBIDDEN(HttpStatus.FORBIDDEN, "POST003", "인계 상태 등록 권한이 없습니다."),
    POST_STATUS_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST004", "인계 상태 수정 권한이 없습니다."),
    NOTICE_REGISTER_FORBIDDEN(HttpStatus.FORBIDDEN, "POST005", "공지는 관리자만 등록할 수 있습니다."),
    NOTICE_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST006", "공지 게시 권한이 없습니다."),
    POST_IMAGE_REGISTER_FORBIDDEN(HttpStatus.FORBIDDEN, "POST007", "해당 게시물에 대한 이미지 등록 권한이 없습니다."),
    POST_IMAGE_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST008", "해당 게시물 이미지에 대한 수정 권한이 없습니다."),
    POST_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST009", "해당 게시물에 대한 수정 권한이 없습니다."),
    POST_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST010", "해당 게시물에 대한 삭제 권한이 없습니다."),
    POST_BULK_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST011", "게시물 일괄 수정 권한이 없습니다."),
    POST_BULK_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "POST012", "게시물 일괄 삭제 권한이 없습니다."),

    // Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMENT001", "존재하지 않는 댓글입니다."),
    COMMENT_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMENT002", "해당 댓글에 대한 수정 권한이 없습니다."),
    COMMENT_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "COMMENT003", "해당 댓글에 대한 삭제 권한이 없습니다."),

    // Category
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY001", "존재하지 않는 카테고리입니다."),

    // Location
    LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "LOCATION001", "존재하지 않는 위치입니다."),

    // Receiver
    RECEIVER_NOT_FOUND(HttpStatus.NOT_FOUND, "RECEIVER001", "존재하지 않는 수령인입니다."),
    RECEIVER_REGISTER_FORBIDDEN(HttpStatus.FORBIDDEN, "RECEIVER002", "수령인 등록 권한이 없습니다."),
    RECEIVER_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "RECEIVER003", "수령인 수정 권한이 없습니다."),
    RECEIVER_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "RECEIVER004", "수령인 삭제 권한이 없습니다."),
    RECEIVER_READ_FORBIDDEN(HttpStatus.FORBIDDEN, "RECEIVER005", "수령인 조회 권한이 없습니다."),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION001", "존재하지 않는 알림입니다."),
    NOTIFICATION_READ_FORBIDDEN(HttpStatus.FORBIDDEN, "NOTIFICATION002", "해당 알림에 대한 읽기 권한이 없습니다."),

    // File / Image
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE001", "이미지가 존재하지 않습니다."),
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE002", "파일 저장 실패");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
