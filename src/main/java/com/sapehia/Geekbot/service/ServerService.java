package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.Server;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public interface ServerService {

    List<Member> listOfMembers(String serverId);

    Server getServerById(String serverId);

    Server addServer(Server server);

    List<Server> getAllServers();

    void registerMemberToServer(String guildId, String userId, String username);

    List<Question> getQuestionFromServer(String serverId);

    void saveServerConfig(String serverId, LocalTime sendTime, List<String> questions, Set<DayOfWeek> excludedDays);
}