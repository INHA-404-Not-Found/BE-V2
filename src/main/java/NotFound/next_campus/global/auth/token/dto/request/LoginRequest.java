package NotFound.next_campus.global.auth.token.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {


    @NotNull
    private Long studentId;
    @NotBlank
    private String password;
    private Boolean isWeb; // true -> web login (cookie), false -> app (json)

    public Boolean getIsWeb() {
        return isWeb == null ? false : isWeb;
    }
}
