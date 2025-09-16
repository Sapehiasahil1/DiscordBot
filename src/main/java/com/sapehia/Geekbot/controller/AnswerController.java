package com.sapehia.Geekbot.controller;

import com.sapehia.Geekbot.model.Answer;
import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.service.AnswerService;
import com.sapehia.Geekbot.service.QuestionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class AnswerController {

    private final AnswerService answerService;
    private final QuestionService questionService;

    public AnswerController(AnswerService answerService, QuestionService questionService) {
        this.answerService = answerService;
        this.questionService = questionService;
    }

    @GetMapping("/responses/{serverId}/details")
    public String getServerMembersAnswers(@PathVariable String serverId,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                          Model model) {
        if(date == null) {
            date = LocalDate.now();
        }

        Map<String, List<Answer>> memberAnswers = answerService
                .getMemberAnswersByServerAndDate(serverId, date);

        List<Question> questions = questionService.getQuestionsForServer(serverId);

        model.addAttribute("serverId", serverId);
        model.addAttribute("date", date);
        model.addAttribute("questions", questions);
        model.addAttribute("memberAnswers", memberAnswers);

        return "member-responses";
    }
}
