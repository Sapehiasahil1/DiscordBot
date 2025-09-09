package com.sapehia.Geekbot.repository;

import com.sapehia.Geekbot.model.Server;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerRepository extends JpaRepository<Server, String> {
}
