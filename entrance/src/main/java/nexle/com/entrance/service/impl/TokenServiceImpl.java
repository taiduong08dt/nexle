package nexle.com.entrance.service.impl;

import jakarta.transaction.Transactional;
import nexle.com.entrance.entity.User;
import nexle.com.entrance.repository.TokenRepository;
import nexle.com.entrance.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenServiceImpl implements TokenService {
    @Autowired
    private TokenRepository tokenRepository;

    @Override
    public String findByToken(String token) {
        return null;
    }

    @Override
    @Transactional
    public Integer deleteToken(User user) {
        return tokenRepository.deleteByUser(user);
    }
}
