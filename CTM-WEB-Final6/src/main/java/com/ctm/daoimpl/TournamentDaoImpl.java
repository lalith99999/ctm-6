package com.ctm.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ctm.dao.TournamentDao;
import com.ctm.model.TeamStanding;
import com.ctm.model.Tournament;
import com.ctm.util.DaoUtil;

public class TournamentDaoImpl implements TournamentDao {

    @Override
    public Tournament createTournament(String name, String format) {
        if (existsByName(name)) return null;

        long newId = 0;
        try (Statement st = DaoUtil.getMyStatement();
             ResultSet rs = st.executeQuery("SELECT NVL(MAX(tournament_id),0)+1 AS next_id FROM tournaments")) {
            if (rs.next()) newId = rs.getLong("next_id");
        } catch (SQLException e) { e.printStackTrace(); }

        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(
                "INSERT INTO tournaments (tournament_id, name, format) VALUES (?, ?, ?)")) {
            ps.setLong(1, newId);
            ps.setString(2, name.trim());
            ps.setString(3, format.trim());
            ps.executeUpdate();
            return new Tournament(newId, name, format);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT 1 FROM tournaments WHERE LOWER(name)=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public Optional<Tournament> findTournament(long tournamentId) {
        String sql = "SELECT tournament_id, name, format FROM tournaments WHERE tournament_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Tournament(
                        rs.getLong("tournament_id"),
                        rs.getString("name"),
                        rs.getString("format")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Tournament> listAllTournaments() {
        List<Tournament> list = new ArrayList<>();
        String sql = "SELECT tournament_id, name, format FROM tournaments ORDER BY tournament_id";
        try (Statement st = DaoUtil.getMyStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Tournament(
                        rs.getLong("tournament_id"),
                        rs.getString("name"),
                        rs.getString("format")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Tournament updateTournament(long id, String newName, String newFormat) {
        String sql = "UPDATE tournaments SET name=?, format=? WHERE tournament_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, newFormat);
            ps.setLong(3, id);
            ps.executeUpdate();
            return new Tournament(id, newName, newFormat);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
 // inside TournamentDaoImpl class (below your updateTournament method)
    public void updateTournamentName(long id, String newName) {
        updateTournament(id, newName, "T20");
    }


    @Override
    public Tournament deleteTournament(long id) {
        Optional<Tournament> before = findTournament(id);
        if (before.isEmpty()) return null;

        try (Connection con = DaoUtil.getMyConnection()) {
            con.setAutoCommit(false);

            con.prepareStatement(
                    "DELETE FROM player_performance WHERE match_id IN (SELECT match_id FROM matches WHERE tournament_id=?)")
                    .executeUpdate();

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM matches WHERE tournament_id=?")) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM tournament_teams WHERE tournament_id=?")) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM tournaments WHERE tournament_id=?")) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }

            con.commit();
            return before.get();
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean fixturesExist(long tournamentId) {
        String sql = "SELECT 1 FROM matches WHERE tournament_id=? FETCH FIRST 1 ROWS ONLY";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Compatibility methods (not implemented)
    @Override public Tournament getTournamentOrThrow(long tournamentId) { return findTournament(tournamentId).orElseThrow(); }
    @Override
    public List<TeamStanding> listTeamsInTournament(long tournamentId) {
        List<TeamStanding> out = new ArrayList<>();
        String sql = "SELECT t.team_id, t.name, t.city, tt.points, tt.nrr, tt.played " +
                     "FROM teams t JOIN tournament_teams tt ON t.team_id = tt.team_id " +
                     "WHERE tt.tournament_id = ? ORDER BY t.team_id";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                out.add(new TeamStanding(
                    rs.getLong("team_id"),
                    rs.getString("name"),
                    rs.getString("city"),
                    rs.getInt("points"),
                    rs.getDouble("nrr"),
                    rs.getInt("played")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }    @Override
    public void enrollTeam(long tournamentId, long teamId) {
        // Check if already enrolled
        String checkSql = "SELECT 1 FROM tournament_teams WHERE tournament_id=? AND team_id=?";
        String insertSql = "INSERT INTO tournament_teams (id, tournament_id, team_id, points, nrr, played) " +
                           "VALUES ((SELECT NVL(MAX(id),0)+1 FROM tournament_teams), ?, ?, 0, 0, 0)";
        try (Connection con = DaoUtil.getMyConnection()) {
            try (PreparedStatement check = con.prepareStatement(checkSql)) {
                check.setLong(1, tournamentId);
                check.setLong(2, teamId);
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    System.out.println("⚠ Team already enrolled in tournament " + tournamentId);
                    return;
                }
            }
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setLong(1, tournamentId);
                ps.setLong(2, teamId);
                ps.executeUpdate();
                System.out.println("✅ Team " + teamId + " enrolled into tournament " + tournamentId);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    @Override public void removeTeam(long tournamentId, long teamId) {}
    @Override public boolean isTeamEnrolled(long tournamentId, long teamId) { return false; }
    @Override public void registerMatch(long tournamentId, long matchId) {}
    @Override public int updateTournamentTeam(long tournamentId, long teamId, String newName, String newCity) { return 0; }
    @Override
    public int deleteTournamentTeam(long tournamentId, long teamId) {
        String sql = "DELETE FROM tournament_teams WHERE tournament_id=? AND team_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, tournamentId);
            ps.setLong(2, teamId);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
    @Override public int countFixtures(long tournamentId) { return 0; }
}
