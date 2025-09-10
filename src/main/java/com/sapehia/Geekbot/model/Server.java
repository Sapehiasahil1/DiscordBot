package com.sapehia.Geekbot.model;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Server {

    @Id
    private String serverId;

    private String serverName;

    private LocalTime questionTime;

    @ManyToMany(mappedBy = "servers")
    private Set<Member> serverMembers = new HashSet<>();


    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL)
    private List<Question> questions = new ArrayList<>();

    public Server() {
    }

    public Server(String serverId, String serverName, HashSet<Member> serverMembers, LocalTime questionTime, List<Question> questions) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.serverMembers = serverMembers;
        this.questionTime = questionTime;
        this.questions = questions;
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

    public Set<Member> getServerMembers() {
        return serverMembers;
    }

    public void setServerMembers(HashSet<Member> serverMembers) {
        this.serverMembers = serverMembers;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
