package nexle.com.entrance.service;

import nexle.com.entrance.entity.User;
import nexle.com.entrance.exception.RefreshTokenValidationException;
import nexle.com.entrance.service.dto.RefreshTokenDto;
import nexle.com.entrance.service.dto.RefreshTokenRequest;
import nexle.com.entrance.service.dto.SignInDto;
import nexle.com.entrance.service.dto.SignInResponse;
import nexle.com.entrance.service.dto.SignUpDto;
import nexle.com.entrance.service.dto.UserDto;
import org.apache.coyote.BadRequestException;

import java.text.ParseException;

public interface UserService {
    User findByEmail(String email);
    boolean isEmailExist(String email);
    UserDto signUp(SignUpDto dto) throws BadRequestException;
    SignInResponse signIn(SignInDto dto) throws BadRequestException;
    RefreshTokenDto refreshToken(RefreshTokenRequest token) throws BadRequestException, ParseException, RefreshTokenValidationException;
}
