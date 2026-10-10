package NotFound.next_campus.domain.post.event;

public record LostItemMatchNotificationEvent(
        Long memberId,
        String title,
        String message,
        String link
) {
}
