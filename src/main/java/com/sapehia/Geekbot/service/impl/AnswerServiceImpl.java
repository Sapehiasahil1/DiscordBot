package com.sapehia.Geekbot.service.impl;

import com.sapehia.Geekbot.model.Answer;
import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.repository.AnswerRepository;
import com.sapehia.Geekbot.service.AnswerService;
import com.sapehia.Geekbot.service.MemberService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnswerServiceImpl implements AnswerService{

    private final AnswerRepository answerRepository;
    private final MemberService memberService;

    public AnswerServiceImpl(AnswerRepository answerRepository, MemberService memberService) {
        this.answerRepository = answerRepository;
        this.memberService = memberService;
    }

    @Override
    public Answer addAnswer(Answer answer) {
        return answerRepository.save(answer);
    }

    @Override
    public List<Answer> getTodayAttendance(String serverId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return answerRepository.findByServerAndDate(serverId, startOfDay, endOfDay);
    }

    @Override
    public long getUserAttendanceInLast30Days(String serverId, String memberId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(30);
        return answerRepository.countAttendanceInPeriod(serverId, memberId, start, now);
    }

    @Override
    public List<Answer> getAllAnswer() {
        return answerRepository.findAll();
    }

    @Override
    public List<Answer> getAnswerByMember(String id) {
        Member member = memberService.getMemberById(id);
        return member.getAnswers();
    }

    @Override
    public List<Answer> getAnswerByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return answerRepository.findBySubmittedDate(startOfDay, endOfDay);
    }
}
