package com.pedianephro.subscription.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendSubscriptionConfirmation_shouldSendMail() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@pedianephro.com");
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);

        emailService.sendSubscriptionConfirmation("a@b.com", "Patient", "Pro", "100 DT");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendExpirationReminder_shouldSendMail() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@pedianephro.com");
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);

        emailService.sendExpirationReminder("a@b.com", "Patient", "Pro", LocalDate.now().plusDays(5));

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendRenewalProposal_shouldSendMail() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@pedianephro.com");
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);

        emailService.sendRenewalProposal(
                "a@b.com",
                "Patient",
                "Basique",
                "Premium",
                "MONTEE",
                10.0,
                "http://localhost:4200/patient/renewal?subscriptionId=1"
        );

        verify(mailSender).send(any(MimeMessage.class));
    }
}

