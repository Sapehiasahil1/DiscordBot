package com.sapehia.Geekbot.service.impl;

import com.sapehia.Geekbot.model.Answer;
import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.model.Server;
import com.sapehia.Geekbot.repository.AnswerRepository;
import com.sapehia.Geekbot.service.AnswerService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;

    public AnswerServiceImpl(AnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }

    @Override
    public Answer addAnswer(Answer answer) {
        return answerRepository.save(answer);
    }

    @Override
    public int getRespondedDaysCount(String serverId, String memberId, LocalDate startDate, LocalDate endDate) {
        return answerRepository.countDistinctDaysByMemberAndServerAndDateBetween(memberId, serverId, startDate, endDate);
    }

    @Override
    public Answer getAnswerByMemberAndAssignment(String discordUserId, Long assignmentId) {
        return answerRepository.findByMemberDiscordUserIdAndAssignmentId(discordUserId, assignmentId);
    }

    @Override
    public Map<Member, List<Answer>> getAnswersGroupedByMember(Server server, LocalDate date) {
        List<Answer> answers = answerRepository.findAnswersByServerAndDate(server, date);

        return answers.stream()
                .collect(Collectors.groupingBy(Answer::getMember));
    }
}
