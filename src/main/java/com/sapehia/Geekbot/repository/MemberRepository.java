package com.sapehia.Geekbot.repository;

import com.sapehia.Geekbot.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    Optional<Member> findByDiscordUserId(String userId);
}
