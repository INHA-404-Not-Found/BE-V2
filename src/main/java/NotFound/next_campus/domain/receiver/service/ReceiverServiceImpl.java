package NotFound.next_campus.domain.receiver.service;

import NotFound.next_campus.domain.member.model.Member;
import NotFound.next_campus.domain.member.model.Role;
import NotFound.next_campus.domain.member.repository.MemberRepository;
import NotFound.next_campus.domain.post.model.Post;
import NotFound.next_campus.domain.post.repository.PostRepository;
import NotFound.next_campus.domain.receiver.dto.ReceiverDTO;
import NotFound.next_campus.domain.receiver.model.Receiver;
import NotFound.next_campus.domain.receiver.repository.ReceiverRepository;
import NotFound.next_campus.global.auth.user.CustomUserDetails;
import NotFound.next_campus.global.exception.BusinessException;
import NotFound.next_campus.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReceiverServiceImpl implements ReceiverService {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final ReceiverRepository receiverRepository;

    @Override
    public Long saveReceiver(ReceiverDTO.CreateRequest dto, CustomUserDetails userDetails) {

        Member member = userDetails.getMember();

        if (!Role.ADMIN.equals(member.getRole())) {
            throw new BusinessException(ErrorCode.RECEIVER_REGISTER_FORBIDDEN);
        }

        Post post = postRepository.findById(dto.getPostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        Receiver receiver = receiverRepository.save(Receiver.builder()
                .post(post)
                .name(dto.getName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .studentId(dto.getStudentId())
                .build());

        return receiver.getId();
    }

    @Override
    public void updateReceiver(Long receiverId, ReceiverDTO.UpdateRequest dto, CustomUserDetails userDetails) {

        Member member = userDetails.getMember();

        if (!Role.ADMIN.equals(member.getRole())) {
            throw new BusinessException(ErrorCode.RECEIVER_UPDATE_FORBIDDEN);
        }

        Receiver receiver = receiverRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEIVER_NOT_FOUND));

        if(dto.getName() != null) receiver.setName(dto.getName());
        if(dto.getEmail() != null) receiver.setEmail(dto.getEmail());
        if(dto.getPhoneNumber() != null) receiver.setPhoneNumber(dto.getPhoneNumber());
        if(dto.getStudentId() != null) receiver.setStudentId(dto.getStudentId());
    }

    @Override
    public void deleteReceiver(Long receiverId, CustomUserDetails userDetails) {

        if(!Role.ADMIN.equals(userDetails.getRole())) {
            throw new BusinessException(ErrorCode.RECEIVER_DELETE_FORBIDDEN);
        }

        Receiver receiver = receiverRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEIVER_NOT_FOUND));

        receiverRepository.delete(receiver);
    }

    @Override
    public ReceiverDTO.Response getReceiverInfo(Long receiverId, CustomUserDetails userDetails) {

        if(!Role.ADMIN.equals(userDetails.getRole())) {
            throw new BusinessException(ErrorCode.RECEIVER_READ_FORBIDDEN);
        }

        Receiver receiver = receiverRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RECEIVER_NOT_FOUND));

        return ReceiverDTO.Response.from(receiver);
    }

    @Override
    public ReceiverDTO.Response getReceiverByPost(Long postId, CustomUserDetails userDetails) {

        if(!Role.ADMIN.equals(userDetails.getRole())) {
            throw new BusinessException(ErrorCode.RECEIVER_READ_FORBIDDEN);
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        Optional<Receiver> receiver = receiverRepository.findByPost(post);

        if(receiver.isPresent()) {

            return ReceiverDTO.Response.from(receiver.get());
        }

        return ReceiverDTO.Response.builder().build();
    }

    @Override
    public List<ReceiverDTO.Response> getAllReceivers(CustomUserDetails userDetails) {

        if(!Role.ADMIN.equals(userDetails.getRole())) {
            throw new BusinessException(ErrorCode.RECEIVER_READ_FORBIDDEN);
        }
        
        return receiverRepository.findAll().stream()
                .map(ReceiverDTO.Response::from)
                .toList();
    }
}
