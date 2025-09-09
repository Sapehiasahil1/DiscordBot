package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Answer;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public interface AnswerService {
    public List<Answer> getAllAnswer();
    public List<Answer> getAnswerByMember(long id);
    public List<Answer> getAnswerByDate(LocalDate date);
}
