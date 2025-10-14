package com.ctm.daoimpl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ctm.dao.TeamDao;
import com.ctm.model.Player;
import com.ctm.model.PlayerType;
import com.ctm.model.Team;
import com.ctm.util.DaoUtil;

public class TeamDaoImpl implements TeamDao {

    // ---------- TEAM ----------
    @Override
    public Team createTeam(String name, String city) {
        if (existsByName(name)) {
            System.out.println("⚠️ Duplicate team name: " + name);
            return null;
        }

        long newId = 0;
        try (Statement st = DaoUtil.getMyStatement();
             ResultSet rs = st.executeQuery("SELECT NVL(MAX(team_id),0)+1 AS next_id FROM teams")) {
            if (rs.next()) newId = rs.getLong("next_id");
        } catch (SQLException e) { e.printStackTrace(); }

        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(
                "INSERT INTO teams (team_id, name, city) VALUES (?, ?, ?)")) {
            ps.setLong(1, newId);
            ps.setString(2, name.trim());
            ps.setString(3, city.trim());
            ps.executeUpdate();
            System.out.println("✅ Team created: " + name);
            return new Team(newId, name, city);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public Optional<Team> findTeam(long teamId) {
        String sql = "SELECT team_id, name, city FROM teams WHERE team_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, teamId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Team(rs.getLong("team_id"),
                                            rs.getString("name"),
                                            rs.getString("city")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public Optional<Team> findTeamByName(String name) {
        String sql = "SELECT team_id, name, city FROM teams WHERE LOWER(name)=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Team(rs.getLong("team_id"),
                                            rs.getString("name"),
                                            rs.getString("city")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Team> listTeams() {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT team_id, name, city FROM teams ORDER BY team_id";
        try (Statement st = DaoUtil.getMyStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Team(rs.getLong("team_id"), rs.getString("name"), rs.getString("city")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Team updateTeam(long id, String newName, String newCity) {
        if (existsByName(newName)) {
            System.out.println("⚠️ Team name already exists: " + newName);
            return null;
        }
        String sql = "UPDATE teams SET name=?, city=? WHERE team_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setString(1, newName.trim());
            ps.setString(2, newCity.trim());
            ps.setLong(3, id);
            ps.executeUpdate();
            return new Team(id, newName, newCity);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public Team deleteTeam(long id) {
        Optional<Team> team = findTeam(id);
        if (!team.isPresent()) return null;

        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement("DELETE FROM teams WHERE team_id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
            return team.get();
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT 1 FROM teams WHERE LOWER(name)=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ---------- PLAYERS ----------
    @Override
    public Optional<Player> findPlayer(long teamId, long jerseyNo) {
        long global = teamId * 1000 + jerseyNo;
        String sql = "SELECT jersey_no, name, type FROM players WHERE jersey_no=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, global);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(new Player(rs.getLong("jersey_no"),
                                              rs.getString("name"),
                                              PlayerType.valueOf(rs.getString("type"))));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Optional.empty();
    }

    @Override
    public List<Player> listPlayersOfTeam(long teamId) {
        List<Player> list = new ArrayList<>();
        String sql = "SELECT p.jersey_no, p.name, p.type FROM players p "
                   + "JOIN team_players tp ON p.jersey_no = tp.jersey_no WHERE tp.team_id=?";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, teamId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                long local = rs.getLong("jersey_no") % 1000;
                list.add(new Player(local, rs.getString("name"),
                                    PlayerType.valueOf(rs.getString("type"))));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Player createPlayer(long teamId, long jerseyNo, String name, PlayerType type) {
        long global = teamId * 1000 + jerseyNo;
        if (findPlayer(teamId, jerseyNo).isPresent()) return null;

        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(
                "INSERT INTO players (jersey_no, name, type) VALUES (?, ?, ?)")) {
            ps.setLong(1, global);
            ps.setString(2, name);
            ps.setString(3, type.name());
            ps.executeUpdate();

            DaoUtil.getMyPreparedStatement("INSERT INTO team_players (team_id, jersey_no) VALUES ("+teamId+","+global+")").execute();
            return new Player(jerseyNo, name, type);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public Player updatePlayerInTeam(long teamId, long jerseyNo, String newName, PlayerType newType) {
        long global = teamId * 1000 + jerseyNo;
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(
                "UPDATE players SET name=?, type=? WHERE jersey_no=?")) {
            ps.setString(1, newName);
            ps.setString(2, newType.name());
            ps.setLong(3, global);
            ps.executeUpdate();
            return new Player(jerseyNo, newName, newType);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public Player deletePlayerInTeam(long teamId, long jerseyNo) {
        long global = teamId * 1000 + jerseyNo;
        Optional<Player> p = findPlayer(teamId, jerseyNo);
        if (!p.isPresent()) return null;
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement("DELETE FROM players WHERE jersey_no=?")) {
            ps.setLong(1, global);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
        return p.get();
    }
}
