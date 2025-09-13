package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.*;
import com.sapehia.Geekbot.repository.MemberRepository;
import com.sapehia.Geekbot.repository.ServerRepository;
import com.sapehia.Geekbot.service.impl.ServerServiceImpl;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerServiceImplTest {

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JDA jda;

    @Mock
    private Guild guild;

    @Mock
    private AnswerService answerService;

    @Mock
    private QuestionService questionService;

    @Mock
    private QuestionAssignmentService questionAssignmentService;

    @InjectMocks
    private ServerServiceImpl serverService;

    private Server server;
    private Member member;
    private Question question;
    private String serverId;
    private String memberId;
    private String username;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(serverService, "jda", jda);

        serverId = "guild-1";
        memberId = "user-1";
        username = "testUser";

        server = new Server();
        server.setServerId(serverId);
        server.setServerName("Test Server");
        server.setQuestionTime(LocalTime.of(9, 30));

        server.setServerMembers(new ArrayList<>());
        server.setQuestions(new ArrayList<>());
        server.setExcludedDays("SUNDAY");

        member = new Member();
        member.setDiscordUserId(memberId);
        member.setUsername(username);

        question = new Question();
        question.setId(1L);
        question.setText("Test Question?");
    }

    @Test
    void testListOfMembers_Success() {
        server.getServerMembers().add(member);
        when(serverRepository.findById(serverId)).thenReturn(Optional.of(server));

        List<Member> members = serverService.listOfMembers(serverId);

        assertNotNull(members);
        assertEquals(1, members.size());
        assertEquals(member.getDiscordUserId(), members.get(0).getDiscordUserId());
        verify(serverRepository, times(1)).findById(serverId);
    }

    @Test
    void testListOfMembers_ServerNotFound_ThrowsException() {
        when(serverRepository.findById(serverId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> serverService.listOfMembers(serverId));
        verify(serverRepository, times(1)).findById(serverId);
    }

    @Test
    void testGetServerById_Success() {
        when(serverRepository.findById(serverId)).thenReturn(Optional.of(server));

        Server foundServer = serverService.getServerById(serverId);

        assertNotNull(foundServer);
        assertEquals(serverId, foundServer.getServerId());
        verify(serverRepository, times(1)).findById(serverId);
    }

    @Test
    void testGetServerById_ServerNotFound_ThrowsException() {
        when(serverRepository.findById(serverId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> serverService.getServerById(serverId));
        verify(serverRepository, times(1)).findById(serverId);
    }

    @Test
    void testAddServer() {
        when(serverRepository.save(server)).thenReturn(server);

        Server savedServer = serverService.addServer(server);

        assertNotNull(savedServer);
        assertEquals(server.getServerId(), savedServer.getServerId());
        verify(serverRepository, times(1)).save(server);
    }

    @Test
    void testGetAllServers() {
        List<Server> serverList = List.of(server, new Server());
        when(serverRepository.findAll()).thenReturn(serverList);

        List<Server> servers = serverService.getAllServers();

        assertNotNull(servers);
        assertEquals(2, servers.size());
        verify(serverRepository, times(1)).findAll();
    }

    @Test
    void testAddMemberToServer_ServerNotFound_ThrowsException() {
        when(serverRepository.findById(serverId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> serverService.addMemberToServer(serverId, member));
        verify(serverRepository, times(1)).findById(serverId);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void testRegisterMemberToServer_NewMemberAndServer() {
        when(serverRepository.findById(serverId)).thenReturn(Optional.empty());
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        Server newServer = new Server();
        newServer.setServerId(serverId);
        when(jda.getGuildById(serverId)).thenReturn(guild);
        when(guild.getName()).thenReturn("New Server");
        when(serverRepository.save(any(Server.class))).thenReturn(newServer);

        Member newMember = new Member();
        newMember.setDiscordUserId(memberId);
        when(memberRepository.save(any(Member.class))).thenReturn(newMember);

        serverService.registerMemberToServer(serverId, memberId, username);

        verify(serverRepository, times(2)).save(any(Server.class));
        verify(memberRepository, times(2)).save(any(Member.class));
    }

    @Test
    void testRegisterMemberToServer_ExistingMemberAndServer() {
        server.getServerMembers().add(member);
        member.getServers().add(server);

        when(serverRepository.findById(serverId)).thenReturn(Optional.of(server));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        serverService.registerMemberToServer(serverId, memberId, username);

        verify(serverRepository, times(1)).findById(serverId);
        verify(memberRepository, times(1)).findById(memberId);
        verify(serverRepository, never()).save(any(Server.class));
        verify(memberRepository, never()).save(any(Member.class));
    }


    @Test
    void testUniqueMemberResponse() {
        List<Member> members = List.of(member);
        List<Answer> answers = List.of(new Answer(), new Answer());
        answers.get(0).setMember(member);
        answers.get(1).setMember(member);

        when(answerService.getTodayMembersResponse(serverId)).thenReturn(answers);

        Set<Member> uniqueMembers = serverService.uniqueMemberResponse(serverId, members);

        assertNotNull(uniqueMembers);
        assertEquals(1, uniqueMembers.size());
        assertTrue(uniqueMembers.contains(member));
        verify(answerService, times(1)).getTodayMembersResponse(serverId);
    }


    @Test
    void testGetQuestionFromServer_Success() {
        server.getQuestions().add(question);
        when(serverRepository.findById(serverId)).thenReturn(Optional.of(server));

        List<Question> questions = serverService.getQuestionFromServer(serverId);

        assertNotNull(questions);
        assertEquals(1, questions.size());
        assertEquals(question.getText(), questions.getFirst().getText());
        verify(serverRepository, times(1)).findById(serverId);
    }

    @Test
    void testGetQuestionFromServer_ServerNotFound_ThrowsException() {
        when(serverRepository.findById(serverId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> serverService.getQuestionFromServer(serverId));
        verify(serverRepository, times(1)).findById(serverId);
    }


    @Test
    void testSaveServerConfig_Success() {
        LocalTime sendTime = LocalTime.of(10, 0);
        List<String> questions = List.of("Q1", "Q2", "Q3");
        Set<DayOfWeek> excludedDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY);

        when(serverRepository.findById(serverId)).thenReturn(Optional.of(server));
        when(serverRepository.save(any(Server.class))).thenAnswer(i -> i.getArgument(0));
        when(questionService.createQuestion(any(Question.class))).thenAnswer(i -> {
            Question q = i.getArgument(0);
            q.setId(Math.round(Math.random() * 1000));
            return q;
        });

        serverService.saveServerConfig(serverId, sendTime, questions, excludedDays);

        assertEquals(sendTime, server.getQuestionTime());
        assertEquals(excludedDays, server.getExcludedDaysAsSet());
        assertEquals(0, server.getQuestions().size());

        verify(serverRepository, times(1)).findById(serverId);
        verify(serverRepository, times(1)).flush();
        verify(serverRepository, times(1)).save(server);
        verify(questionService, times(questions.size())).createQuestion(any(Question.class));
        verify(questionAssignmentService, times(questions.size())).save(any(QuestionAssignment.class));
    }


    @Test
    void testUpdateServerExcludedDays_Success() {
        Set<DayOfWeek> newExcludedDays = Set.of(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
        when(serverRepository.findById(serverId)).thenReturn(Optional.of(server));
        when(serverRepository.save(any(Server.class))).thenAnswer(i -> i.getArgument(0));

        serverService.updateServerExcludedDays(serverId, newExcludedDays);

        assertEquals(newExcludedDays, server.getExcludedDaysAsSet());
        verify(serverRepository, times(1)).findById(serverId);
        verify(serverRepository, times(1)).save(server);
    }

    @Test
    void testUpdateServerExcludedDays_ServerNotFound_ThrowsException() {
        Set<DayOfWeek> newExcludedDays = Set.of(DayOfWeek.SUNDAY);
        when(serverRepository.findById(serverId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> serverService.updateServerExcludedDays(serverId, newExcludedDays));
        verify(serverRepository, times(1)).findById(serverId);
        verify(serverRepository, never()).save(any(Server.class));
    }

    @Test
    void testGetServerExcludedDays_Success() {
        Set<DayOfWeek> excludedDays = Set.of(DayOfWeek.SUNDAY, DayOfWeek.MONDAY);
        server.setExcludedDaysFromSet(excludedDays);
        when(serverRepository.findById(serverId)).thenReturn(Optional.of(server));

        Set<DayOfWeek> returnedDays = serverService.getServerExcludedDays(serverId);

        assertEquals(excludedDays, returnedDays);
        verify(serverRepository, times(1)).findById(serverId);
    }

    @Test
    void testGetServerExcludedDays_ServerNotFound_ThrowsException() {
        when(serverRepository.findById(serverId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> serverService.getServerExcludedDays(serverId));
        verify(serverRepository, times(1)).findById(serverId);
    }
}