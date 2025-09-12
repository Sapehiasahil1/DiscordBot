package com.sapehia.Geekbot.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ServerConfigForm {
    private LocalTime sendTime;
    private List<String> questions = new ArrayList<>();

    public LocalTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalTime sendTime) {
        this.sendTime = sendTime;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }
}

