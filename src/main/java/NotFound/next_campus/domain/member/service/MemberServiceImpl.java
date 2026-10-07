package NotFound.next_campus.domain.member.service;

import NotFound.next_campus.domain.member.dto.MemberDTO;
import NotFound.next_campus.domain.member.model.Member;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {

    @Override
    public MemberDTO.ProfileResponse getProfile(CustomUserDetails userDetails) {

        Member member = userDetails.getMember();

        return MemberDTO.ProfileResponse.from(member);
    }
}
