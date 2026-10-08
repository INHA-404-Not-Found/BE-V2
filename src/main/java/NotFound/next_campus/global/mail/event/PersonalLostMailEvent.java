package NotFound.next_campus.global.mail.event;

import java.time.LocalDateTime;

public record PersonalLostMailEvent(
        String toEmail,
        String name,
        String itemTitle,
        LocalDateTime createdAt
) {
}
