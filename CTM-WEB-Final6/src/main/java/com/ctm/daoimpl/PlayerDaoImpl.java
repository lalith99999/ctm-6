package com.ctm.daoimpl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ctm.dao.PlayerDao;
import com.ctm.model.Player;
import com.ctm.model.PlayerType;
import com.ctm.util.DaoUtil;

public class PlayerDaoImpl implements PlayerDao {

    private long global(long teamId, long local) {
        return teamId * 1000 + local;
    }

    @Override
    public Player createPlayer(long teamId, long jerseyNo, String name, PlayerType type) {
        if (maxLimitReached(teamId)) return null;
        if (existsInTeam(teamId, jerseyNo)) return null;

        long global = global(teamId, jerseyNo);
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(
                "INSERT INTO players (jersey_no, name, type) VALUES (?, ?, ?)")) {
            ps.setLong(1, global);
            ps.setString(2, name);
            ps.setString(3, type.name());
            ps.executeUpdate();

            try (PreparedStatement map = DaoUtil.getMyPreparedStatement(
                    "INSERT INTO team_players (team_id, jersey_no) VALUES (?, ?)")) {
                map.setLong(1, teamId);
                map.setLong(2, global);
                map.executeUpdate();
            }

            return new Player(jerseyNo, name, type);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public Player updatePlayer(long teamId, long jerseyNo, String newName, PlayerType newType) {
        long global = global(teamId, jerseyNo);
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
    public Player deletePlayer(long teamId, long jerseyNo) {
        long global = global(teamId, jerseyNo);
        Optional<Player> existing = findPlayer(teamId, jerseyNo);
        if (!existing.isPresent()) return null;

        try (PreparedStatement delMap = DaoUtil.getMyPreparedStatement(
                "DELETE FROM team_players WHERE team_id=? AND jersey_no=?")) {
            delMap.setLong(1, teamId);
            delMap.setLong(2, global);
            delMap.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }

        try (PreparedStatement delPlayer = DaoUtil.getMyPreparedStatement(
                "DELETE FROM players WHERE jersey_no=?")) {
            delPlayer.setLong(1, global);
            delPlayer.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }

        return existing.get();
    }

    @Override
    public Optional<Player> findPlayer(long teamId, long jerseyNo) {
        long global = global(teamId, jerseyNo);
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
    public List<Player> listPlayers(long teamId) {
        List<Player> out = new ArrayList<>();
        String sql = "SELECT p.jersey_no, p.name, p.type FROM players p "
                   + "JOIN team_players tp ON p.jersey_no = tp.jersey_no "
                   + "WHERE tp.team_id=? ORDER BY p.jersey_no";
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(sql)) {
            ps.setLong(1, teamId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                long local = rs.getLong("jersey_no") % 1000;
                out.add(new Player(local, rs.getString("name"),
                        PlayerType.valueOf(rs.getString("type"))));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return out;
    }

    @Override
    public boolean existsInTeam(long teamId, long jerseyNo) {
        long global = global(teamId, jerseyNo);
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(
                "SELECT 1 FROM players WHERE jersey_no=?")) {
            ps.setLong(1, global);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean maxLimitReached(long teamId) {
        try (PreparedStatement ps = DaoUtil.getMyPreparedStatement(
                "SELECT COUNT(*) FROM team_players WHERE team_id=?")) {
            ps.setLong(1, teamId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) >= 11;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
