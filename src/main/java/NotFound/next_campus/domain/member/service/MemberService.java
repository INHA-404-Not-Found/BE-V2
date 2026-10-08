package NotFound.next_campus.domain.member.service;

import NotFound.next_campus.domain.member.dto.MemberDTO;
import NotFound.next_campus.global.auth.user.CustomUserDetails;

public interface MemberService {

    /* 프로필 조회 */
    MemberDTO.ProfileResponse getProfile(CustomUserDetails userDetails);
}
