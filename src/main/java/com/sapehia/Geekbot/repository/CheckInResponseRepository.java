package com.sapehia.Geekbot.repository;

import com.sapehia.Geekbot.model.CheckInResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CheckInResponseRepository extends JpaRepository<CheckInResponse, Long> {
    List<CheckInResponse> findByServerId(String serverId);
    List<CheckInResponse> findByServerIdAndDate(String serverId, LocalDate date);
    List<CheckInResponse> findByDiscordUserId(String userId);
    List<CheckInResponse> findByServerIdAndDateBetween(String serverId, LocalDate startDate, LocalDate endDate);
}
