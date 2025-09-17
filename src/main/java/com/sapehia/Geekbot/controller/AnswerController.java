package com.sapehia.Geekbot.controller;

import com.sapehia.Geekbot.model.Answer;
import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.Server;
import com.sapehia.Geekbot.service.AnswerService;
import com.sapehia.Geekbot.service.QuestionService;
import com.sapehia.Geekbot.service.ServerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AnswerController {

    private final AnswerService answerService;
    private final QuestionService questionService;
    private final ServerService serverService;

    public AnswerController(AnswerService answerService,
                            QuestionService questionService,
                            ServerService serverService) {
        this.answerService = answerService;
        this.questionService = questionService;
        this.serverService = serverService;
    }

    @GetMapping("/responses/{serverId}/details")
    public String getResponses(
            @PathVariable String serverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model
    ) {
        Server server = serverService.getServerById(serverId);
        List<Question> questions = questionService.findByServerAndDate(server, date);
        Map<Member, List<Answer>> memberAnswers = answerService.getAnswersGroupedByMember(server, date);

        Map<String, Map<Long, Answer>> memberQuestionAnswers = new HashMap<>();

        for (Map.Entry<Member, List<Answer>> entry : memberAnswers.entrySet()) {
            Member member = entry.getKey();
            List<Answer> answers = entry.getValue();

            Map<Long, Answer> answersByQuestion = new HashMap<>();
            for (Question question : questions) {
                Answer answer = answers.stream()
                        .filter(a -> a.getAssignment().getQuestion().getId() == question.getId())
                        .findFirst()
                        .orElse(null);
                answersByQuestion.put(question.getId(), answer);
            }
            memberQuestionAnswers.put(member.getDiscordUserId(), answersByQuestion);
        }

        model.addAttribute("serverId", serverId);
        model.addAttribute("date", date);
        model.addAttribute("questions", questions);
        model.addAttribute("memberAnswers", memberAnswers);
        model.addAttribute("memberQuestionAnswers", memberQuestionAnswers);

        return "member-responses";
    }
}
