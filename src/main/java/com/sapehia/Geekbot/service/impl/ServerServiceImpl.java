package com.sapehia.Geekbot.service.impl;

import com.sapehia.Geekbot.model.*;
import com.sapehia.Geekbot.repository.*;
import com.sapehia.Geekbot.service.QuestionAssignmentService;
import com.sapehia.Geekbot.service.QuestionService;
import com.sapehia.Geekbot.service.ServerService;
import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Service
public class ServerServiceImpl implements ServerService {

    private final ServerRepository serverRepository;
    private final MemberRepository memberRepository;
    private final QuestionService questionService;
    private final QuestionAssignmentService questionAssignmentService;

    private JDA jda;

    public ServerServiceImpl(ServerRepository serverRepository,
                             MemberRepository memberRepository,
                             QuestionService questionService,
                             QuestionAssignmentService questionAssignmentService) {
        this.serverRepository = serverRepository;
        this.memberRepository = memberRepository;
        this.questionService = questionService;
        this.questionAssignmentService = questionAssignmentService;
    }

    @Autowired
    private void setJda(JDA jda) {
        this.jda = jda;
    }

    @Override
    public List<Member> listOfMembers(String serverId) {
        Server server = serverRepository.findById(serverId).orElseThrow();
        return server.getServerMembers();
    }

    @Override
    public Server getServerById(String serverId) {
        return serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found with id " + serverId));
    }

    @Override
    public Server addServer(Server server) {
        return serverRepository.save(server);
    }

    @Override
    public List<Server> getAllServers() {
        return serverRepository.findAll();
    }

    @Transactional
    public void addMemberToServer(String guildId, com.sapehia.Geekbot.model.Member memberEntity) {
        Server server = serverRepository.findById(guildId)
                .orElseThrow(() -> new RuntimeException("Server not found"));

        server.getServerMembers().add(memberEntity);
        memberEntity.getServers().add(server);

        serverRepository.save(server);
        memberRepository.save(memberEntity);
    }

    @Transactional
    public void registerMemberToServer(String guildId, String userId, String username) {
        Server server = serverRepository.findById(guildId)
                .orElseGet(() -> {
                    Server newServer = new Server();
                    newServer.setServerId(guildId);
                    newServer.setServerName(jda.getGuildById(guildId).getName());
                    newServer.setQuestionTime(LocalTime.of(9, 30));
                    newServer.setExcludedDays("SUNDAY"); // Set default excluded day
                    return serverRepository.save(newServer);
                });

        Member member = memberRepository.findById(userId)
                .orElseGet(() -> {
                    Member newMember = new Member();
                    newMember.setDiscordUserId(userId);
                    newMember.setUsername(username);
                    return memberRepository.save(newMember);
                });

        if (!server.getServerMembers().contains(member)) {
            server.getServerMembers().add(member);
            member.getServers().add(server);

            serverRepository.save(server);
            memberRepository.save(member);
        }
    }

    @Override
    public List<Question> getQuestionFromServer(String serverId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found with id " + serverId));

        return server.getQuestions();
    }

    @Override
    @Transactional
    public void saveServerConfig(String serverId, LocalTime sendTime, List<String> questions, Set<DayOfWeek> excludedDays) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found with id " + serverId));

        server.setQuestionTime(sendTime);

        server.setExcludedDaysFromSet(excludedDays != null ? excludedDays : Set.of(DayOfWeek.SUNDAY));

        server.getQuestions().clear();
        serverRepository.flush();

        for (String qText : questions) {
            if (qText == null || qText.isBlank()) continue;

            Question q = new Question();
            q.setText(qText);
            q.setServer(server);

            Question savedQuestion = questionService.createQuestion(q);

            QuestionAssignment assignment = new QuestionAssignment();
            assignment.setServer(server);
            assignment.setQuestion(savedQuestion);
            assignment.setDate(LocalDate.now());

            questionAssignmentService.save(assignment);
        }

        serverRepository.save(server);
    }
}