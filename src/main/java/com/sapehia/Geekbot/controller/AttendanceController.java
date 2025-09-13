package com.sapehia.Geekbot.controller;

import com.sapehia.Geekbot.model.*;
import com.sapehia.Geekbot.service.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/attendance")
public class AttendanceController {
    private final AnswerService answerService;
    private final ServerService serverService;
    private final QuestionService questionService;
    private final MemberService memberService;
    private final QuestionAssignmentService questionAssignmentService;

    public AttendanceController (AnswerService answerService,
                                 ServerService serverService,
                                 QuestionService questionService,
                                 MemberService memberService,
                                 QuestionAssignmentService questionAssignmentService
    ) {
        this.answerService = answerService;
        this.serverService = serverService;
        this.questionService = questionService;
        this.questionAssignmentService = questionAssignmentService;
        this.memberService = memberService;
    }

    @GetMapping("/{serverId}")
    public void downloadAttendanceReport(
            @PathVariable String serverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {

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

        for (Member member : members) {
            int respondedDays = answerService.getRespondedDaysCount(serverId, member.getDiscordUserId(), startDate, endDate);
            double percentage = totalDays > 0 ? (respondedDays * 100.0 / totalDays) : 0;

            MemberAttendance attendance = new MemberAttendance(member, respondedDays, totalDays, percentage);
            memberAttendanceList.add(attendance);
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance Report");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Member Name");
        header.createCell(1).setCellValue("Responded Days");
        header.createCell(2).setCellValue("Total Days");
        header.createCell(3).setCellValue("Percentage");

        int rowNum = 1;
        for (MemberAttendance attendance : memberAttendanceList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(attendance.getMember().getUsername());
            row.createCell(1).setCellValue(attendance.getRespondedDays());
            row.createCell(2).setCellValue(attendance.getTotalDays());
            row.createCell(3).setCellValue(attendance.getPercentage());
        }

        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=attendance_report.xlsx");

        ServletOutputStream out = response.getOutputStream();
        workbook.write(out);
        workbook.close();
        out.close();
    }

    @GetMapping("/{serverId}/member/{userId}/today")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMemberTodayDetails(
            @PathVariable String serverId,
            @PathVariable String userId) {

        try {
            LocalDate today = LocalDate.now();

            Member member = memberService.getMemberByDiscordUserId(userId);
            if (member == null) {
                return ResponseEntity.notFound().build();
            }

            List<QuestionAssignment> todayAssignments = questionAssignmentService
                    .getAssignmentsByServerAndDate(serverId, today);

            List<Map<String, Object>> responses = new ArrayList<>();

            for (QuestionAssignment assignment : todayAssignments) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("questionText", assignment.getQuestion().getText());
                responseData.put("date", today.toString());

                Answer answer = answerService.getAnswerByMemberAndAssignment(userId, assignment.getId());

                if (answer != null) {
                    responseData.put("hasResponse", true);
                    responseData.put("response", answer.getResponse());
                    responseData.put("submittedAt", answer.getSubmittedAt().toString());
                } else {
                    responseData.put("hasResponse", false);
                    responseData.put("response", null);
                    responseData.put("submittedAt", null);
                }

                responses.add(responseData);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("memberName", member.getUsername());
            result.put("responses", responses);
            result.put("date", today.toString());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
