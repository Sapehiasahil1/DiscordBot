package com.sapehia.Geekbot.model;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Server {

    @Id
    private String serverId;

    private String serverName;

    private LocalTime questionTime;

    private String excludedDays = "SUNDAY";

    @ManyToMany(mappedBy = "servers", fetch = FetchType.EAGER)
    private List<Member> serverMembers = new ArrayList<>();


    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    public Server() {
    }

    public Server(String serverId, String serverName, ArrayList<Member> serverMembers, LocalTime questionTime, List<Question> questions) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.serverMembers = serverMembers;
        this.questionTime = questionTime;
        this.questions = questions;
        this.excludedDays = "SUNDAY";
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public LocalTime getQuestionTime() {
        return questionTime;
    }

    public void setQuestionTime(LocalTime questionTime) {
        this.questionTime = questionTime;
    }

    public List<Member> getServerMembers() {
        return serverMembers;
    }

    public void setServerMembers(List<Member> serverMembers) {
        this.serverMembers = serverMembers;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public String getExcludedDays() {
        return excludedDays;
    }

    public void setExcludedDays(String excludedDays) {
        this.excludedDays = excludedDays;
    }

    public Set<DayOfWeek> getExcludedDaysAsSet() {
        if (excludedDays == null || excludedDays.trim().isEmpty()) {
            return Set.of();
        }
        return Arrays.stream(excludedDays.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());
    }

    public void setExcludedDaysFromSet(Set<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            this.excludedDays = "";
        } else {
            this.excludedDays = days.stream()
                    .map(DayOfWeek::name)
                    .collect(Collectors.joining(","));
        }
    }

    public boolean shouldSendQuestionsToday() {
        return !getExcludedDaysAsSet().contains(LocalDate.now().getDayOfWeek());
    }

    public boolean shouldSendQuestionsOnDay(DayOfWeek dayOfWeek) {
        return !getExcludedDaysAsSet().contains(dayOfWeek);
    }
}
