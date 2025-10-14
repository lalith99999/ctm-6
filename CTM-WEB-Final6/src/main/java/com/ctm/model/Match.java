package com.ctm.model;

import java.time.LocalDateTime;

import com.ctm.annotations.Check;
import com.ctm.annotations.Column;
import com.ctm.annotations.FK;
import com.ctm.annotations.Id;
import com.ctm.annotations.Table;
import com.ctm.annotations.Transient;

@Table(name = "matches")
public class Match {

    @Id(auto = true)
    @Column(name = "match_id", nullable = false, unique = true)
    private long matchId;

    @Column(name = "tournament_id", nullable = false)
    @FK(references = "tournaments(tournament_id)", onDelete = "CASCADE", onUpdate = "CASCADE")
    private long tournamentId;

    @Column(name = "team_a_id", nullable = false)
    @FK(references = "teams(team_id)", onDelete = "RESTRICT", onUpdate = "CASCADE")
    private long team1Id;   // same as team_a_id

    @Column(name = "team_b_id", nullable = false)
    @FK(references = "teams(team_id)", onDelete = "RESTRICT", onUpdate = "CASCADE")
    private long team2Id;   // same as team_b_id

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

    // --- Team A score (first innings) ---
    @Column(name = "a_runs",   nullable = false, defaultValue = "0") @Check("a_runs >= 0")
    private int aRuns;

    @Column(name = "a_wkts",   nullable = false, defaultValue = "0") @Check("a_wkts BETWEEN 0 AND 10")
    private int aWkts;

    @Column(name = "a_extras", nullable = false, defaultValue = "0") @Check("a_extras >= 0")
    private int aExtras;

    @Column(name = "a_overs",  nullable = false, defaultValue = "0") @Check("a_overs >= 0")
    private double aOvers;

    // --- Team B score (second innings) ---
    @Column(name = "b_runs",   nullable = false, defaultValue = "0") @Check("b_runs >= 0")
    private int bRuns;

    @Column(name = "b_wkts",   nullable = false, defaultValue = "0") @Check("b_wkts BETWEEN 0 AND 10")
    private int bWkts;

    @Column(name = "b_extras", nullable = false, defaultValue = "0") @Check("b_extras >= 0")
    private int bExtras;

    @Column(name = "b_overs",  nullable = false, defaultValue = "0") @Check("b_overs >= 0")
    private double bOvers;

    // --- Logical check for DDL generator ---
    @Transient @Check("team_a_id <> team_b_id")
    private transient int __chkTeamsDifferent;

    // --- Transient display fields for JSPs and DAO convenience ---
    @Transient
    private String tournamentName;

    @Transient
    private String team1Name;

    @Transient
    private String team2Name;

    // --- Constructors ---
    public Match() {}

    public Match(long matchId, long tournamentId, long team1Id, long team2Id, LocalDateTime dateTime, String venue) {
        this.matchId = matchId;
        this.tournamentId = tournamentId;
        this.team1Id = team1Id;
        this.team2Id = team2Id;
        this.dateTime = dateTime;
        this.venue = venue;
        this.status = MatchStatus.SCHEDULED;
    }

    // --- Getters and Setters ---
    public long getMatchId() { return matchId; }
    public void setMatchId(long matchId) { this.matchId = matchId; }

    public long getTournamentId() { return tournamentId; }
    public void setTournamentId(long tournamentId) { this.tournamentId = tournamentId; }

    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }

    public long getTeam1Id() { return team1Id; }
    public void setTeam1Id(long team1Id) { this.team1Id = team1Id; }

    public long getTeam2Id() { return team2Id; }
    public void setTeam2Id(long team2Id) { this.team2Id = team2Id; }

    public String getTeam1Name() { return team1Name; }
    public void setTeam1Name(String team1Name) { this.team1Name = team1Name; }

    public String getTeam2Name() { return team2Name; }
    public void setTeam2Name(String team2Name) { this.team2Name = team2Name; }

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

    public int getARuns() { return aRuns; }
    public void setARuns(int aRuns) { this.aRuns = aRuns; }

    public int getAWkts() { return aWkts; }
    public void setAWkts(int aWkts) { this.aWkts = aWkts; }

    public int getAExtras() { return aExtras; }
    public void setAExtras(int aExtras) { this.aExtras = aExtras; }

    public double getAOvers() { return aOvers; }
    public void setAOvers(double aOvers) { this.aOvers = aOvers; }

    public int getBRuns() { return bRuns; }
    public void setBRuns(int bRuns) { this.bRuns = bRuns; }

    public int getBWkts() { return bWkts; }
    public void setBWkts(int bWkts) { this.bWkts = bWkts; }

    public int getBExtras() { return bExtras; }
    public void setBExtras(int bExtras) { this.bExtras = bExtras; }

    public double getBOvers() { return bOvers; }
    public void setBOvers(double bOvers) { this.bOvers = bOvers; }
}
