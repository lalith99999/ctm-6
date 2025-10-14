package com.ctm.model;

import com.ctm.annotations.Column;
import com.ctm.annotations.Id;
import com.ctm.annotations.Table;
import com.ctm.annotations.Check;

@Table(name = "teams")
public class Team {

    @Id(auto = true)
    @Column(name = "team_id", nullable = false, unique = true)
    private long id;

    @Column(name = "name", length = 80, nullable = false, unique = true)
    @Check("LENGTH(name) > 0")
    private String name;

    @Column(name = "city", length = 80, nullable = false)
    private String city;

    public Team() {}
    public Team(long id, String name, String city) {
        this.id = id;
        this.name = name;
        this.city = city;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }

    public void setName(String name) { this.name = name; }
    public void setCity(String city) { this.city = city; }
}
