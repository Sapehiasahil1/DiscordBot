package com.sapehia.Geekbot.controller;

import com.sapehia.Geekbot.model.Answer;
import org.springframework.ui.Model;
import com.sapehia.Geekbot.service.AnswerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {
    private final AnswerService answerService;

    public AttendanceController (AnswerService answerService) {
        this.answerService = answerService;
    }

    @GetMapping("/today/{serverId}")
    public String getTodayAttendance(@PathVariable String serverId, Model model) {
        List<Answer> answers = answerService.getTodayAttendance(serverId);
        model.addAttribute("answers", answers);
        model.addAttribute("serverId", serverId);

        return "attendance-today";
    }

    @GetMapping("/{serverId}/user/{memberId}")
    public String getUserAttendance(@PathVariable String memberId,
                                    @PathVariable String serverId,
                                    Model model) {
        long attendanceCount = answerService.getUserAttendanceInLast30Days(serverId, memberId);
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
