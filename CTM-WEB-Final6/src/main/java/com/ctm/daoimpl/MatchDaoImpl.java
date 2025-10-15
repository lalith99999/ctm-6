package com.ctm.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import com.ctm.dao.MatchDao;
import com.ctm.model.Match;
import com.ctm.model.MatchStatus;
import com.ctm.model.TossDecision;
import com.ctm.util.DaoUtil;

public class MatchDaoImpl implements MatchDao {

    private static final String STATUS_SCHEDULED = "SCHEDULED";
    private static final String STATUS_LIVE = "LIVE";
    private static final String STATUS_FINISHED = "FINISHED";

    @Override
    public Map<Long, Integer> todayScheduledCountMap() {
        String sql =
            "SELECT tournament_id, COUNT(*) AS cnt " +
            "FROM matches WHERE status=? AND TRUNC(datetime)=TRUNC(SYSDATE) " +
            "GROUP BY tournament_id";
        Map<Long, Integer> map = new HashMap<>();
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setString(1, STATUS_SCHEDULED);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getLong("tournament_id"), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    @Override
    public List<Map<String, Object>> getTodayMatches(long tournamentId) {
        String sql =
            "SELECT m.match_id AS id, m.team_a_id AS aId, m.team_b_id AS bId, " +
            "       ta.name AS aName, tb.name AS bName, m.venue, " +
            "       TO_CHAR(m.datetime,'DD-MON-YYYY') AS dt " +
            "FROM matches m " +
            "JOIN teams ta ON ta.team_id = m.team_a_id " +
            "JOIN teams tb ON tb.team_id = m.team_b_id " +
            "WHERE m.tournament_id=? AND m.status=? " +
            "AND TRUNC(m.datetime)=TRUNC(SYSDATE) " +
            "ORDER BY m.datetime, m.match_id";
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            ps.setString(2, STATUS_SCHEDULED);
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

    @Override
    public Optional<Match> startMatch(long matchId) {
        String fetchSql =
            "SELECT m.match_id, m.tournament_id, m.team_a_id, m.team_b_id, m.datetime, m.venue, " +
            "       ta.name AS team_a_name, tb.name AS team_b_name " +
            "FROM matches m " +
            "JOIN teams ta ON ta.team_id = m.team_a_id " +
            "JOIN teams tb ON tb.team_id = m.team_b_id " +
            "WHERE m.match_id=? AND m.status=?";
        String updateSql =
            "UPDATE matches SET toss_winner_id=?, toss_decision=?, first_innings_team_id=?, status=? " +
            "WHERE match_id=? AND status=?";

        try (Connection con = DaoUtil.getMyConnection()) {
            con.setAutoCommit(false);
            Match match = null;
            try (PreparedStatement ps = con.prepareStatement(fetchSql)) {
                ps.setLong(1, matchId);
                ps.setString(2, STATUS_SCHEDULED);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        match = mapMatch(rs);
                    }
                }
            }

            if (match == null) {
                con.rollback();
                return Optional.empty();
            }

            boolean tossForTeamA = ThreadLocalRandom.current().nextBoolean();
            long tossWinnerId = tossForTeamA ? match.getTeam1Id() : match.getTeam2Id();
            TossDecision decision = TossDecision.values()[ThreadLocalRandom.current().nextInt(TossDecision.values().length)];
            long firstBatting = (decision == TossDecision.BAT) ? tossWinnerId
                    : (tossWinnerId == match.getTeam1Id() ? match.getTeam2Id() : match.getTeam1Id());

            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setLong(1, tossWinnerId);
                ps.setString(2, decision.name());
                ps.setLong(3, firstBatting);
                ps.setString(4, STATUS_LIVE);
                ps.setLong(5, matchId);
                ps.setString(6, STATUS_SCHEDULED);
                if (ps.executeUpdate() == 0) {
                    con.rollback();
                    return Optional.empty();
                }
            }

            con.commit();
            match.setTossWinnerTeamId(tossWinnerId);
            match.setTossDecision(decision);
            match.setFirstInningsTeamId(firstBatting);
            match.setStatus(MatchStatus.LIVE);
            return Optional.of(match);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Map<String, Object>> liveTournamentCounts() {
        String sql =
            "SELECT m.tournament_id AS id, t.name, COUNT(*) AS cnt " +
            "FROM matches m JOIN tournaments t ON t.tournament_id = m.tournament_id " +
            "WHERE m.status=? GROUP BY m.tournament_id, t.name ORDER BY t.name";
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setString(1, STATUS_LIVE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", rs.getLong("id"));
                    row.put("name", rs.getString("name"));
                    row.put("count", rs.getInt("cnt"));
                    list.add(row);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<Match> getLiveMatchesByTournament(long tournamentId) {
        String sql =
            "SELECT m.match_id, m.tournament_id, m.team_a_id, m.team_b_id, " +
            "       ta.name AS team_a_name, tb.name AS team_b_name, m.datetime, m.venue, " +
            "       m.a_runs, m.a_wkts, m.a_extras, m.a_overs, " +
            "       m.b_runs, m.b_wkts, m.b_extras, m.b_overs, m.first_innings_team_id, m.toss_winner_id, m.toss_decision " +
            "FROM matches m " +
            "JOIN teams ta ON ta.team_id = m.team_a_id " +
            "JOIN teams tb ON tb.team_id = m.team_b_id " +
            "WHERE m.tournament_id=? AND m.status=? ORDER BY m.match_id";
        List<Match> list = new ArrayList<>();
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            ps.setString(2, STATUS_LIVE);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Match m = mapMatch(rs);
                    populateScores(rs, m);
                    list.add(m);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Optional<Match> findMatch(long matchId) {
        String sql =
            "SELECT m.match_id, m.tournament_id, m.team_a_id, m.team_b_id, m.datetime, m.venue, m.status, " +
            "       m.toss_winner_id, m.toss_decision, m.first_innings_team_id, m.winner_team_id, m.result, " +
            "       m.a_runs, m.a_wkts, m.a_extras, m.a_overs, m.b_runs, m.b_wkts, m.b_extras, m.b_overs, " +
            "       ta.name AS team_a_name, tb.name AS team_b_name " +
            "FROM matches m " +
            "JOIN teams ta ON ta.team_id = m.team_a_id " +
            "JOIN teams tb ON tb.team_id = m.team_b_id " +
            "WHERE m.match_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, matchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Match match = mapMatch(rs);
                    populateScores(rs, match);
                    String status = rs.getString("status");
                    try { match.setStatus(MatchStatus.valueOf(status)); } catch (Exception ignore) {}
                    match.setTossWinnerTeamId(getLong(rs, "toss_winner_id"));
                    String decision = rs.getString("toss_decision");
                    if (decision != null) match.setTossDecision(TossDecision.valueOf(decision));
                    match.setFirstInningsTeamId(getLong(rs, "first_innings_team_id"));
                    match.setWinnerTeamId(getLong(rs, "winner_team_id"));
                    match.setResult(rs.getString("result"));
                    return Optional.of(match);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public boolean lockFirstInnings(long matchId, long battingTeamId, int runs, int wickets, double overs, int extras) {
        String select =
            "SELECT tournament_id, team_a_id, team_b_id, first_innings_team_id, status " +
            "FROM matches WHERE match_id=? FOR UPDATE";
        String update =
            "UPDATE matches SET first_innings_team_id=?, %s_runs=?, %s_wkts=?, %s_overs=?, %s_extras=? WHERE match_id=?";

        try (Connection con = DaoUtil.getMyConnection();
             PreparedStatement psSelect = con.prepareStatement(select)) {
            con.setAutoCommit(false);
            psSelect.setLong(1, matchId);
            try (ResultSet rs = psSelect.executeQuery()) {
                if (!rs.next()) { con.rollback(); return false; }
                if (!STATUS_LIVE.equals(rs.getString("status"))) { con.rollback(); return false; }
                if (rs.getObject("first_innings_team_id") != null) { con.rollback(); return false; }

                long teamA = rs.getLong("team_a_id");
                long teamB = rs.getLong("team_b_id");
                if (battingTeamId != teamA && battingTeamId != teamB) { con.rollback(); return false; }

                if (!validateInnings(runs, wickets, overs, true)) { con.rollback(); return false; }
                if (extras < 0) { con.rollback(); return false; }

                String prefix = (battingTeamId == teamA) ? "a" : "b";
                String sql = String.format(update, prefix, prefix, prefix, prefix);
                try (PreparedStatement psUpdate = con.prepareStatement(sql)) {
                    psUpdate.setLong(1, battingTeamId);
                    psUpdate.setInt(2, runs);
                    psUpdate.setInt(3, wickets);
                    psUpdate.setDouble(4, overs);
                    psUpdate.setInt(5, extras);
                    psUpdate.setLong(6, matchId);
                    if (psUpdate.executeUpdate() == 0) { con.rollback(); return false; }
                }
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean lockSecondInnings(long matchId, int runs, int wickets, double overs, int extras) {
        String select =
            "SELECT m.tournament_id, m.team_a_id, m.team_b_id, m.first_innings_team_id, m.status, " +
            "       m.a_runs, m.a_wkts, m.a_extras, m.a_overs, " +
            "       m.b_runs, m.b_wkts, m.b_extras, m.b_overs, " +
            "       ta.name AS team_a_name, tb.name AS team_b_name " +
            "FROM matches m " +
            "JOIN teams ta ON ta.team_id = m.team_a_id " +
            "JOIN teams tb ON tb.team_id = m.team_b_id " +
            "WHERE m.match_id=? FOR UPDATE";
        String update =
            "UPDATE matches SET %s_runs=?, %s_wkts=?, %s_overs=?, %s_extras=?, " +
            "winner_team_id=?, result=?, status=? WHERE match_id=?";

        try (Connection con = DaoUtil.getMyConnection();
             PreparedStatement psSelect = con.prepareStatement(select)) {
            con.setAutoCommit(false);
            psSelect.setLong(1, matchId);
            try (ResultSet rs = psSelect.executeQuery()) {
                if (!rs.next()) { con.rollback(); return false; }
                if (!STATUS_LIVE.equals(rs.getString("status"))) { con.rollback(); return false; }

                Long firstTeamObj = getLong(rs, "first_innings_team_id");
                if (firstTeamObj == null) { con.rollback(); return false; }

                long tournamentId = rs.getLong("tournament_id");
                long teamA = rs.getLong("team_a_id");
                long teamB = rs.getLong("team_b_id");
                long firstTeam = firstTeamObj;
                long secondTeam = (firstTeam == teamA) ? teamB : teamA;

                int firstRuns = (firstTeam == teamA) ? rs.getInt("a_runs") : rs.getInt("b_runs");
                int target = firstRuns + 1;

                if (!validateInnings(runs, wickets, overs, false)) { con.rollback(); return false; }
                if (extras < 0) { con.rollback(); return false; }
                if (runs > target + 5) { con.rollback(); return false; }

                String prefix = (secondTeam == teamA) ? "a" : "b";

                Long winnerTeamId = null;
                String resultText;
                boolean tie = runs == firstRuns;
                if (tie) {
                    resultText = "Match tied";
                } else if (runs >= target) {
                    winnerTeamId = secondTeam;
                    int wicketsRemaining = Math.max(0, 10 - wickets);
                    String name = (secondTeam == teamA) ? rs.getString("team_a_name") : rs.getString("team_b_name");
                    resultText = name + " won by " + wicketsRemaining + " wicket" + (wicketsRemaining == 1 ? "" : "s");
                } else {
                    winnerTeamId = firstTeam;
                    int margin = firstRuns - runs;
                    String name = (firstTeam == teamA) ? rs.getString("team_a_name") : rs.getString("team_b_name");
                    resultText = name + " won by " + margin + " run" + (margin == 1 ? "" : "s");
                }

                String sql = String.format(update, prefix, prefix, prefix, prefix);
                try (PreparedStatement psUpdate = con.prepareStatement(sql)) {
                    psUpdate.setInt(1, runs);
                    psUpdate.setInt(2, wickets);
                    psUpdate.setDouble(3, overs);
                    psUpdate.setInt(4, extras);
                    if (winnerTeamId == null && !tie) {
                        psUpdate.setNull(5, java.sql.Types.NUMERIC);
                    } else if (tie) {
                        psUpdate.setNull(5, java.sql.Types.NUMERIC);
                    } else {
                        psUpdate.setLong(5, winnerTeamId);
                    }
                    psUpdate.setString(6, resultText);
                    psUpdate.setString(7, STATUS_FINISHED);
                    psUpdate.setLong(8, matchId);
                    if (psUpdate.executeUpdate() == 0) { con.rollback(); return false; }
                }

                applyPoints(con, tournamentId, teamA, teamB, winnerTeamId, tie);
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void applyPoints(Connection con, long tournamentId, long teamAId, long teamBId,
                              Long winnerTeamId, boolean tie) throws SQLException {
        String sql = "UPDATE tournament_teams SET played = played + 1, points = points + ? " +
                     "WHERE tournament_id=? AND team_id=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (tie) {
                ps.setInt(1, 1); ps.setLong(2, tournamentId); ps.setLong(3, teamAId); ps.executeUpdate();
                ps.setInt(1, 1); ps.setLong(2, tournamentId); ps.setLong(3, teamBId); ps.executeUpdate();
            } else if (winnerTeamId != null) {
                long loser = (winnerTeamId == teamAId) ? teamBId : teamAId;
                ps.setInt(1, 2); ps.setLong(2, tournamentId); ps.setLong(3, winnerTeamId); ps.executeUpdate();
                ps.setInt(1, 0); ps.setLong(2, tournamentId); ps.setLong(3, loser); ps.executeUpdate();
            }
        }
    }

    private Match mapMatch(ResultSet rs) throws SQLException {
        Match m = new Match();
        m.setMatchId(rs.getLong("match_id"));
        m.setTournamentId(rs.getLong("tournament_id"));
        m.setTeam1Id(rs.getLong("team_a_id"));
        m.setTeam2Id(rs.getLong("team_b_id"));
        m.setTeam1Name(rs.getString("team_a_name"));
        m.setTeam2Name(rs.getString("team_b_name"));
        Timestamp ts = rs.getTimestamp("datetime");
        if (ts != null) m.setDateTime(ts.toLocalDateTime());
        m.setVenue(rs.getString("venue"));
        return m;
    }

    private void populateScores(ResultSet rs, Match m) throws SQLException {
        m.setARuns(rs.getInt("a_runs"));
        m.setAWkts(rs.getInt("a_wkts"));
        m.setAExtras(rs.getInt("a_extras"));
        m.setAOvers(rs.getDouble("a_overs"));
        m.setBRuns(rs.getInt("b_runs"));
        m.setBWkts(rs.getInt("b_wkts"));
        m.setBExtras(rs.getInt("b_extras"));
        m.setBOvers(rs.getDouble("b_overs"));
        m.setFirstInningsTeamId(getLong(rs, "first_innings_team_id"));
        m.setTossWinnerTeamId(getLong(rs, "toss_winner_id"));
        String decision = rs.getString("toss_decision");
        if (decision != null) {
            m.setTossDecision(TossDecision.valueOf(decision));
        }
    }

    private Long getLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private boolean validateInnings(int runs, int wickets, double overs, boolean enforceFullOvers) {
        if (runs < 0 || wickets < 0 || wickets > 10) return false;
        if (overs < 0.0 || overs > 20.0) return false;
        if (enforceFullOvers && wickets < 10 && Math.abs(overs - 20.0) > 1e-3) return false;
        return true;
    }
}
