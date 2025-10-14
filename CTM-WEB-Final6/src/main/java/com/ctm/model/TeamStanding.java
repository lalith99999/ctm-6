package com.ctm.model;

public class TeamStanding {
    private long teamId;
    private String name;
    private String city;
    private int points;
    private double nrr;
    private int played;

    public TeamStanding(long teamId, String name, String city,
                        int points, double nrr, int played) {
        this.teamId = teamId;
        this.name = name;
        this.city = city;
        this.points = points;
        this.nrr = nrr;
        this.played = played;
    }

    public long getTeamId() { return teamId; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public int getPoints() { return points; }
    public double getNrr() { return nrr; }
    public int getPlayed() { return played; }
}
