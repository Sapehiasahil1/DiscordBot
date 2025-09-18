package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Member;

import java.util.List;

public interface MemberService {
    Member getMemberByDiscordUserId(String userId);
}