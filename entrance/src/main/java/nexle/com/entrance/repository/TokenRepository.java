package nexle.com.entrance.repository;

import nexle.com.entrance.entity.Token;
import nexle.com.entrance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {
    Optional<Token> findByRefreshToken(String refreshToken);

    Optional<Token> findByUser(User user);
    Integer deleteByUser(User user);
}
