package com.ctm.model;

import com.ctm.annotations.Check;
import com.ctm.annotations.Column;
import com.ctm.annotations.Table;

@Table(name = "match_team_scores")
public class MatchTeamScore {

    @Column(name = "runs", nullable = false, defaultValue = "0")
    @Check("runs >= 0")
    private int runs;

    @Column(name = "wickets", nullable = false, defaultValue = "0")
    @Check("wickets BETWEEN 0 AND 10")
    private int wickets;

    @Column(name = "extras", nullable = false, defaultValue = "0")
    @Check("extras >= 0")
    private int extras;

    @Column(name = "overs", nullable = false, defaultValue = "0")
    @Check("overs >= 0")
    private double overs;

    public int getRuns() { return runs; }
    public int getWickets() { return wickets; }
    public int getExtras() { return extras; }
    public double getOvers() { return overs; }

    /** Add increments to the current score */
    public void add(int addRuns, int addWkts, int addExtras, double addOvers) {
        this.runs += addRuns;
        this.wickets += addWkts;
        this.extras += addExtras;
        this.overs += addOvers;
    }

    /** Reset score to 0s */
    public void reset() {
        this.runs = 0;
        this.wickets = 0;
        this.extras = 0;
        this.overs = 0.0;
    }
}
