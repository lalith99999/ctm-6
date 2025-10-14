// Java 8
package com.ctm.dao;

import java.util.List;
import java.util.Map;

import com.ctm.model.Match;

public interface MatchDao {
    List<Match> getLiveMatches();                  // grouped in JSP by tournament
    boolean updateFirstInnings(long matchId, int total, int overs, int wkts);
    boolean updateSecondInnings(long matchId, int total, int overs, int wkts);
    boolean computeAndPersistResult(long matchId); // sets status FINISHED and winner/tie
	/**
	 * Returns map of tournamentId → count of matches scheduled today.
	 */
	Map<Long, Integer> todayScheduledCountMap();
	/**
	 * Get all matches scheduled today for a specific tournament.
	 */
	List<Map<String, Object>> getTodayMatches(long tournamentId);
	/**
	 * Start match: sets toss winner, decision, and marks it LIVE.
	 */
	boolean startMatch(long matchId, long tossWinnerId, String tossDecision);
}
