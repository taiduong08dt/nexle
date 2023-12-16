package nexle.com.entrance.service.dto;

import jakarta.validation.constraints.NotNull;

public class RefreshTokenRequest {
    @NotNull
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
