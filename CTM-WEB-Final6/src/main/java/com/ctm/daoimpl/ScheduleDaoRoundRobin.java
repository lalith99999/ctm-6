package com.ctm.daoimpl;

import java.sql.*;
import java.time.LocalDateTime;
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

        try (Connection con = DaoUtil.getMyConnection()) {
            con.setAutoCommit(false);

            // ✅ Step 1: Check for existing fixtures
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) FROM matches WHERE tournament_id=?")) {
                ps.setLong(1, tournamentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.err.println("⚠ Fixtures already exist for tournament " + tournamentId);
                        return false;
                    }
                }
            }

            // ✅ Step 2: Prepare data
            int n = teams.size();
            boolean odd = (n % 2 != 0);
            if (odd) {
                teams.add(new TeamStanding(-1L, "BYE", "NA", 0, 0.0, 0));
                n++;
            }

            LocalDateTime matchDate = LocalDateTime.now().plusDays(1);
            int matchCount = 0;

            // ✅ Step 3: Generate round-robin fixtures
            for (int round = 0; round < n - 1; round++) {
                for (int i = 0; i < n / 2; i++) {
                    long teamA = teams.get(i).getTeamId();
                    long teamB = teams.get(n - 1 - i).getTeamId();

                    if (teamA == -1 || teamB == -1) continue;

                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO matches " +
                            "(match_id, tournament_id, team_a_id, team_b_id, venue, datetime, status, " +
                            "a_runs, a_wkts, a_extras, a_overs, b_runs, b_wkts, b_extras, b_overs) " +
                            "VALUES ((SELECT NVL(MAX(match_id),0)+1 FROM matches), ?, ?, ?, ?, ?, 'SCHEDULED', 0,0,0,0,0,0,0,0)")) {

                        ps.setLong(1, tournamentId);
                        ps.setLong(2, teamA);
                        ps.setLong(3, teamB);
                        ps.setString(4, venue);
                        ps.setString(5, matchDate.toString());
                        ps.executeUpdate();
                        matchCount++;
                    }

                    matchDate = matchDate.plusDays(1);
                }

                // rotate teams except first
                TeamStanding fixed = teams.get(0);
                TeamStanding last = teams.remove(teams.size() - 1);
                teams.add(1, last);
                teams.set(0, fixed);
            }

            con.commit();
            System.out.println("✅ Fixtures generated successfully! Count: " + matchCount);
            return matchCount > 0;

        } catch (SQLException e) {
            System.err.println("❌ SQL error while generating fixtures:");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ General error while generating fixtures:");
            e.printStackTrace();
        }
        return false;
    }
}
