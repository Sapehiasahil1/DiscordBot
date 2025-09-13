package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Answer;
import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.repository.AnswerRepository;
import com.sapehia.Geekbot.service.impl.AnswerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnswerServiceImplTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private AnswerServiceImpl answerService;

    private Answer answer1;
    private Answer answer2;
    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setDiscordUserId("member-1");

        answer1 = new Answer();
        answer1.setId(1L);
        answer1.setSubmittedAt(LocalDateTime.now());
        answer1.setMember(member);

        answer2 = new Answer();
        answer2.setId(2L);
        answer2.setSubmittedAt(LocalDateTime.now());
        answer2.setMember(member);
    }

    @Test
    void testAddAnswer() {
        when(answerRepository.save(answer1)).thenReturn(answer1);

        Answer savedAnswer = answerService.addAnswer(answer1);

        assertEquals(answer1, savedAnswer);
        verify(answerRepository, times(1)).save(answer1);
    }

    @Test
    void testGetTodayMembersResponse() {
        String serverId = "server-123";
        LocalDate today = LocalDate.now();
        List<Answer> todayAnswers = List.of(answer1, answer2);

        when(answerRepository.findAllByServerIdAndDate(serverId, today)).thenReturn(todayAnswers);

        List<Answer> foundAnswers = answerService.getTodayMembersResponse(serverId);

        assertEquals(2, foundAnswers.size());
        assertEquals(todayAnswers, foundAnswers);
        verify(answerRepository, times(1)).findAllByServerIdAndDate(serverId, today);
    }

    @Test
    void testGetUserResponsesInLast30Days() {
        String serverId = "server-123";
        String memberId = "member-1";
        long expectedCount = 5;

        // Using ArgumentMatchers to handle dynamic LocalDateTime values
        when(answerRepository.countAttendanceInPeriod(eq(serverId), eq(memberId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expectedCount);

        long count = answerService.getUserResponsesInLast30Days(serverId, memberId);

        assertEquals(expectedCount, count);
        verify(answerRepository, times(1)).countAttendanceInPeriod(eq(serverId), eq(memberId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetAllAnswer() {
        List<Answer> answers = List.of(answer1, answer2);

        when(answerRepository.findAll()).thenReturn(answers);

        List<Answer> foundAnswers = answerService.getAllAnswer();

        assertEquals(2, foundAnswers.size());
        assertEquals(answers, foundAnswers);
        verify(answerRepository, times(1)).findAll();
    }

    @Test
    void testGetAnswerByMember() {
        List<Answer> memberAnswers = new ArrayList<>();
        memberAnswers.add(answer1);
        memberAnswers.add(answer2);
        member.setAnswers(memberAnswers);

        when(memberService.getMemberById("member-1")).thenReturn(member);

        List<Answer> foundAnswers = answerService.getAnswerByMember("member-1");

        assertEquals(2, foundAnswers.size());
        assertEquals(memberAnswers, foundAnswers);
        verify(memberService, times(1)).getMemberById("member-1");
    }

    @Test
    void testGetAnswerByMember_NoAnswers() {
        member.setAnswers(Collections.emptyList());
        when(memberService.getMemberById("member-1")).thenReturn(member);

        List<Answer> foundAnswers = answerService.getAnswerByMember("member-1");

        assertEquals(0, foundAnswers.size());
        assertEquals(Collections.emptyList(), foundAnswers);
    }

    @Test
    void testGetAnswerByDate() {
        LocalDate testDate = LocalDate.now();
        List<Answer> answersOnDate = List.of(answer1, answer2);

        when(answerRepository.findBySubmittedDate(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(answersOnDate);

        List<Answer> foundAnswers = answerService.getAnswerByDate(testDate);

        assertEquals(2, foundAnswers.size());
        assertEquals(answersOnDate, foundAnswers);
        verify(answerRepository, times(1)).findBySubmittedDate(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testGetAnswersByDateRange() {
        String serverId = "server-123";
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 10);
        List<Answer> answersInRange = List.of(answer1, answer2);

        when(answerRepository.findByAssignmentServerServerIdAndAssignmentDateBetween(serverId, startDate, endDate)).thenReturn(answersInRange);

        List<Answer> foundAnswers = answerService.getAnswersByDateRange(serverId, startDate, endDate);

        assertEquals(2, foundAnswers.size());
        assertEquals(answersInRange, foundAnswers);
        verify(answerRepository, times(1)).findByAssignmentServerServerIdAndAssignmentDateBetween(serverId, startDate, endDate);
    }

    @Test
    void testGetRespondedDaysCount() {
        String serverId = "server-123";
        String memberId = "member-1";
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 10);
        int expectedCount = 8;

        when(answerRepository.countDistinctDaysByMemberAndServerAndDateBetween(memberId, serverId, startDate, endDate))
                .thenReturn(expectedCount);

        int count = answerService.getRespondedDaysCount(serverId, memberId, startDate, endDate);

        assertEquals(expectedCount, count);
        verify(answerRepository, times(1)).countDistinctDaysByMemberAndServerAndDateBetween(memberId, serverId, startDate, endDate);
    }

    @Test
    void testGetAnswerByMemberAndAssignment() {
        String memberId = "member-1";
        Long assignmentId = 1L;

        when(answerRepository.findByMemberDiscordUserIdAndAssignmentId(memberId, assignmentId)).thenReturn(answer1);

        Answer foundAnswer = answerService.getAnswerByMemberAndAssignment(memberId, assignmentId);

        assertNotNull(foundAnswer);
        assertEquals(answer1, foundAnswer);
        verify(answerRepository, times(1)).findByMemberDiscordUserIdAndAssignmentId(memberId, assignmentId);
    }
}