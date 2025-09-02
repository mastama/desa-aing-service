package com.yolifay.domain.port.out;

public interface MailerPortOut {
    void sendVerification(String email, String subject, String body);
}
