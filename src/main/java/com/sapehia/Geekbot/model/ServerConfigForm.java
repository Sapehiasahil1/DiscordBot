package com.sapehia.Geekbot.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServerConfigForm {
    private LocalTime sendTime;
    private List<String> questions = new ArrayList<>();
    private Set<DayOfWeek> excludedDays = new HashSet<>();

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

    public Set<DayOfWeek> getExcludedDays() {
        return excludedDays;
    }

    public void setExcludedDays(Set<DayOfWeek> excludedDays) {
        this.excludedDays = excludedDays;
    }
}