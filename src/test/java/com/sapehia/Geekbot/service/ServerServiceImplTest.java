package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Answer;
import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.QuestionAssignment;
import com.sapehia.Geekbot.model.Server;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(serverService, "jda", jda);

        server = new Server();
        server.setServerId("guild-1");
        server.setServerName("Test Server");
        server.setQuestionTime(LocalTime.of(9, 30));

        server.setServerMembers(new ArrayList<>());
        server.setQuestions(new ArrayList<>());

        member = new Member();
        member.setDiscordUserId("user-1");
        member.setUsername("testUser");

        question = new Question();
        question.setId(1L);
        question.setText("Test Question?");
    }

    @Test
    void testListOfMembers_Found() {
        server.getServerMembers().add(member);
        when(serverRepository.findById("guild-1")).thenReturn(Optional.of(server));

        List<Member> members = serverService.listOfMembers("guild-1");

        assertNotNull(members);
        assertEquals(1, members.size());
        assertTrue(members.contains(member));
        verify(serverRepository, times(1)).findById("guild-1");
    }

    @Test
    void testListOfMembers_NotFound() {
        when(serverRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class, () -> serverService.listOfMembers("nonexistent"));
        verify(serverRepository, times(1)).findById("nonexistent");
    }

    @Test
    void testGetServerById_Found() {
        when(serverRepository.findById("guild-1")).thenReturn(Optional.of(server));

        Server foundServer = serverService.getServerById("guild-1");

        assertNotNull(foundServer);
        assertEquals("guild-1", foundServer.getServerId());
        verify(serverRepository, times(1)).findById("guild-1");
    }

    @Test
    void testGetServerById_NotFound() {
        when(serverRepository.findById("nonexistent")).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> serverService.getServerById("nonexistent"));
        assertEquals("Server not found with id nonexistent", thrown.getMessage());
        verify(serverRepository, times(1)).findById("nonexistent");
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
        when(serverRepository.findAll()).thenReturn(List.of(server));

        List<Server> servers = serverService.getAllServers();

        assertNotNull(servers);
        assertEquals(1, servers.size());
        verify(serverRepository, times(1)).findAll();
    }

    @Test
    void testAddMemberToServer() {
        when(serverRepository.findById("guild-1")).thenReturn(Optional.of(server));
        when(serverRepository.save(any(Server.class))).thenReturn(server);
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        serverService.addMemberToServer("guild-1", member);

        assertTrue(server.getServerMembers().contains(member));
        verify(serverRepository, times(1)).findById("guild-1");
        verify(serverRepository, times(1)).save(any(Server.class));
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void testAddMemberToServer_NotFound() {
        when(serverRepository.findById("nonexistent")).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> serverService.addMemberToServer("nonexistent", member));
        assertEquals("Server not found", thrown.getMessage());
        verify(serverRepository, times(1)).findById("nonexistent");
        verify(serverRepository, never()).save(any(Server.class));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void testRegisterMemberToServer_ServerAndMemberExist() {
        when(serverRepository.findById("guild-1")).thenReturn(Optional.of(server));
        when(memberRepository.findById("user-1")).thenReturn(Optional.of(member));
        server.getServerMembers().add(member);

        serverService.registerMemberToServer("guild-1", "user-1", "testUser");

        verify(serverRepository, times(1)).findById("guild-1");
        verify(memberRepository, times(1)).findById("user-1");
        verify(serverRepository, never()).save(any(Server.class));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void testRegisterMemberToServer_ServerExistsMemberNot() {
        when(serverRepository.findById("guild-1")).thenReturn(Optional.of(server));
        when(memberRepository.findById("user-2")).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));

        serverService.registerMemberToServer("guild-1", "user-2", "newUser");

        verify(serverRepository, times(1)).findById("guild-1");
        verify(memberRepository, times(1)).findById("user-2");
        verify(memberRepository, times(2)).save(any(Member.class));
        verify(serverRepository, times(1)).save(any(Server.class));
    }

    @Test
    void testRegisterMemberToServer_ServerAndMemberNotExist() {
        when(serverRepository.findById("guild-2")).thenReturn(Optional.empty());
        when(memberRepository.findById("user-2")).thenReturn(Optional.empty());
        when(jda.getGuildById("guild-2")).thenReturn(guild);
        when(guild.getName()).thenReturn("New Server");
        when(serverRepository.save(any(Server.class))).thenAnswer(i -> i.getArgument(0));
        when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));

        serverService.registerMemberToServer("guild-2", "user-2", "newUser");

        verify(serverRepository, times(1)).findById("guild-2");
        verify(memberRepository, times(1)).findById("user-2");
        verify(serverRepository, times(2)).save(any(Server.class));
        verify(memberRepository, times(2)).save(any(Member.class));
    }

    @Test
    void testUniqueMemberResponse() {
        String serverId = "guild-1";
        Member member2 = new Member();
        member2.setDiscordUserId("user-2");

        Answer answer1 = new Answer();
        answer1.setMember(member);
        Answer answer2 = new Answer();
        answer2.setMember(member2);
        Answer answer3 = new Answer();
        answer3.setMember(member);

        List<Answer> mockAnswers = List.of(answer1, answer2, answer3);
        when(answerService.getTodayMembersResponse(serverId)).thenReturn(mockAnswers);

        Set<Member> uniqueMembers = serverService.uniqueMemberResponse(serverId, List.of(member, member2));

        assertNotNull(uniqueMembers);
        assertEquals(2, uniqueMembers.size());
        assertTrue(uniqueMembers.contains(member));
        assertTrue(uniqueMembers.contains(member2));
        verify(answerService, times(1)).getTodayMembersResponse(serverId);
    }

    @Test
    void testGetQuestionFromServer_Found() {
        server.getQuestions().add(question);
        when(serverRepository.findById("guild-1")).thenReturn(Optional.of(server));

        List<Question> foundQuestions = serverService.getQuestionFromServer("guild-1");

        assertNotNull(foundQuestions);
        assertEquals(1, foundQuestions.size());
        assertEquals(question.getId(), foundQuestions.getFirst().getId());
        verify(serverRepository, times(1)).findById("guild-1");
    }

    @Test
    void testGetQuestionFromServer_NotFound() {
        when(serverRepository.findById("nonexistent")).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> serverService.getQuestionFromServer("nonexistent")
        );

        assertEquals("Server not found with id nonexistent", thrown.getMessage());
        verify(serverRepository, times(1)).findById("nonexistent");
    }

    @Test
    void testSaveServerConfig_Success() {
        LocalTime sendTime = LocalTime.of(10, 0);
        List<String> questions = List.of("Q1", "Q2", "Q3");

        when(serverRepository.findById("guild-1")).thenReturn(Optional.of(server));

        when(serverRepository.save(any(Server.class))).thenAnswer(i -> {
            return i.<Server>getArgument(0);
        });

        when(questionService.createQuestion(any(Question.class))).thenAnswer(i -> {
            Question q = i.getArgument(0);
            q.setId(Math.round(Math.random() * 1000));
            return q;
        });
        when(questionAssignmentService.save(any(QuestionAssignment.class))).thenAnswer(i -> i.getArgument(0));

        serverService.saveServerConfig("guild-1", sendTime, questions);

        assertEquals(sendTime, server.getQuestionTime());
        assertEquals(questions.size(), server.getQuestions().size());

        verify(serverRepository, times(1)).findById("guild-1");
        verify(serverRepository, times(1)).save(server);
        verify(questionService, times(questions.size())).createQuestion(any(Question.class));
        verify(questionAssignmentService, times(questions.size())).save(any(QuestionAssignment.class));
    }

    @Test
    void testSaveServerConfig_ServerNotFound() {
        when(serverRepository.findById("nonexistent")).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> serverService.saveServerConfig("nonexistent", LocalTime.now(), List.of("Q1"))
        );

        assertEquals("Server not found with id nonexistent", thrown.getMessage());
        verify(serverRepository, times(1)).findById("nonexistent");
        verify(serverRepository, never()).save(any(Server.class));
        verify(questionService, never()).createQuestion(any(Question.class));
    }
}