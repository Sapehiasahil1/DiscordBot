package com.sapehia.Geekbot.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "member_details")
public class Member {

    @Id
    private String discordUserId;

    private String username;

    @ManyToMany(mappedBy = "serverMembers")
    private List<Server> servers;

    public Member() {
    }

    public String getDiscordUserId() {
        return discordUserId;
    }

    public void setDiscordUserId(String  discordUserId) {
        this.discordUserId = discordUserId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Server> getServers() {
        return servers;
    }

    public void setServers(List<Server> servers) {
        this.servers = servers;
    }
}
