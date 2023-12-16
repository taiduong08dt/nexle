package nexle.com.entrance.service.impl;

import nexle.com.entrance.entity.Token;
import nexle.com.entrance.entity.User;
import nexle.com.entrance.exception.RefreshTokenValidationException;
import nexle.com.entrance.repository.TokenRepository;
import nexle.com.entrance.repository.UserRepository;
import nexle.com.entrance.security.JwtUtil;
import nexle.com.entrance.service.UserService;
import nexle.com.entrance.service.dto.RefreshTokenDto;
import nexle.com.entrance.service.dto.RefreshTokenRequest;
import nexle.com.entrance.service.dto.SignInDto;
import nexle.com.entrance.service.dto.SignInResponse;
import nexle.com.entrance.service.dto.SignUpDto;
import nexle.com.entrance.service.dto.UserDto;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Value("${jwt.refreshExpiration}")
    private long refreshExpirationTime; // 30 days
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    private final  SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public boolean isEmailExist(String email) {
        Optional<User> op = userRepository.findByEmail(email);
        return op.isPresent();
    }

    @Override
    public UserDto signUp(SignUpDto dto) throws BadRequestException {
        if(isEmailExist(dto.getEmail())) {
            throw new BadRequestException("account with this email is exist, email:" + dto.getEmail());
        }
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setHash(passwordEncoder.encode(dto.getPassword()));
        user.setCreatedAt(new Timestamp(new Date().getTime()));
        userRepository.save(user);
        return entityToDto(user);
    }

    @Override
    public SignInResponse signIn(SignInDto dto) throws BadRequestException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found, email:" + dto.getEmail()));
        String tokenOut = jwtUtil.generateToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // save refreshToken
        Token token = tokenRepository.findByUser(user).orElse(new Token());
        token.setUser(user);
        token.setRefreshToken(refreshToken);
        token.setCreatedAt(new Timestamp(new Date().getTime()));
        token.setUpdatedAt(new Timestamp(new Date().getTime()));
        Date date = new Date(System.currentTimeMillis() + refreshExpirationTime * 24 * 60 *60 * 1000);
        token.setExpiresIn(sdf.format(date));
        tokenRepository.save(token);

        // response
        SignInResponse response = new SignInResponse();
        response.setUser(entityToDto(user));
        response.setToken(tokenOut);
        response.setRefreshToken(refreshToken);

        return response;
    }

    @Override
    public RefreshTokenDto refreshToken(RefreshTokenRequest request) throws BadRequestException, ParseException, RefreshTokenValidationException {
        Token token = tokenRepository.findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new RefreshTokenValidationException("refreshToken does not exist, token:" + request.getRefreshToken()));
        Date now = new Date();
        Date expiresIn = sdf.parse(token.getExpiresIn());
        if(now.after(expiresIn)) {
            tokenRepository.delete(token);
            throw new BadRequestException("refreshToken has expired");
        }
        String tokenOut = jwtUtil.generateToken(token.getUser());
        String rfToken = jwtUtil.generateRefreshToken(token.getUser());

        // save refreshToken
        token.setRefreshToken(rfToken);
        token.setUpdatedAt(new Timestamp(new Date().getTime()));
        Date date = new Date(System.currentTimeMillis() + refreshExpirationTime * 24 * 60 *60 * 1000);
        token.setExpiresIn(sdf.format(date));
        tokenRepository.save(token);

        // response
        RefreshTokenDto dto = new RefreshTokenDto();
        dto.setToken(tokenOut);
        dto.setRefreshToken(rfToken);
        return dto;
    }

    private UserDto entityToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setDisplayName(user.getFirstName() + " " + user.getLastName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
