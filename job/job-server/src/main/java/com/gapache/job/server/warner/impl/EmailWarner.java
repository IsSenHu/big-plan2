package com.gapache.job.server.warner.impl;

import com.gapache.job.server.warner.Warner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * @author HuSen
 * @since 2021/2/8 2:41 下午
 */
@Component
public class EmailWarner implements Warner {

    private final JavaMailSender javaMailSender;

    public EmailWarner(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void warning(String subject, String text, String to, String from) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(from);
        message.setTo(to);
        javaMailSender.send(message);
    }
}
