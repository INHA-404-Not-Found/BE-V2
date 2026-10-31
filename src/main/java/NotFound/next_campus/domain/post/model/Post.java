package NotFound.next_campus.domain.post.model;

import NotFound.next_campus.domain.location.model.Location;
import NotFound.next_campus.domain.member.model.Member;
import NotFound.next_campus.domain.member.model.Role;
import NotFound.next_campus.global.exception.BusinessException;
import NotFound.next_campus.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "location_detail")
    private String locationDetail;

    @Column(nullable = false)
    private String title;

    private String content;

    @Column(name = "stored_location")
    private String storedLocation;

    // 완료 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PostStatus status;

    // 게시물 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "post_type")
    private PostType type;

    @Column(name = "is_personal")
    private Boolean isPersonal;

    @Column(name = "student_id")
    private String studentId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void validateOwnerOrAdmin(Member requester, ErrorCode onDenied) {

        boolean isOwner = member.getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if(!isOwner && !isAdmin) throw new BusinessException(onDenied);
    }
}
