package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Answer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AnswerService {
    Answer addAnswer(Answer answer);
    List<Answer> getTodayMembersResponse(String serverId);
    long getUserResponsesInLast30Days(String serverId, String memberId);
    List<Answer> getAllAnswer();
    List<Answer> getAnswerByMember(String id);
    List<Answer> getAnswerByDate(LocalDate date);

    List<Answer> getAnswersByDateRange(String serverId, LocalDate startDate, LocalDate endDate);

    int getRespondedDaysCount(String serverId, String discordUserId, LocalDate startDate, LocalDate endDate);

    String findResponseForMemberQuestionOnDate(String serverId, String memberId, long id, LocalDate today);

    Answer getAnswerByMemberAndAssignment(String userId, Long id);
    Map<String, List<Answer>> getMemberAnswersByServerAndDate(String serverId, LocalDate date);
}
