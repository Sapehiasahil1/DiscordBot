package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Member;

import java.util.List;

public interface ServerService {
    public List<Member> listOfMembers(String id);
}
