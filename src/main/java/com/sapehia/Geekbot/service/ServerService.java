package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Answer;
import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.Server;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface ServerService {
    List<Member> listOfMembers(String serverId);
    Server getServerById(String serverId);
    Server addServer(Server server);
    List<Server> getAllServers();
    void addMemberToServer(String guildId, com.sapehia.Geekbot.model.Member memberEntity);
    void registerMemberToServer(String guildId, String userId, String username);
    Set<Member> uniqueMemberResponse(String serverId, List<Member> members);

    List<Question> getQuestionFromServer(String serverId);

    void saveServerConfig(String serverId, LocalTime sendTime, List<String> questions);
}
