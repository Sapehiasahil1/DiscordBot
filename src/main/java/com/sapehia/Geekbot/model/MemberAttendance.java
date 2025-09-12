package com.sapehia.Geekbot.model;

public class MemberAttendance {
    private Member member;
    private int respondedDays;
    private int totalDays;
    private double percentage;

    public MemberAttendance(Member member, int respondedDays, int totalDays, double percentage) {
        this.member = member;
        this.respondedDays = respondedDays;
        this.totalDays = totalDays;
        this.percentage = percentage;
    }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public int getRespondedDays() { return respondedDays; }
    public void setRespondedDays(int respondedDays) { this.respondedDays = respondedDays; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
}
