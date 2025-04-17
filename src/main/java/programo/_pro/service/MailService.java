package programo._pro.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import programo._pro.dto.EmailRequest;
import programo._pro.entity.EmailVerification;
import programo._pro.repository.EmailVerificationRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final EmailVerificationRepository emailVerificationRepository;

    @Value("${spring.mail.username}")
    private String username;

    private final JavaMailSender javaMailSender;

    // 이메일 전송 함수
    public void sendMail(EmailRequest emailRequest) throws MessagingException {
        MimeMessage message = CreateMail(emailRequest.getEmail());
        javaMailSender.send(message);
    }

    public MimeMessage CreateMail(String email) throws MessagingException {
        // 랜덤 인증번호 생성
        int code = (int) (Math.random() * (90000)) + 100000; //(int) Math.random() * (최댓값-최소값+1) + 최소값

        // 이메일 인증 객체 생성
        EmailVerification emailVerification = EmailVerification.builder()
                .email(email)
                .code(code)
                .build();

        // 이메일 인증 객체 저장
        emailVerificationRepository.save(emailVerification);
        log.info("emailVerification : {}", emailVerification);


        MimeMessage message = javaMailSender.createMimeMessage();

        // 전송자, 수신자 설정
        message.setFrom(username);
        message.setRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("이메일 인증");

        // 요청 양식
        String body = "";
        body += "<h3>" + "요청하신 인증 번호입니다." + "</h3>";
        body += "<h1>" + code + "</h1>";
        body += "<h3>" + "감사합니다." + "</h3>";
        message.setText(body, "UTF-8", "html");

        return message;
    }

    public boolean mailCheck(String email, int code) {
        // 받아온 이메일을 키값으로 사용하고 기존의 테이블과 인증번호가 일치하는지 확인
        Optional<EmailVerification> verificationOptional =
                emailVerificationRepository.findByEmail(email);

        // 이메일 인증정보가 존재한다면
        if (verificationOptional.isPresent()) {
            // 객체를 꺼냄
            EmailVerification verification = verificationOptional.get();

            // 테이블의 인증코드와 입력받은 인증코드가 같다면
            if (verification.getCode() == code) {

                //인증 상태 true 변경
                verification.setVerified(true);
                emailVerificationRepository.save(verification);

                return true;
            }
        }
        return false;
    }
}