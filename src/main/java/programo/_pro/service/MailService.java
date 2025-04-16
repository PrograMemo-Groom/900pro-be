package programo._pro.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import programo._pro.dto.EmailRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    @Value("${spring.mail.username}")
    private String username;

    private final JavaMailSender javaMailSender;
    private static int number;

    // 랜덤 인증번호 생성
    public static void createNumber() {
        number = (int) (Math.random() * (90000)) + 100000; //(int) Math.random() * (최댓값-최소값+1) + 최소값
    }

    public MimeMessage CreateMail(String email) throws MessagingException {
        // 랜덤 인증번호 생성
        createNumber();
        MimeMessage message = javaMailSender.createMimeMessage();

        // 전송자, 수신자 설정
        message.setFrom(username);
        message.setRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("이메일 인증");

        // 요청 양식
        String body = "";
        body += "<h3>" + "요청하신 인증 번호입니다." + "</h3>";
        body += "<h1>" + number + "</h1>";
        body += "<h3>" + "감사합니다." + "</h3>";
        message.setText(body, "UTF-8", "html");

        return message;
    }

    // 이메일 전송 함수
    public int sendMail(EmailRequest emailRequest) throws MessagingException {
        MimeMessage message = CreateMail(emailRequest.getEmail());
        javaMailSender.send(message);

        return number;
    }
}