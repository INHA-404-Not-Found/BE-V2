package NotFound.next_campus.domain.notification.service;

import NotFound.next_campus.domain.member.model.Member;
import NotFound.next_campus.domain.member.repository.MemberRepository;
import NotFound.next_campus.domain.notification.dto.NotificationDTO;
import NotFound.next_campus.domain.notification.repository.NotificationRepository;
import NotFound.next_campus.global.firebase.repository.FcmTokenRepository;
import NotFound.next_campus.global.firebase.service.FirebaseMessagingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private FcmTokenRepository fcmTokenRepository;
    @Mock
    private FirebaseMessagingService firebaseMessagingService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void FCM_토큰이_있으면_알림을_저장하고_토큰_개수만큼_발송한다() {

        Member member = Member.builder().id(1L).build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(fcmTokenRepository.findTokensByMember(member)).thenReturn(List.of("token-a", "token-b"));

        notificationService.sendAndSaveNotification(
                NotificationDTO.CreateRequest.builder()
                        .memberId(1L)
                        .title("습득물 매칭")
                        .message("비슷한 분실물이 등록됐어요")
                        .link("/posts/1")
                        .build()
        );

        verify(notificationRepository, times(1)).save(any());
        verify(firebaseMessagingService, times(1)).sendNotification("token-a", "습득물 매칭", "비슷한 분실물이 등록됐어요");
        verify(firebaseMessagingService, times(1)).sendNotification("token-b", "습득물 매칭", "비슷한 분실물이 등록됐어요");
    }

    @Test
    void FCM_토큰이_없어도_알림은_저장하고_발송은_건너뛴다() {

        Member member = Member.builder().id(1L).build();
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(fcmTokenRepository.findTokensByMember(member)).thenReturn(List.of());

        notificationService.sendAndSaveNotification(
                NotificationDTO.CreateRequest.builder()
                        .memberId(1L)
                        .title("습득물 매칭")
                        .message("비슷한 분실물이 등록됐어요")
                        .build()
        );

        verify(notificationRepository, times(1)).save(any());
        verify(firebaseMessagingService, never()).sendNotification(any(), any(), any());
    }
}
