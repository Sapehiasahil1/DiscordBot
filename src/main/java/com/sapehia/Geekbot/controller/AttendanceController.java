package com.sapehia.Geekbot.controller;

import com.sapehia.Geekbot.model.*;
import com.sapehia.Geekbot.service.QuestionService;
import com.sapehia.Geekbot.service.ServerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.ui.Model;
import com.sapehia.Geekbot.service.AnswerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @GetMapping("/{serverId}")
    public String getAttendanceReport(
            @PathVariable String serverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Server server = serverService.getServerById(serverId);
        List<Member> members = serverService.listOfMembers(serverId);

        List<MemberAttendance> memberAttendanceList = new ArrayList<>();
        int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double totalAttendancePercentage = 0;

        for (Member member : members) {
            int respondedDays = answerService.getRespondedDaysCount(serverId, member.getDiscordUserId(), startDate, endDate);

            double percentage = totalDays > 0 ? (respondedDays * 100.0 / totalDays) : 0;
            totalAttendancePercentage += percentage;

            MemberAttendance attendance = new MemberAttendance(member, respondedDays, totalDays, percentage);
            memberAttendanceList.add(attendance);
        }

        double avgAttendance = members.isEmpty() ? 0 : totalAttendancePercentage / members.size();

        model.addAttribute("memberAttendance", memberAttendanceList);
        model.addAttribute("totalMembers", members.size());
        model.addAttribute("totalDays", totalDays);
        model.addAttribute("avgAttendance", avgAttendance);
        model.addAttribute("serverName", server.getServerName());
        model.addAttribute("serverId", serverId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "attendance-today";
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
