package com.sapehia.Geekbot.service.impl;

import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.repository.MemberRepository;
import com.sapehia.Geekbot.service.MemberService;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Member getMemberByDiscordUserId(String userId) {
        return memberRepository.findByDiscordUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found with discordUserId " + userId));

    }
}
