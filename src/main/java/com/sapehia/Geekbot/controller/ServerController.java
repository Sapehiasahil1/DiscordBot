package com.sapehia.Geekbot.controller;

import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.ServerConfigForm;
import com.sapehia.Geekbot.service.ServerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/server")
public class ServerController {
    private final ServerService serverService;

    public ServerController(ServerService serverService) {
        this.serverService = serverService;
    }

    @GetMapping("/{serverId}")
    public String serverHome(@PathVariable String serverId, Model model) {
        model.addAttribute("serverId", serverId);
        return "server-home";
    }

    @GetMapping("/{serverId}/configuration")
    public String serverConfig(@PathVariable String serverId, Model model) {
        List<Question> defaultQuestions = serverService.getQuestionFromServer(serverId);

        ServerConfigForm form = new ServerConfigForm();
        form.setSendTime(LocalTime.of(9, 30));
        form.setQuestions(
                defaultQuestions.stream()
                        .map(Question::getText)
                        .toList()
        );

        model.addAttribute("serverId", serverId);
        model.addAttribute("serverConfigForm", form);
        return "server-config";
    }

    @PostMapping("/{serverId}/configuration")
    public String saveServerConfig(@PathVariable String serverId,
                                   @ModelAttribute("serverConfigForm") ServerConfigForm form,
                                   RedirectAttributes redirectAttributes) {

        serverService.saveServerConfig(serverId, form.getSendTime(), form.getQuestions());

        redirectAttributes.addAttribute("serverId", serverId);
        redirectAttributes.addFlashAttribute("success", true);

        return "redirect:/server/" + serverId;
    }


}