package com.ctm.model;

import com.ctm.annotations.Check;
import com.ctm.annotations.Column;
import com.ctm.annotations.Id;
import com.ctm.annotations.Table;

@Table(name = "tournaments")
public class Tournament {

    @Id(auto = true)
    @Column(name = "tournament_id", nullable = false, unique = true)
    private long id;

    @Column(name = "name", length = 100, nullable = false, unique = true)
    @Check("LENGTH(name) > 0")
    private String name;

    @Column(name = "format", length = 20, nullable = false)
    private String format;

    public Tournament() {}

    public Tournament(long id, String name, String format) {
        this.id = id;
        this.name = name;
        this.format = format;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getFormat() { return format; }

    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setFormat(String format) { this.format = format; }
}
