package com.sapehia.Geekbot.controller;

import com.sapehia.Geekbot.model.Question;
import com.sapehia.Geekbot.model.ServerConfigForm;
import com.sapehia.Geekbot.service.ServerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        List<Question> defaultQuestions = serverService.getQuestionFromServer;

        model.addAttribute("serverId", serverId);
        model.addAttribute("questions", new ArrayList<>(defaultQuestions));
        model.addAttribute("sendTime", "09:30");
        return "server-config";
    }

    @PostMapping("/{serverId}/configuration")
    public String saveServerConfig(@PathVariable String serverId,
                                   @ModelAttribute("configForm") ServerConfigForm form,
                                   Model model) {
        serverService.saveServerConfig(serverId, form.getSendTime(), form.getQuestions());

        model.addAttribute("serverId", serverId);
        model.addAttribute("configForm", form);
        model.addAttribute("success", true);
        return "redirect:server-home";
    }
}