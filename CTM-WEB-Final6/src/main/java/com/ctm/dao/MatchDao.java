package com.ctm.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ctm.model.Match;

public interface MatchDao {
    Map<Long, Integer> todayScheduledCountMap();
    List<Map<String, Object>> getTodayMatches(long tournamentId);
    Optional<Match> startMatch(long matchId);

    List<Map<String, Object>> liveTournamentCounts();
    List<Match> getLiveMatchesByTournament(long tournamentId);
    Optional<Match> findMatch(long matchId);

    boolean lockFirstInnings(long matchId, long battingTeamId, int runs, int wickets, double overs, int extras);
    boolean lockSecondInnings(long matchId, int runs, int wickets, double overs, int extras);
}
