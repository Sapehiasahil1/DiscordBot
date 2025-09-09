package com.sapehia.Geekbot.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Server {

    @Id
    private String serverId;

    private String serverName;

    @ManyToMany
    @JoinTable(
            name = "server_members",
            joinColumns = @JoinColumn(name = "server_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private List<Member> serverMembers;

    public Server() {
    }

    public Server(String serverId, String serverName, List<Member> serverMembers) {
        this.serverId = serverId;
        this.serverName = serverName;
        this.serverMembers = serverMembers;
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

    public List<Member> getServerMembers() {
        return serverMembers;
    }

    public void setServerMembers(List<Member> serverMembers) {
        this.serverMembers = serverMembers;
    }
}
