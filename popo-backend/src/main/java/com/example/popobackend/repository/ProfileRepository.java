package com.example.popobackend.repository;

import com.example.popobackend.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    /**
     * 활성화된 프로필 조회 (단일)
     */
    Optional<Profile> findFirstByIsActiveTrue();
}
