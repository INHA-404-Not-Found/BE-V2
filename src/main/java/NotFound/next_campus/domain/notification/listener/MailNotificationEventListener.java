package NotFound.next_campus.domain.notification.listener;

import NotFound.next_campus.domain.post.event.PersonalLostMailEvent;
import NotFound.next_campus.global.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MailNotificationEventListener {

    private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePersonalLostMail(PersonalLostMailEvent event) {
        mailService.sendPersonalLostEmail(
                event.toEmail(),
                event.name(),
                event.itemTitle(),
                event.createdAt()
        );
    }
}
