package NotFound.next_campus.domain.member.dto;

import NotFound.next_campus.domain.member.model.Member;
import NotFound.next_campus.domain.member.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class MemberDTO {

    @Getter
    @AllArgsConstructor
    public static class ProfileResponse {
        private Long studentId;
        private String name;
        private String email;
        private String department;
        private Role role;

        public static ProfileResponse from(Member member) {

            return new ProfileResponse(
                    member.getStudentId(),
                    member.getName(),
                    member.getEmail(),
                    member.getDepartment(),
                    member.getRole()
            );
        }
    }
}
