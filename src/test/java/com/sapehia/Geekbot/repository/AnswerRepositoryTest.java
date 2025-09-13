package com.sapehia.Geekbot.repository;

import com.sapehia.Geekbot.model.Answer;
import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.model.QuestionAssignment;
import com.sapehia.Geekbot.model.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class AnswerRepositoryTest {

//    @Autowired
    @Mock
    private TestEntityManager entityManager;

//    @Autowired
    @Mock
    private AnswerRepository answerRepository;

    private Server server1;
    private Server server2;
    private Member member1;
    private Member member2;
    private QuestionAssignment assignment1;
    private QuestionAssignment assignment2;

    @BeforeEach
    void setUp() {
        server1 = new Server();
        server1.setServerId("server-1");
        server1.setServerName("Test Server 1");
        server2 = new Server();
        server2.setServerId("server-2");
        server2.setServerName("Test Server 2");

        member1 = new Member();
        member1.setDiscordUserId("user-1");
        member1.setUsername("Test User 1");
        member1.getServers().add(server1);
        server1.getServerMembers().add(member1);

        member2 = new Member();
        member2.setDiscordUserId("user-2");
        member2.setUsername("Test User 2");
        member2.getServers().add(server1);
        server1.getServerMembers().add(member2);

        entityManager.persist(server1);
        entityManager.persist(server2);
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.flush();

        assignment1 = new QuestionAssignment();
        assignment1.setServer(server1);
        assignment1.setDate(LocalDate.now());
        assignment2 = new QuestionAssignment();
        assignment2.setServer(server2);
        assignment2.setDate(LocalDate.now());
        entityManager.persist(assignment1);
        entityManager.persist(assignment2);
        entityManager.flush();

        Answer answer1 = new Answer();
        answer1.setSubmittedAt(LocalDateTime.now().minusHours(2));
        answer1.setMember(member1);
        answer1.setAssignment(assignment1);

        Answer answer2 = new Answer();
        answer2.setSubmittedAt(LocalDateTime.now().minusHours(1));
        answer2.setMember(member2);
        answer2.setAssignment(assignment1);

        Answer answer3 = new Answer();
        answer3.setSubmittedAt(LocalDateTime.now().minusDays(1));
        answer3.setMember(member1);
        answer3.setAssignment(assignment1);

        entityManager.persist(answer1);
        entityManager.persist(answer2);
        entityManager.persist(answer3);
        entityManager.flush();
    }

    @Test
    void testFindBySubmittedDate() {
        LocalDateTime start = LocalDateTime.now().minusDays(1).minusMinutes(1);
        LocalDateTime end = LocalDateTime.now().plusMinutes(1);

        List<Answer> answers = answerRepository.findBySubmittedDate(start, end);

        assertEquals(3, answers.size());
    }

    @Test
    void testFindByServerAndDate() {
        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now().plusMinutes(1);

        List<Answer> answers = answerRepository.findByServerAndDate("server-1", start, end);

        assertEquals(2, answers.size());
        assertTrue(answers.stream().anyMatch(a -> a.getMember().getDiscordUserId().equals("user-1")));
        assertTrue(answers.stream().anyMatch(a -> a.getMember().getDiscordUserId().equals("user-2")));
    }

    @Test
    void testFindAllByServerIdAndDate() {
        LocalDate today = LocalDate.now();

        List<Answer> answers = answerRepository.findAllByServerIdAndDate("server-1", today);

        assertEquals(2, answers.size());
    }

    @Test
    void testCountAttendanceInPeriod() {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();

        long count = answerRepository.countAttendanceInPeriod("server-1", "user-1", start, end);

        assertEquals(2, count);
    }

    @Test
    void testFindByAssignmentServerServerIdAndAssignmentDateBetween() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

        List<Answer> answers = answerRepository.findByAssignmentServerServerIdAndAssignmentDateBetween("server-1", startDate, endDate);

        assertEquals(3, answers.size());
    }

    @Test
    void testCountDistinctDaysByMemberAndServerAndDateBetween() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

        int count = answerRepository.countDistinctDaysByMemberAndServerAndDateBetween("user-1", "server-1", startDate, endDate);

        assertEquals(2, count);
    }

    @Test
    void testFindByMemberDiscordUserIdAndAssignmentId() {
        Answer foundAnswer = answerRepository.findByMemberDiscordUserIdAndAssignmentId("user-1", assignment1.getId());

        assertNotNull(foundAnswer);
        assertEquals("user-1", foundAnswer.getMember().getDiscordUserId());
        assertEquals(assignment1.getId(), foundAnswer.getAssignment().getId());
    }
}