package NotFound.next_campus.global.auth.token.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenDTO {
    private final String accessToken;
    private final String refreshToken;
}
