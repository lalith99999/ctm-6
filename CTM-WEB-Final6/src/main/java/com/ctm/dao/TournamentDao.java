package com.ctm.dao;

import java.util.List;
import java.util.Optional;

import com.ctm.model.TeamStanding;
import com.ctm.model.Tournament;

public interface TournamentDao {

    Tournament createTournament(String name, String format);
    Optional<Tournament> findTournament(long tournamentId);
    Tournament getTournamentOrThrow(long tournamentId);
    List<Tournament> listAllTournaments();
    Tournament updateTournament(long id, String newName, String newFormat);
    Tournament deleteTournament(long id);

    void enrollTeam(long tournamentId, long teamId);
    void removeTeam(long tournamentId, long teamId);
    boolean isTeamEnrolled(long tournamentId, long teamId);
    List<TeamStanding> listTeamsInTournament(long tournamentId);

    void registerMatch(long tournamentId, long matchId);
    int updateTournamentTeam(long tournamentId, long teamId, String newName, String newCity);
    int deleteTournamentTeam(long tournamentId, long teamId);
    boolean fixturesExist(long tournamentId);
    int countFixtures(long tournamentId);

    boolean existsByName(String name);
}
