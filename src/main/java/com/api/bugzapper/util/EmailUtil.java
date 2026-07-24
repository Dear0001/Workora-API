package com.api.bugzapper.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class EmailUtil {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void sendOtpEmail(String email, String otp) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Verify OTP");

        Context context = new Context();
        context.setVariable("otp", otp);

        mimeMessageHelper.setText(templateEngine.process("send-otp-mail", context), true);
        javaMailSender.send(mimeMessage);
    }

    /**
     * Fired per phase/project member on create+update — synchronous SMTP sends here
     * previously blocked the HTTP response for every recipient (multiplied across
     * a whole member list). Async so the request returns without waiting on mail delivery.
     */
    @Async
    public void sendNotificationToEmail(String email, String message, String type) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Notification from BugZapper");

        Context context = new Context();
        context.setVariable("message", message);
        context.setVariable("type", type);

        mimeMessageHelper.setText(templateEngine.process("send-notification-mail", context), true);
        javaMailSender.send(mimeMessage);
    }

}
