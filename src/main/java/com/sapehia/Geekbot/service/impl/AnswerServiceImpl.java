package com.sapehia.Geekbot.service.impl;

import com.sapehia.Geekbot.model.Answer;
import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.repository.AnswerRepository;
import com.sapehia.Geekbot.service.AnswerService;
import com.sapehia.Geekbot.service.MemberService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        List<Answer> answers = answerRepository.findBySubmittedDate(date);
        return answers;
    }
}
