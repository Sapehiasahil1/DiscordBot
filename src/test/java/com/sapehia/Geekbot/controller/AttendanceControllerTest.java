package com.sapehia.Geekbot.controller;

import com.sapehia.Geekbot.model.*;
import com.sapehia.Geekbot.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceControllerTest {

    @Mock
    private AnswerService answerService;

    @Mock
    private ServerService serverService;

    @Mock
    private QuestionService questionService;

    @Mock
    private MemberService memberService;

    @Mock
    private QuestionAssignmentService questionAssignmentService;

    @InjectMocks
    private AttendanceController attendanceController;

    private String serverId;
    private String memberId;
    private Server server;
    private Member member1;
    private Member member2;
    private Question question1;
    private Question question2;
    private QuestionAssignment assignment1;
    private QuestionAssignment assignment2;

    @BeforeEach
    void setUp() {
        serverId = "server-123";
        memberId = "user-123";

        server = new Server();
        server.setServerId(serverId);
        server.setServerName("Test Server");

        member1 = new Member();
        member1.setDiscordUserId(memberId);
        member1.setUsername("TestUser");

        member2 = new Member();
        member2.setDiscordUserId("user-456");
        member2.setUsername("AnotherUser");

        question1 = new Question();
        question1.setId(1L);
        question1.setText("Q1?");

        question2 = new Question();
        question2.setId(2L);
        question2.setText("Q2?");

        assignment1 = new QuestionAssignment();
        assignment1.setId(1L);
        assignment1.setQuestion(question1);
        assignment1.setServer(server);

        assignment2 = new QuestionAssignment();
        assignment2.setId(2L);
        assignment2.setQuestion(question2);
        assignment2.setServer(server);
    }

    @Test
    void testGetAttendanceReport_WithDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();
        Model model = new ConcurrentModel();
        List<Member> members = List.of(member1, member2);

        when(serverService.getServerById(serverId)).thenReturn(server);
        when(serverService.listOfMembers(serverId)).thenReturn(members);
        when(answerService.getRespondedDaysCount(serverId, member1.getDiscordUserId(), startDate, endDate)).thenReturn(8);
        when(answerService.getRespondedDaysCount(serverId, member2.getDiscordUserId(), startDate, endDate)).thenReturn(5);

        String viewName = attendanceController.getAttendanceReport(serverId, startDate, endDate, model);

        assertEquals("attendance-today", viewName);
        assertEquals(11, model.getAttribute("totalDays"));
        assertEquals(2, model.getAttribute("totalMembers"));
        assertEquals(server.getServerName(), model.getAttribute("serverName"));

        @SuppressWarnings("unchecked")
        List<MemberAttendance> memberAttendanceList = (List<MemberAttendance>) model.getAttribute("memberAttendance");
        assertNotNull(memberAttendanceList);
        assertEquals(2, memberAttendanceList.size());

        verify(serverService, times(1)).getServerById(serverId);
        verify(serverService, times(1)).listOfMembers(serverId);
        verify(answerService, times(1)).getRespondedDaysCount(serverId, member1.getDiscordUserId(), startDate, endDate);
        verify(answerService, times(1)).getRespondedDaysCount(serverId, member2.getDiscordUserId(), startDate, endDate);
    }

    @Test
    void testGetAttendanceReport_WithoutDateRange() {
        Model model = new ConcurrentModel();
        List<Member> members = List.of(member1);
        LocalDate expectedStartDate = LocalDate.now().minusDays(30);
        LocalDate expectedEndDate = LocalDate.now();

        when(serverService.getServerById(serverId)).thenReturn(server);
        when(serverService.listOfMembers(serverId)).thenReturn(members);
        when(answerService.getRespondedDaysCount(eq(serverId), anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(25);

        String viewName = attendanceController.getAttendanceReport(serverId, null, null, model);

        assertEquals("attendance-today", viewName);
        assertEquals(31, model.getAttribute("totalDays"));
        assertEquals(1, model.getAttribute("totalMembers"));

        verify(serverService, times(1)).getServerById(serverId);
        verify(serverService, times(1)).listOfMembers(serverId);
        verify(answerService, times(1)).getRespondedDaysCount(serverId, member1.getDiscordUserId(), expectedStartDate, expectedEndDate);
    }

    @Test
    void testGetMemberTodayDetails_Success() {
        Answer answer1 = new Answer();
        answer1.setMember(member1);
        answer1.setAssignment(assignment1);
        answer1.setResponse("Response 1");
        answer1.setSubmittedAt(LocalDateTime.now());

        List<QuestionAssignment> assignments = List.of(assignment1, assignment2);

        when(memberService.getMemberByDiscordUserId(memberId)).thenReturn(member1);
        when(questionAssignmentService.getAssignmentsByServerAndDate(serverId, LocalDate.now())).thenReturn(assignments);
        when(answerService.getAnswerByMemberAndAssignment(memberId, assignment1.getId())).thenReturn(answer1);
        when(answerService.getAnswerByMemberAndAssignment(memberId, assignment2.getId())).thenReturn(null);

        ResponseEntity<Map<String, Object>> responseEntity = attendanceController.getMemberTodayDetails(serverId, memberId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Map<String, Object> body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals(member1.getUsername(), body.get("memberName"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> responses = (List<Map<String, Object>>) body.get("responses");
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertTrue((boolean) responses.get(0).get("hasResponse"));
        assertFalse((boolean) responses.get(1).get("hasResponse"));

        verify(memberService, times(1)).getMemberByDiscordUserId(memberId);
        verify(questionAssignmentService, times(1)).getAssignmentsByServerAndDate(serverId, LocalDate.now());
        verify(answerService, times(1)).getAnswerByMemberAndAssignment(memberId, assignment1.getId());
        verify(answerService, times(1)).getAnswerByMemberAndAssignment(memberId, assignment2.getId());
    }

    @Test
    void testGetMemberTodayDetails_MemberNotFound() {
        when(memberService.getMemberByDiscordUserId(memberId)).thenReturn(null);

        ResponseEntity<Map<String, Object>> responseEntity = attendanceController.getMemberTodayDetails(serverId, memberId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        verify(memberService, times(1)).getMemberByDiscordUserId(memberId);
        verify(questionAssignmentService, never()).getAssignmentsByServerAndDate(any(), any());
    }

    @Test
    void testGetMemberTodayDetails_ExceptionThrown() {
        when(memberService.getMemberByDiscordUserId(memberId)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<Map<String, Object>> responseEntity = attendanceController.getMemberTodayDetails(serverId, memberId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        verify(memberService, times(1)).getMemberByDiscordUserId(memberId);
    }

    @Test
    void testGetTodayResponses() {
        List<Question> questions = List.of(question1, question2);
        List<Member> members = List.of(member1, member2);
        Set<Member> uniqueMembers = new HashSet<>(members);
        Model model = new ConcurrentModel();

        when(questionService.getQuestionsForServer(serverId)).thenReturn(questions);
        when(serverService.listOfMembers(serverId)).thenReturn(members);
        when(serverService.uniqueMemberResponse(serverId, members)).thenReturn(uniqueMembers);
        when(serverService.getServerById(serverId)).thenReturn(server);

        String viewName = attendanceController.getTodayResponses(serverId, model);

        assertEquals("attendance-today", viewName);
        assertEquals(questions, model.getAttribute("questions"));
        assertEquals(members, model.getAttribute("members"));
        assertEquals(uniqueMembers, model.getAttribute("answers"));
        assertEquals(server.getServerName(), model.getAttribute("serverName"));

        verify(questionService, times(1)).getQuestionsForServer(serverId);
        verify(serverService, times(1)).listOfMembers(serverId);
        verify(serverService, times(1)).uniqueMemberResponse(serverId, members);
        verify(serverService, times(1)).getServerById(serverId);
    }

    @Test
    void testGetUserAttendance() {
        long attendanceCount = 20;
        Model model = new ConcurrentModel();

        when(answerService.getUserResponsesInLast30Days(serverId, memberId)).thenReturn(attendanceCount);

        String viewName = attendanceController.getUserAttendance(memberId, serverId, model);

        assertEquals("attendance-user", viewName);
        assertEquals(memberId, model.getAttribute("memberId"));
        assertEquals(attendanceCount, model.getAttribute("attendanceCount"));

        verify(answerService, times(1)).getUserResponsesInLast30Days(serverId, memberId);
    }

    @Test
    void testGetByDate() {
        LocalDate date = LocalDate.of(2025, 10, 26);
        List<Answer> answers = List.of(new Answer(), new Answer());
        Model model = new ConcurrentModel();

        when(answerService.getAnswerByDate(date)).thenReturn(answers);

        String viewName = attendanceController.getByDate(date.toString(), model);

        assertEquals("attendance-date", viewName);
        assertEquals(answers, model.getAttribute("answers"));
        assertEquals(date.toString(), model.getAttribute("date"));

        verify(answerService, times(1)).getAnswerByDate(date);
    }
}