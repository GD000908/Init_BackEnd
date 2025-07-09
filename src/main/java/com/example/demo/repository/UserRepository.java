package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 로그인 시 사용자 아이디(String)로 사용자를 찾습니다.
     * @param userId 사용자가 입력한 아이디
     * @return User 객체 (Optional)
     */
    Optional<User> findByUserId(String userId);

    /**
     * 회원가입 시 사용자 아이디의 중복 여부를 확인합니다.
     * @param userId 확인할 아이디
     * @return 중복이면 true, 아니면 false
     */
    boolean existsByUserId(String userId);

    /**
     * 회원가입 시 이메일의 중복 여부를 확인합니다.
     * @param email 확인할 이메일
     * @return 중복이면 true, 아니면 false
     */
    boolean existsByEmail(String email);

    /**
     * (선택) 이메일로 사용자를 찾습니다.
     * @param email 사용자 이메일
     * @return User 객체 (Optional)
     */
    Optional<User> findByEmail(String email);
}
