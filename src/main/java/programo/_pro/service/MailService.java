package programo._pro.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import programo._pro.dto.mailDto.EmailRequest;
import programo._pro.entity.EmailVerification;
import programo._pro.entity.User;
import programo._pro.global.exception.userException.NotFoundUserException;
import programo._pro.repository.EmailVerificationRepository;
import programo._pro.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    @Value("${spring.mail.username}")
    private String username;

    private final JavaMailSender javaMailSender;
    private final BCryptPasswordEncoder passwordEncoder;

    // 이메일 전송 함수
    @Transactional
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

    @Transactional
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

    // 비밀번호 초기화 메서드
    @Transactional
    public boolean resetPassword(String email) {
        if(userRepository.existsByEmail(email)) {
            // 랜덤 비밀번호 생성
            String tempPassword = generateRandomPassword();
            // 비밀번호 초기화 메일 전송
            sendTemporaryPasswordMail(email, tempPassword);

            // 변경한 비밀번호로 회원정보 수정
            User user = userRepository.findByEmail(email).orElseThrow(NotFoundUserException::byEmail);
            String encodedPassword = passwordEncoder.encode(tempPassword); // 암호화 된 비밀번호 생성
            user.setPassword(encodedPassword); // 암호화된 임시 비밀번호 설정

            return true;
        }
        return false;
    }

    /**
     * 임시 비밀번호 자동 생성 메서드
     */
    private static String generateRandomPassword() {
        int length = 8;
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()-_=+[]{};:<>/?";

        String allChars = upper + lower + digits + special;
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        // 필수 문자 유형 1개씩 포함 (보안 강화 목적)
        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(special.charAt(random.nextInt(special.length())));

        // 나머지는 전체 풀에서 랜덤 선택
        for (int i = 4; i < length; i++) {
            sb.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 순서 섞기
        List<Character> passwordChars = sb.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(passwordChars);

        // 최종 문자열로 변환
        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }

    /**
     * 임시 비밀번호 전송
     */
    public void sendTemporaryPasswordMail(String email, String tempPassword) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(username);
            helper.setTo(email);
            helper.setSubject("임시 비밀번호");
            String body = "<h2>000에 오신걸 환영합니다!</h2><p>아래의 임시 비밀번호를 사용하세요.</p><h1>" + tempPassword + "</h1><h3>반드시 비밀번호를 재설정하세요.</h3>";
            helper.setText(body, true);

            // 해당 이메일로 전송
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("임시 비밀번호 전송 오류", e);
        }
    }
}