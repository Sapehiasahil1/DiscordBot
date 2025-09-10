package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Member;

import java.util.List;

public interface MemberService {
    List<Member> getAllMembers();

    Member getMemberById(String id);

    Member addMember(Member member);

    Member getMemberByDiscordUserId(String userId);

    Member getOrCreateMember(String discordUserId, String username);

}