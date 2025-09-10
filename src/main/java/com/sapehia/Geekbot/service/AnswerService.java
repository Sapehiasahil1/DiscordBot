package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Answer;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public interface AnswerService {
    Answer addAnswer(Answer answer);
    List<Answer> getTodayAttendance(String serverId);
    long getUserAttendanceInLast30Days(String serverId, String memberId);
    List<Answer> getAllAnswer();
    List<Answer> getAnswerByMember(String id);
    List<Answer> getAnswerByDate(LocalDate date);
}
