package com.sapehia.Geekbot.model;

import java.util.ArrayList;
import java.util.List;

public class ServerConfigForm {
    private String sendTime;
    private List<String> questions = new ArrayList<>();

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }
}

