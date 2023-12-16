package nexle.com.entrance.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import nexle.com.entrance.entity.User;
import nexle.com.entrance.exception.RefreshTokenValidationException;
import nexle.com.entrance.service.TokenService;
import nexle.com.entrance.service.UserService;
import nexle.com.entrance.service.dto.RefreshTokenDto;
import nexle.com.entrance.service.dto.RefreshTokenRequest;
import nexle.com.entrance.service.dto.SignInDto;
import nexle.com.entrance.service.dto.SignInResponse;
import nexle.com.entrance.service.dto.SignUpDto;
import nexle.com.entrance.service.dto.UserDto;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
public class AuthenticationController {

    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserDto> signUp(@Valid @RequestBody SignUpDto signUpDto) throws BadRequestException {
        if(userService.isEmailExist(signUpDto.getEmail())) {
            throw new BadRequestException();
        }
        UserDto dto = userService.signUp(signUpDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponse> signIn(@Valid @RequestBody SignInDto dto) throws BadRequestException {
        SignInResponse response = userService.signIn(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<SignInResponse> signOut(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication !=null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            String email = authentication.getName();
            User user = userService.findByEmail(email);
            tokenService.deleteToken(user);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenDto> refreshToken(@Valid @RequestBody RefreshTokenRequest token) throws BadRequestException, ParseException, RefreshTokenValidationException {
        RefreshTokenDto dto = userService.refreshToken(token);
        return ResponseEntity.ok(dto);
    }
}
