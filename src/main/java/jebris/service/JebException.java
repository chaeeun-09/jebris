package jebris.service;

/** 제브 API 호출 실패. status 0은 네트워크·설정 문제. */
public class JebException extends Exception {

    private static final long serialVersionUID = 1L;

    private final int status;

    public JebException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
