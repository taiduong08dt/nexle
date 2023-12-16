package nexle.com.entrance.service;

import nexle.com.entrance.entity.User;

public interface TokenService {
    String findByToken(String token);
    Integer deleteToken(User user);
}
