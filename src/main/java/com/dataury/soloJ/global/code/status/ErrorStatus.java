package com.dataury.soloJ.global.code.status;

import com.dataury.soloJ.global.code.BaseErrorCode;
import com.dataury.soloJ.global.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001", "사용자가 없습니다."),
    EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4003", "이메일이 없습니다."),
    NICKNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "MEMBER4002", "닉네임은 필수 입니다."),
    EMAIL_FAILED(HttpStatus.BAD_REQUEST, "MEMBER4004","이메일 전송에 실패하였습니다."),
    EMAIL_EXIST(HttpStatus.BAD_REQUEST,"MEMBER4005","이메일이 이미 존재합니다."),
    NICKNAME_EXIST(HttpStatus.BAD_REQUEST,"MEMBER4006","닉네임이 이미 존재합니다."),

    PASSWORD_VALIDATION_FAILED(HttpStatus.BAD_REQUEST,"MEMBER4007","비밀번호는 영어 대/소문자, 숫자 중 2종류 이상을 조합해야 합니다."),
    EMAIL_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "MEMBER4008","올바르지 않은 이메일 형식입니다."),

    JWT_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH001", "JWT 서명이 올바르지 않습니다."),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH002", "JWT 토큰이 만료되었습니다."),
    JWT_MALFORMED(HttpStatus.UNAUTHORIZED, "AUTH003", "JWT 토큰이 올바르지 않은 형식입니다."),


    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_MISSION_4001", "미션이 존재하지 않습니다."),

    PAGE_BOUND_ERROR(HttpStatus.BAD_REQUEST, "PAGE4001", "페이징 번호가 적절하지 않습니다."),

    CATEGORY_TYPE_ERROR(HttpStatus.BAD_REQUEST,"CATEGORY4001", "카테고리의 타입 문자열이 잘못되었습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND,"CATEGORY4002", "카테고리를 찾을 수 없습니다."),
    SUBCATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY4003", "서브카테고리가 존재하지 않습니다. "),
    SUBCATEGORY_HAS_ACCOUNT(HttpStatus.BAD_REQUEST,"CATEGORY4004", "가계부를 가진 서브 카테고리는 삭제할 수 없습니다."),
    MAINCATEGORY_HAS_SUBCATEGORY(HttpStatus.BAD_REQUEST, "CATEGORY4005", "연관된 서브 카테고리가 존재하는 메인 카테고리는 삭제할 수 없습니다."),


    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION4001", "조회할 알림 목록이 없습니다."),
    NOTIFICATION_ALREADY_READ(HttpStatus.BAD_REQUEST, "NOTIFICATION4002", "이미 읽음처리 된 알람입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE4001", "존재하지 않는 리소스입니다."),

    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "FOLLOW4001", "팔로우 되지 않은 사용자입니다."),
    ALREADY_FOLLOW(HttpStatus.BAD_REQUEST, "FOLLOW4002", "이미 팔로우 된 사용자입니다."),
    FOLLOW_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "FOLLOW4003", "팔로우 내역이 없습니다."),
    INVALID_FOLLOW_DATA(HttpStatus.BAD_REQUEST, "FOLLOW004", "팔로우한 사용자를 찾을 수 없습니다."),

    IMAGE_FAILED(HttpStatus.BAD_REQUEST,"IMAGE4001","이미지 올리는 것을 실패하였습니다."),

    IMAGE_TEXT_FAILD(HttpStatus.BAD_REQUEST, "IMAGETEXT4001", "이미지 텍스트 추출을 실패하였습니다."),

    DISCORD_ERROR(HttpStatus.BAD_REQUEST,"DISCORD4001","디스코드 메시지 보내기에 실패하였습니다, ");



    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
