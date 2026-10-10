package NotFound.next_campus.domain.notification.listener;

import NotFound.next_campus.domain.notification.dto.NotificationDTO;
import NotFound.next_campus.domain.post.event.LostItemMatchNotificationEvent;
import NotFound.next_campus.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PushNotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLostItemMatch(LostItemMatchNotificationEvent event) {
        notificationService.sendAndSaveNotification(
                NotificationDTO.CreateRequest.builder()
                        .memberId(event.memberId())
                        .title(event.title())
                        .message(event.message())
                        .link(event.link())
                        .build()
        );
    }
}
