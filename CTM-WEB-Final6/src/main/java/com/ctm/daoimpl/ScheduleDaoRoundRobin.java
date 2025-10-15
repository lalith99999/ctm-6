package com.ctm.daoimpl;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ctm.model.TeamStanding;
import com.ctm.util.DaoUtil;

/**
 * Final version — generates proper round-robin fixtures
 * compatible with your Oracle table structure.
 */
public class ScheduleDaoRoundRobin {

    public boolean generateFixtures(long tournamentId, List<TeamStanding> teams, String venue) {
        if (teams == null || teams.size() < 3) {
            System.err.println("❌ Not enough teams to generate fixtures.");
            return false;
        }

        Connection con = null;
        try {
            con = DaoUtil.getMyConnection();
            if (con == null) {
                System.err.println("❌ Unable to obtain database connection – aborting fixture generation.");
                return false;
            }
            con.setAutoCommit(false);

            // ✅ Step 1: Check for existing fixtures
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM matches WHERE tournament_id=?")) {
                ps.setLong(1, tournamentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.err.println("⚠ Fixtures already exist for tournament " + tournamentId);
                        try {
                            con.rollback();
                        } catch (SQLException rollEx) {
                            System.err.println("⚠ Rollback failed: " + rollEx.getMessage());
                        }
                        return false;
                    }
                }
            }

            // ✅ Step 2: Prepare data
            List<TeamStanding> bracket = new ArrayList<>(teams);
            int n = bracket.size();
            boolean odd = (n % 2 != 0);
            if (odd) {
                bracket.add(new TeamStanding(-1L, "BYE", "NA", 0, 0.0, 0));
                n++;
            }

            LocalDateTime matchDate = LocalDateTime.now()
                    .withHour(9)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);
            int matchCount = 0;

            // ✅ Step 3: Generate round-robin fixtures
            for (int round = 0; round < n - 1; round++) {
                for (int i = 0; i < n / 2; i++) {
                    long teamA = bracket.get(i).getTeamId();
                    long teamB = bracket.get(n - 1 - i).getTeamId();

                    if (teamA == -1 || teamB == -1) continue;

                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO matches " +
                            "(match_id, tournament_id, team_a_id, team_b_id, venue, datetime, status, " +
                            "a_runs, a_wkts, a_extras, a_overs, b_runs, b_wkts, b_extras, b_overs) " +
                            "VALUES ((SELECT NVL(MAX(match_id),0)+1 FROM matches), ?, ?, ?, ?, ?, ?, 0,0,0,0,0,0,0,0)")) {

                        ps.setLong(1, tournamentId);
                        ps.setLong(2, teamA);
                        ps.setLong(3, teamB);
                        ps.setString(4, venue);
                        ps.setTimestamp(5, java.sql.Timestamp.valueOf(matchDate));
                        ps.setString(6, com.ctm.model.MatchStatus.SCHEDULED.name());
                        ps.executeUpdate();
                        matchCount++;
                    }

                    matchDate = matchDate.plusDays(1);
                }

                // rotate teams except first
                TeamStanding fixed = bracket.get(0);
                TeamStanding last = bracket.remove(bracket.size() - 1);
                bracket.add(1, last);
                bracket.set(0, fixed);
            }

            con.commit();
            String sql = "SELECT m.match_id, t1.name AS team_a, t2.name AS team_b, m.venue, m.datetime " +
                         "FROM matches m " +
                         "JOIN teams t1 ON m.team_a_id = t1.team_id " +
                         "JOIN teams t2 ON m.team_b_id = t2.team_id " +
                         "WHERE m.tournament_id=? ORDER BY m.datetime, m.match_id";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, tournamentId);
                try (ResultSet rs = ps.executeQuery()) {
                    System.out.println("✅ Fixtures generated successfully! Count: " + matchCount);
                    while (rs.next()) {
                        long matchId = rs.getLong("match_id");
                        String teamAName = rs.getString("team_a");
                        String teamBName = rs.getString("team_b");
                        String matchVenue = rs.getString("venue");
                        Timestamp when = rs.getTimestamp("datetime");
                        String dateStr = when == null ? "-" : when.toLocalDateTime().toLocalDate().toString();
                        System.out.printf("#%d: %s vs %s at %s on %s%n", matchId, teamAName, teamBName, matchVenue, dateStr);
                    }
                }
            }
            return matchCount > 0;

        } catch (SQLException e) {
            System.err.println("❌ SQL error while generating fixtures:");
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollEx) {
                    System.err.println("⚠ Rollback failed: " + rollEx.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ General error while generating fixtures:");
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollEx) {
                    System.err.println("⚠ Rollback failed: " + rollEx.getMessage());
                }
            }
        } finally {
            if (con != null) {
                try {
                    if (!con.getAutoCommit()) {
                        con.setAutoCommit(true);
                    }
                } catch (SQLException ignore) {
                    // ignore
                }
                try {
                    con.close();
                } catch (SQLException ignore) {
                    // ignore
                }
            }
        }
        return false;
    }
}
