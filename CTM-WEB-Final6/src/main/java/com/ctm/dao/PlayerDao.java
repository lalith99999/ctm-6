package com.ctm.dao;

import java.util.List;
import java.util.Optional;
import com.ctm.model.Player;
import com.ctm.model.PlayerType;

public interface PlayerDao {
    Player createPlayer(long teamId, long jerseyNo, String name, PlayerType type);
    Player updatePlayer(long teamId, long jerseyNo, String newName, PlayerType newType);
    Player deletePlayer(long teamId, long jerseyNo);
    Optional<Player> findPlayer(long teamId, long jerseyNo);
    List<Player> listPlayers(long teamId);
    boolean existsInTeam(long teamId, long jerseyNo);
    boolean maxLimitReached(long teamId);
}
