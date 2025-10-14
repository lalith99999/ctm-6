package com.ctm.main;

import java.time.LocalDateTime;

import com.ctm.annotations.Check;
import com.ctm.annotations.Column;
import com.ctm.annotations.FK;
import com.ctm.annotations.Id;
import com.ctm.annotations.Table;
import com.ctm.annotations.Transient;
import com.ctm.model.MatchStatus;
import com.ctm.model.TossDecision;

@Table(name = "matches")
public class Match {

    @Id(auto = true)
    @Column(name = "match_id", nullable = false, unique = true)
    private long id;

    @Column(name = "tournament_id", nullable = false)
    @FK(references = "tournaments(tournament_id)", onDelete = "CASCADE", onUpdate = "CASCADE")
    private long tournamentId;

    @Column(name = "team_a_id", nullable = false)
    @FK(references = "teams(team_id)", onDelete = "RESTRICT", onUpdate = "CASCADE")
    private long teamAId;

    @Column(name = "team_b_id", nullable = false)
    @FK(references = "teams(team_id)", onDelete = "RESTRICT", onUpdate = "CASCADE")
    private long teamBId;

    @Column(name = "datetime", nullable = true)
    private LocalDateTime dateTime;

    @Column(name = "venue", length = 100, nullable = true)
    private String venue;

    @Column(name = "status", length = 20, nullable = false, defaultValue = "SCHEDULED")
    @Check("status IN ('SCHEDULED','LIVE','FINISHED')")
    private MatchStatus status;

    @Column(name = "result", length = 200, nullable = true)
    private String result;

    @Column(name = "max_overs", nullable = true)
    @Check("max_overs IS NULL OR max_overs > 0")
    private Integer maxOversPerInnings;

    @Column(name = "toss_winner_id", nullable = true)
    @FK(references = "teams(team_id)", onDelete = "SET NULL", onUpdate = "CASCADE")
    private Long tossWinnerTeamId;

    @Column(name = "toss_decision", length = 10, nullable = true)
    @Check("toss_decision IS NULL OR toss_decision IN ('BAT','BOWL')")
    private TossDecision tossDecision;

    @Column(name = "winner_team_id", nullable = true)
    @FK(references = "teams(team_id)", onDelete = "SET NULL", onUpdate = "CASCADE")
    private Long winnerTeamId;

    @Column(name = "pom_jersey", nullable = true)
    @FK(references = "players(jersey_no)", onDelete = "SET NULL", onUpdate = "CASCADE")
    private Long playerOfMatchJersey;

    // Team A score
    @Column(name = "a_runs",   nullable = false, defaultValue = "0") @Check("a_runs >= 0")
    private int aRuns;
    @Column(name = "a_wkts",   nullable = false, defaultValue = "0") @Check("a_wkts BETWEEN 0 AND 10")
    private int aWkts;
    @Column(name = "a_extras", nullable = false, defaultValue = "0") @Check("a_extras >= 0")
    private int aExtras;
    @Column(name = "a_overs",  nullable = false, defaultValue = "0") @Check("a_overs >= 0")
    private double aOvers;

    // Team B score
    @Column(name = "b_runs",   nullable = false, defaultValue = "0") @Check("b_runs >= 0")
    private int bRuns;
    @Column(name = "b_wkts",   nullable = false, defaultValue = "0") @Check("b_wkts BETWEEN 0 AND 10")
    private int bWkts;
    @Column(name = "b_extras", nullable = false, defaultValue = "0") @Check("b_extras >= 0")
    private int bExtras;
    @Column(name = "b_overs",  nullable = false, defaultValue = "0") @Check("b_overs >= 0")
    private double bOvers;

    // table-level check captured on a transient field for your DDL generator
    @Transient @Check("team_a_id <> team_b_id")
    private transient int __chkTeamsDifferent;

    // ✅ ADDED transient display fields for JSPs / DAO convenience
    @Transient
    private String tournamentName;

    @Transient
    private String teamAName;

    @Transient
    private String teamBName;

    public Match() {}

    public Match(long id, long tournamentId, long teamAId, long teamBId, LocalDateTime dateTime, String venue) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.teamAId = teamAId;
        this.teamBId = teamBId;
        this.dateTime = dateTime;
        this.venue = venue;
        this.status = MatchStatus.SCHEDULED;
    }

    // --------- Getters & Setters ---------
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTournamentId() { return tournamentId; }
    public void setTournamentId(long tournamentId) { this.tournamentId = tournamentId; }

    public long getTeamAId() { return teamAId; }
    public void setTeamAId(long teamAId) { this.teamAId = teamAId; }

    public long getTeamBId() { return teamBId; }
    public void setTeamBId(long teamBId) { this.teamBId = teamBId; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public Integer getMaxOversPerInnings() { return maxOversPerInnings; }
    public void setMaxOversPerInnings(Integer maxOversPerInnings) { this.maxOversPerInnings = maxOversPerInnings; }

    public Long getTossWinnerTeamId() { return tossWinnerTeamId; }
    public void setTossWinnerTeamId(Long tossWinnerTeamId) { this.tossWinnerTeamId = tossWinnerTeamId; }

    public TossDecision getTossDecision() { return tossDecision; }
    public void setTossDecision(TossDecision tossDecision) { this.tossDecision = tossDecision; }

    public Long getWinnerTeamId() { return winnerTeamId; }
    public void setWinnerTeamId(Long winnerTeamId) { this.winnerTeamId = winnerTeamId; }

    public Long getPlayerOfMatchJersey() { return playerOfMatchJersey; }
    public void setPlayerOfMatchJersey(Long playerOfMatchJersey) { this.playerOfMatchJersey = playerOfMatchJersey; }

    public int getaRuns() { return aRuns; }
    public void setaRuns(int aRuns) { this.aRuns = aRuns; }

    public int getaWkts() { return aWkts; }
    public void setaWkts(int aWkts) { this.aWkts = aWkts; }

    public int getaExtras() { return aExtras; }
    public void setaExtras(int aExtras) { this.aExtras = aExtras; }

    public double getaOvers() { return aOvers; }
    public void setaOvers(double aOvers) { this.aOvers = aOvers; }

    public int getbRuns() { return bRuns; }
    public void setbRuns(int bRuns) { this.bRuns = bRuns; }

    public int getbWkts() { return bWkts; }
    public void setbWkts(int bWkts) { this.bWkts = bWkts; }

    public int getbExtras() { return bExtras; }
    public void setbExtras(int bExtras) { this.bExtras = bExtras; }

    public double getbOvers() { return bOvers; }
    public void setbOvers(double bOvers) { this.bOvers = bOvers; }

    // ✅ New transient getters/setters
    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }

    public String getTeamAName() { return teamAName; }
    public void setTeamAName(String teamAName) { this.teamAName = teamAName; }

    public String getTeamBName() { return teamBName; }
    public void setTeamBName(String teamBName) { this.teamBName = teamBName; }
}
