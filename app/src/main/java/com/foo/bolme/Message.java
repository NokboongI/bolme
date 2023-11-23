package com.foo.bolme;

public class Message {
    private String contactName; // 연락처 이름을 저장하는 변수
    private String phoneNumber; // 전화번호를 저장하는 변수
    public static final int TYPE_RIGHT = 1; // 오른쪽 정렬 타입을 나타내는 정수 상수
    public static final int TYPE_LEFT = 2; // 왼쪽 정렬 타입을 나타내는 정수 상수

    private String messageContent; // 메시지 내용을 저장하는 변수
    private int messageType; // 메시지 타입을 나타내는 변수

    // 생성자 - 메시지의 연락처 이름, 전화번호, 내용, 타입을 초기화
    public Message(String contactName, String phoneNumber, String messageContent, int messageType) {
        this.contactName = contactName;
        this.phoneNumber = phoneNumber;
        this.messageContent = messageContent;
        this.messageType = messageType;
    }

    public String getContactName() {
        return contactName; // 연락처 이름 반환
    }

    public String getPhoneNumber() {
        return phoneNumber; // 전화번호 반환
    }

    public String getMessageContent() {
        return messageContent; // 메시지 내용 반환
    }
    public int getMessageType() {
        return messageType;
    }
}
