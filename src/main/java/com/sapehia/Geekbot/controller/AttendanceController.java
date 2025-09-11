package com.sapehia.Geekbot.controller;

import com.sapehia.Geekbot.model.Answer;
import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.service.QuestionService;
import com.sapehia.Geekbot.service.ServerService;
import org.springframework.ui.Model;
import com.sapehia.Geekbot.service.AnswerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {
    private final AnswerService answerService;
    private final ServerService serverService;
    private final QuestionService questionService;

    public AttendanceController (AnswerService answerService,
                                 ServerService serverService,
                                 QuestionService questionService) {
        this.answerService = answerService;
        this.serverService = serverService;
        this.questionService = questionService;
    }

    @GetMapping("/today/{serverId}")
    public String getTodayResponses(@PathVariable String serverId, Model model) {
        List<Question> questionList = questionService.getQuestionsForServer(serverId);
        List<Member> members = serverService.listOfMembers(serverId);
        Set<Member> uniqueMemberResponse = serverService.uniqueMemberResponse(serverId, members);

        model.addAttribute("members", members);
        model.addAttribute("questions", questionList);
        model.addAttribute("answers", uniqueMemberResponse);
        model.addAttribute("serverName", serverService.getServerById(serverId).getServerName());
        model.addAttribute("serverId", serverId);

        return "attendance-today";
    }

    @GetMapping("/{serverId}/user/{memberId}")
    public String getUserAttendance(@PathVariable String memberId,
                                    @PathVariable String serverId,
                                    Model model) {
        long attendanceCount = answerService.getUserResponsesInLast30Days(serverId, memberId);
        model.addAttribute("memberId", memberId);
        model.addAttribute("attendanceCount", attendanceCount);

        return "attendance-user";
    }

    @GetMapping("/date/{date}")
    public String getByDate(@PathVariable String date, Model model) {
        List<Answer> answers = answerService.getAnswerByDate(LocalDate.parse(date));
        model.addAttribute("answers", answers);
        model.addAttribute("date", date);

        return "attendance-date";
    }
}
