package com.ctm.dao;

import java.time.LocalDateTime;
import java.util.List;

import com.ctm.model.Match;

public interface ScheduleDao {
    List<Match> generateSchedule(long tournamentId, LocalDateTime start, String venue);
}
