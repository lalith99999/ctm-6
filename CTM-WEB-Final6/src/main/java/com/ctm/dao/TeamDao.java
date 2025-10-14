package com.ctm.dao;

import java.util.List;
import java.util.Optional;

import com.ctm.model.Player;
import com.ctm.model.PlayerType;
import com.ctm.model.Team;

public interface TeamDao {

    // ---------- TEAM ----------
    Team createTeam(String name, String city);
    Optional<Team> findTeam(long teamId);
    Optional<Team> findTeamByName(String name);
    List<Team> listTeams();
    Team updateTeam(long id, String newName, String newCity);
    Team deleteTeam(long id);
    boolean existsByName(String name);

    // ---------- PLAYERS ----------
    Optional<Player> findPlayer(long teamId, long jerseyNo);
    List<Player> listPlayersOfTeam(long teamId);
    Player createPlayer(long teamId, long jerseyNo, String name, PlayerType type);
    Player updatePlayerInTeam(long teamId, long jerseyNo, String newName, PlayerType newType);
    Player deletePlayerInTeam(long teamId, long jerseyNo);
}
