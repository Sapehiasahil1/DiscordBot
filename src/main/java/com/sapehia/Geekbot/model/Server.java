package com.sapehia.Geekbot.model;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Server {

    @Id
    private String serverId;

    private String serverName;

    private LocalTime questionTime;

    @ManyToMany(mappedBy = "servers", fetch = FetchType.EAGER)
    private List<Member> serverMembers = new ArrayList<>();


    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL)
    private List<Question> questions = new ArrayList<>();

    public Server() {
    }

    public Server(String serverId, String serverName, ArrayList<Member> serverMembers, LocalTime questionTime, List<Question> questions) {
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
}
