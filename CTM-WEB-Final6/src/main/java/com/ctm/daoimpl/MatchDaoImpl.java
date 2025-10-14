// Java 8
package com.ctm.daoimpl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ctm.dao.MatchDao;
import com.ctm.model.Match;
import com.ctm.util.DaoUtil;

public class MatchDaoImpl implements MatchDao {

    // ---------------- EXISTING METHODS ----------------

    @Override
    public List<Match> getLiveMatches() {
        String sql =
            "SELECT m.match_id, m.tournament_id, t.name AS tournament_name, " +
            "       m.team1_id, m.team2_id, " +
            "       (SELECT name FROM teams WHERE team_id = m.team1_id) AS team1_name, " +
            "       (SELECT name FROM teams WHERE team_id = m.team2_id) AS team2_name " +
            "FROM matches m JOIN tournaments t ON t.tournament_id = m.tournament_id " +
            "WHERE m.status = 'LIVE' " +
            "ORDER BY t.name, m.match_id";
        List<Match> list = new ArrayList<>();
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Match m = new Match();
                m.setMatchId(rs.getLong("match_id"));
                m.setTournamentId(rs.getLong("tournament_id"));
                m.setTournamentName(rs.getString("tournament_name"));
                m.setTeam1Id(rs.getLong("team1_id"));
                m.setTeam2Id(rs.getLong("team2_id"));
                m.setTeam1Name(rs.getString("team1_name"));
                m.setTeam2Name(rs.getString("team2_name"));
                list.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean updateFirstInnings(long id, int total, int overs, int wkts) {
        String sql =
            "UPDATE matches " +
            "SET first_innings_total=?, first_innings_overs=?, first_innings_wickets=? " +
            "WHERE match_id=? AND status='LIVE'";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setInt(1, total);
            ps.setInt(2, overs);
            ps.setInt(3, wkts);
            ps.setLong(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean updateSecondInnings(long id, int total, int overs, int wkts) {
        String sql =
            "UPDATE matches " +
            "SET second_innings_total=?, second_innings_overs=?, second_innings_wickets=? " +
            "WHERE match_id=? AND status='LIVE'";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setInt(1, total);
            ps.setInt(2, overs);
            ps.setInt(3, wkts);
            ps.setLong(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean computeAndPersistResult(long id) {
        // 0 = tie, else winner team_id
        String sql =
            "UPDATE matches m " +
            "SET m.result = (CASE WHEN NVL(m.first_innings_total,0) > NVL(m.second_innings_total,0) THEN m.team1_id " +
            "                    WHEN NVL(m.second_innings_total,0) > NVL(m.first_innings_total,0) THEN m.team2_id " +
            "                    ELSE 0 END), " +
            "    m.status = 'FINISHED' " +
            "WHERE m.match_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ---------------- NEW METHODS FOR START MATCH FLOW ----------------

    /**
     * Start match: sets toss winner, decision, and marks it LIVE.
     */
    @Override
    public boolean startMatch(long matchId, long tossWinnerId, String tossDecision) {
        String sql = "UPDATE matches SET toss_winner_id=?, toss_decision=?, status='LIVE' " +
                     "WHERE match_id=? AND status='SCHEDULED'";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tossWinnerId);
            ps.setString(2, tossDecision.toUpperCase());
            ps.setLong(3, matchId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get all matches scheduled today for a specific tournament.
     */
    @Override
    public List<Map<String, Object>> getTodayMatches(long tournamentId) {
        String sql =
            "SELECT m.match_id AS id, m.team1_id AS aId, m.team2_id AS bId, " +
            "       (SELECT name FROM teams WHERE team_id=m.team1_id) AS aName, " +
            "       (SELECT name FROM teams WHERE team_id=m.team2_id) AS bName, " +
            "       m.venue, TO_CHAR(m.datetime,'DD-MON HH24:MI') AS dt " +
            "FROM matches m " +
            "WHERE m.tournament_id=? AND m.status='SCHEDULED' " +
            "AND TRUNC(m.datetime)=TRUNC(SYSDATE) " +
            "ORDER BY m.match_id";
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", rs.getLong("id"));
                    m.put("aId", rs.getLong("aId"));
                    m.put("bId", rs.getLong("bId"));
                    m.put("aName", rs.getString("aName"));
                    m.put("bName", rs.getString("bName"));
                    m.put("venue", rs.getString("venue"));
                    m.put("datetime", rs.getString("dt"));
                    list.add(m);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /**
     * Returns map of tournamentId → count of matches scheduled today.
     */
    @Override
    public Map<Long, Integer> todayScheduledCountMap() {
        String sql =
            "SELECT tournament_id, COUNT(*) AS cnt " +
            "FROM matches WHERE status='SCHEDULED' AND TRUNC(datetime)=TRUNC(SYSDATE) " +
            "GROUP BY tournament_id";
        Map<Long, Integer> map = new HashMap<>();
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getLong("tournament_id"), rs.getInt("cnt"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }
}
