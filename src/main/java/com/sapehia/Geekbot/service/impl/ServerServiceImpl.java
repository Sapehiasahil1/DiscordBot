package com.sapehia.Geekbot.service.impl;

import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.model.Server;
import com.sapehia.Geekbot.repository.ServerRepository;
import com.sapehia.Geekbot.service.ServerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServerServiceImpl implements ServerService {

    private  final ServerRepository serverRepository;

    public ServerServiceImpl(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    @Override
    public List<Member> listOfMembers(String id) {
        Server server = serverRepository.findById(id).orElseThrow();
        return server.getServerMembers();
    }
}
