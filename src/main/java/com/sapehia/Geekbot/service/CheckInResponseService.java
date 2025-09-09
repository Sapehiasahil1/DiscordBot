package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.CheckInResponse;
import com.sapehia.Geekbot.repository.CheckInResponseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CheckInResponseService {

    private final CheckInResponseRepository checkInResponseRepository;

    public CheckInResponseService(CheckInResponseRepository checkInResponseRepository) {
        this.checkInResponseRepository = checkInResponseRepository;
    }

    public List<CheckInResponse> getByServerId(String serverId) {
        return checkInResponseRepository.findByServerId(serverId);
    }

    public List<CheckInResponse> getByDiscordUserId(String userId) {
        return checkInResponseRepository.findByDiscordUserId(userId);
    }

    public List<CheckInResponse> getByServerIdAndDate(String serverId, LocalDate date) {
        return checkInResponseRepository.findByServerIdAndDate(serverId, date);
    }

    public List<CheckInResponse> getByServerIdAndDateBetween(String serverId, LocalDate startDate, LocalDate endDate) {
        return checkInResponseRepository.findByServerIdAndDateBetween(serverId, startDate, endDate);
    }

}
