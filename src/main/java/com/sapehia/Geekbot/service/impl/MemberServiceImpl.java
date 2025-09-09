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
    public Member getMemberById(long id) {
        Optional<Member> member = memberRepository.findById(id);
        return member.get();
    }
}
