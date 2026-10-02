package NotFound.next_campus.global.mail.service;

import NotFound.next_campus.global.exception.BusinessException;
import NotFound.next_campus.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;         // Gmail 계정으로 발송
    private final SpringTemplateEngine templateEngine; // Thymeleaf 템플릿 엔진

    /**
     * 분실물 개인 알림 메일 발송
     * @param toEmail 수신자 이메일
     * @param name 수신자 이름
     * @param itemName 분실물 이름
     * @param date 분실물 등록일
     */
    public void sendPersonalLostEmail(String toEmail, String name, String itemName, LocalDateTime date) {
        // 1. Thymeleaf 컨텍스트 설정
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("itemName", itemName);
        context.setVariable("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        // 2. HTML 템플릿 렌더링
        String html = templateEngine.process("mail/personalLost", context); // resources/templates/mail/personalLost.html

        // 3. MimeMessage 생성 및 설정
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("[분실물 알림] 회원님의 물품이 발견되었습니다");
            helper.setText(html, true); // true = HTML 사용

            // 4. 메일 발송
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
        }
    }
}