package com.example.popobackend.repository;

import com.example.popobackend.entity.ChatSession;
import com.example.popobackend.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findBySessionId(String sessionId);

    /**
     * JSONB messages 컬럼을 네이티브 쿼리로 직접 UPDATE
     * Hibernate의 JSONB dirty checking 문제를 우회
     */
    @Modifying
    @Query(value = "UPDATE chat_sessions SET messages = CAST(:messages AS jsonb), updated_at = NOW() WHERE session_id = :sessionId", nativeQuery = true)
    int updateMessagesBySessionId(@Param("sessionId") String sessionId, @Param("messages") String messages);

    List<ChatSession> findByUserId(String userId);

    List<ChatSession> findByUserIdAndStatus(String userId, SessionStatus status);

    List<ChatSession> findByStatus(SessionStatus status);

    List<ChatSession> findByClientIp(String clientIp);

    List<ChatSession> findByClientIpAndStatus(String clientIp, SessionStatus status);
}