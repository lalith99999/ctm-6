package com.ctm.model;

import com.ctm.annotations.Column;
import com.ctm.annotations.FK;
import com.ctm.annotations.Id;
import com.ctm.annotations.Table;

@Table(name = "tournament_teams")
public class TournamentTeam {

    @Id(auto = true)
    @Column(name = "id", nullable = false, unique = true)
    private long id;

    @Column(name = "tournament_id", nullable = false)
    @FK(references = "tournaments(tournament_id)", onDelete = "CASCADE", onUpdate = "CASCADE")
    private long tournamentId;

    @Column(name = "team_id", nullable = false)
    @FK(references = "teams(team_id)", onDelete = "CASCADE", onUpdate = "CASCADE")
    private long teamId;

    @Column(name = "points", nullable = false, defaultValue = "0")
    private int points;

    @Column(name = "nrr", nullable = false, defaultValue = "0")
    private double nrr;

    @Column(name = "played", nullable = false, defaultValue = "0")
    private int played;

    public TournamentTeam() {}

    public TournamentTeam(long tournamentId, long teamId) {
        this.tournamentId = tournamentId;
        this.teamId = teamId;
        this.points = 0;
        this.nrr = 0;
        this.played = 0;
    }

    public long getId() { return id; }
    public long getTournamentId() { return tournamentId; }
    public long getTeamId() { return teamId; }
    public int getPoints() { return points; }
    public double getNrr() { return nrr; }
    public int getPlayed() { return played; }

    public void setPoints(int points) { this.points = points; }
    public void setNrr(double nrr) { this.nrr = nrr; }
    public void setPlayed(int played) { this.played = played; }
}
