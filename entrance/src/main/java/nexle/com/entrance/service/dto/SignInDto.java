package nexle.com.entrance.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class SignInDto {
    @Email
    private String email;
    @Size(min = 8, max = 20)
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
