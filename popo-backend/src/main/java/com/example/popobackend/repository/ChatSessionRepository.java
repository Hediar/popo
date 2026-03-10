package com.example.popobackend.repository;

import com.example.popobackend.entity.ChatSession;
import com.example.popobackend.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findBySessionId(String sessionId);

    List<ChatSession> findByUserId(String userId);

    List<ChatSession> findByUserIdAndStatus(String userId, SessionStatus status);

    List<ChatSession> findByStatus(SessionStatus status);

    List<ChatSession> findByClientIp(String clientIp);

    List<ChatSession> findByClientIpAndStatus(String clientIp, SessionStatus status);
}