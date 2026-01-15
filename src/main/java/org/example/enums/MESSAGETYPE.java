package org.example.enums;

public enum MESSAGETYPE {
    CONNECT(0),
    MOVE(1),
    UPDATE(2),
    DISCONNECT(3),
    SCORE(4);
    private final int code;

    MESSAGETYPE(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
