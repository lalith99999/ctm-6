package com.ctm.model;

import com.ctm.annotations.Check;
import com.ctm.annotations.Column;
import com.ctm.annotations.Id;
import com.ctm.annotations.Table;
import com.ctm.annotations.Transient;

@Table(name = "players")
public class Player {

    // 🧩 Global unique ID = teamId * 1000 + localJersey
    @Id(auto = false)
    @Column(name = "jersey_no", nullable = false, unique = true)
    @Check("jersey_no > 0")
    private long globalJerseyNumber;

    // 🧩 Local jersey visible in UI only (not persisted)
    @Transient
    private transient long localJerseyNumber;

    // 🧩 Player name (only alphabets + spaces allowed)
    @Column(name = "name", length = 100, nullable = false)
    @Check("CHAR_LENGTH(name) BETWEEN 2 AND 100")
    private String name;

    // 🧩 Role type
    @Column(name = "type", length = 20, nullable = false)
    @Check("type IN ('BATTER','BOWLER','ALL_ROUNDER','WICKET_KEEPER')")
    private PlayerType type;

    public Player() {}

    // ✅ Constructed directly from DB (global ID stored; local derived)
    public Player(long globalJerseyNumber, String name, PlayerType type) {
        this.globalJerseyNumber = globalJerseyNumber;
        this.localJerseyNumber = globalJerseyNumber % 1000;
        this.name = name;
        this.type = type;
    }

    // ✅ Helper: Construct from UI (teamId + local jersey)
    public static Player fromLocal(long teamId, long localJersey, String name, PlayerType type) {
        if (localJersey <= 0 || localJersey > 999)
            throw new IllegalArgumentException("Jersey number must be between 1 and 999");
        if (!name.matches("[A-Za-z\\s]+"))
            throw new IllegalArgumentException("Player name must contain only alphabets and spaces");

        Player p = new Player();
        p.globalJerseyNumber = teamId * 1000 + localJersey;
        p.localJerseyNumber = localJersey;
        p.name = name.trim();
        p.type = type;
        return p;
    }

    // --- Getters ---
    public long getGlobalJerseyNumber() { return globalJerseyNumber; }
    public long getJerseyNumber() { return localJerseyNumber; }  // Used in JSP/UI
    public String getName() { return name; }
    public PlayerType getType() { return type; }

    // --- Setters ---
    public void setGlobalJerseyNumber(long globalJerseyNumber) {
        this.globalJerseyNumber = globalJerseyNumber;
        this.localJerseyNumber = globalJerseyNumber % 1000;
    }

    public void setJerseyNumber(long localJerseyNumber) {
        if (localJerseyNumber <= 0 || localJerseyNumber > 999)
            throw new IllegalArgumentException("Invalid local jersey number");
        this.localJerseyNumber = localJerseyNumber;
    }

    public void setName(String name) {
        if (!name.matches("[A-Za-z\\s]+"))
            throw new IllegalArgumentException("Player name must contain only alphabets and spaces");
        this.name = name.trim();
    }

    public void setType(PlayerType type) { this.type = type; }

    @Override
    public String toString() {
        return "Player{#" + localJerseyNumber + ", name='" + name + "', role=" + type + "}";
    }
}
