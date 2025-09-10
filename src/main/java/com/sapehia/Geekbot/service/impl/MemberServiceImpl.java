package com.sapehia.Geekbot.service.impl;

import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.repository.MemberRepository;
import com.sapehia.Geekbot.service.MemberService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Override
    public Member getMemberById(String id) {
        Optional<Member> member = memberRepository.findById(id);
        return member.get();
    }

    @Override
    public Member addMember(Member member) {
        return memberRepository.save(member);
    }

    @Override
    public Member getMemberByDiscordUserId(String userId) {
        return memberRepository.findByDiscordUserId(userId)
                .orElseThrow(()-> new RuntimeException("Member not found with discordUserId " + userId));

    }

    public Member getOrCreateMember(String discordUserId, String username) {
        return memberRepository.findByDiscordUserId(discordUserId)
                .orElseGet(() -> {
                    com.sapehia.Geekbot.model.Member m = new com.sapehia.Geekbot.model.Member();
                    m.setDiscordUserId(discordUserId);
                    m.setUsername(username);
                    return memberRepository.save(m);
                });
    }
}
