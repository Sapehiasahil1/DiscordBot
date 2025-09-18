package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Answer;
import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.model.Server;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AnswerService {
    Answer addAnswer(Answer answer);

    int getRespondedDaysCount(String serverId, String discordUserId, LocalDate startDate, LocalDate endDate);

    Answer getAnswerByMemberAndAssignment(String userId, Long id);

    Map<Member, List<Answer>> getAnswersGroupedByMember(Server server, LocalDate date);
}
