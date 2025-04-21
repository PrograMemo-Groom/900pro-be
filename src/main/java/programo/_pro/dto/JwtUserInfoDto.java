package programo._pro.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class JwtUserInfoDto {
    private long Id;
    private String email;
}