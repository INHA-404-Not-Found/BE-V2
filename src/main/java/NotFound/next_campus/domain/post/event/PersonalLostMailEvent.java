package NotFound.next_campus.domain.post.event;

import java.time.LocalDateTime;

public record PersonalLostMailEvent(
        String toEmail,
        String name,
        String itemTitle,
        LocalDateTime createdAt
) {
}
