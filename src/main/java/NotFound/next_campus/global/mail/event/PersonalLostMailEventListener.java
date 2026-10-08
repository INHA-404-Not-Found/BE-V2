package NotFound.next_campus.global.mail.event;

import NotFound.next_campus.global.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PersonalLostMailEventListener {

    private final MailService mailService;

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
