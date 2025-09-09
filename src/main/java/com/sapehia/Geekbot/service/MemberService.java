package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Member;

import java.util.List;

public interface MemberService {
    public List<Member> getAllMembers();
    public Member getMemberById(long id);
}