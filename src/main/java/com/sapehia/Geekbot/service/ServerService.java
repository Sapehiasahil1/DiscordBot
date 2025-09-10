package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.model.Server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface ServerService {
    Set<Member> listOfMembers(String Id);
    Server getServerById(String serverId);
    Server addServer(Server server);
    List<Server> getAllServers();
    void addMemberToServer(String guildId, com.sapehia.Geekbot.model.Member memberEntity);
    void registerMemberToServer(String guildId, String userId, String username);
}
