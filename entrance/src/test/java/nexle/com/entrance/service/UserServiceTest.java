package nexle.com.entrance.service;

import nexle.com.entrance.entity.Token;
import nexle.com.entrance.entity.User;
import nexle.com.entrance.exception.RefreshTokenValidationException;
import nexle.com.entrance.repository.TokenRepository;
import nexle.com.entrance.repository.UserRepository;
import nexle.com.entrance.security.JwtUtil;
import nexle.com.entrance.service.dto.RefreshTokenDto;
import nexle.com.entrance.service.dto.RefreshTokenRequest;
import nexle.com.entrance.service.dto.SignInDto;
import nexle.com.entrance.service.dto.SignInResponse;
import nexle.com.entrance.service.dto.SignUpDto;
import nexle.com.entrance.service.dto.UserDto;
import nexle.com.entrance.service.impl.UserServiceImpl;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.prefs.BackingStoreException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    UserServiceImpl service;
    @Mock
    UserRepository userRepository;
    @Mock
    TokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;

    @Captor
    ArgumentCaptor<Token> argumentCaptor;

    @Test
    public void test_findByEmail_notFound() {
        String email = "test@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        User user = service.findByEmail(email);

        assertNull(user);
    }

    @Test
    public void test_findByEmail_found() {
        String email = "test@test.com";
        User mockUser = new User();
        mockUser.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        User user = service.findByEmail(email);

        assertNotNull(user);
        assertEquals(email, user.getEmail());
    }
    @Test
    public void test_isEmailExist_false() {
        String email = "test@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertFalse(service.isEmailExist(email));
    }
    @Test
    public void test_isEmailExist_true() {
        String email = "test@test.com";
        User mockUser = new User();
        mockUser.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        assertTrue(service.isEmailExist(email));
    }
    @Test
    public void test_signUp_false() {
        SignUpDto dto = new SignUpDto();
        dto.setEmail("test@test.com");
        dto.setPassword("password");
        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(mockUser));
        try{
            service.signUp(dto);
            fail("must thow BadRequestException");
        }catch (BadRequestException e) {
            assertTrue(e.getMessage().startsWith("account with this email is exist"));
        }
    }
    @Test
    public void test_signUp_ok() throws BadRequestException {
        SignUpDto dto = new SignUpDto();
        dto.setEmail("test@test.com");
        dto.setPassword("password");
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        UserDto userDto = service.signUp(dto);

        assertNotNull(userDto);
        assertEquals("test@test.com", userDto.getEmail());
    }
    @Test
    public void test_signIn_ok() throws BadRequestException {
        SignInDto dto = new SignInDto();
        dto.setEmail("test@test.com");
        dto.setPassword("password");
        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(mockUser)).thenReturn("token");
        when(jwtUtil.generateRefreshToken(mockUser)).thenReturn("refresh token");

        SignInResponse response = service.signIn(dto);

        assertNotNull(response);
        assertEquals("token", response.getToken());
        assertEquals("refresh token", response.getRefreshToken());
        assertEquals("test@test.com", response.getUser().getEmail());

        verify(tokenRepository).save(argumentCaptor.capture());
        Token token = argumentCaptor.getValue();
        assertNotNull(token);
        assertEquals("refresh token", token.getRefreshToken());
        assertEquals("test@test.com", token.getUser().getEmail());
    }
    @Test
    public void test_signIn_false() {
        SignInDto dto = new SignInDto();
        dto.setEmail("test@test.com");
        dto.setPassword("password");
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        try{
            service.signIn(dto);
            fail("must thow BadRequestException");
        }catch (BadRequestException e) {
            assertTrue(e.getMessage().startsWith("User not found"));
        }
    }
    @Test
    public void test_refreshToken_false() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh token");
        when(tokenRepository.findByRefreshToken(request.getRefreshToken())).thenReturn(Optional.empty());
        assertThrows(RefreshTokenValidationException.class, () -> service.refreshToken(request));
    }
    @Test
    public void test_refreshToken_false_2() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh token");
        Token mockToken = new Token();
        mockToken.setRefreshToken("refresh token");
        mockToken.setExpiresIn("2023/10/11"); // expired
        when(tokenRepository.findByRefreshToken(request.getRefreshToken())).thenReturn(Optional.of(mockToken));
        assertThrows(BadRequestException.class, () -> service.refreshToken(request));
    }

    @Test
    public void test_refreshToken_ok() throws BadRequestException, ParseException, RefreshTokenValidationException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh token");
        Token mockToken = new Token();
        mockToken.setRefreshToken("refresh token");
        Date date = new Date(System.currentTimeMillis() + 36000000);
        mockToken.setExpiresIn(sdf.format(date));
        User mockUser = new User();
        mockUser.setEmail("test@test.com");
        mockToken.setUser(mockUser);
        when(tokenRepository.findByRefreshToken(request.getRefreshToken())).thenReturn(Optional.of(mockToken));
        when(jwtUtil.generateToken(mockUser)).thenReturn("token");
        when(jwtUtil.generateRefreshToken(mockUser)).thenReturn("refresh token");

        RefreshTokenDto dto = service.refreshToken(request);

        assertNotNull(dto);
        assertEquals("token", dto.getToken());
        assertEquals("refresh token", dto.getRefreshToken());


        assertEquals("refresh token", mockToken.getRefreshToken());
        assertEquals("test@test.com", mockToken.getUser().getEmail());
    }
}
