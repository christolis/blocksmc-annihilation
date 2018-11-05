package me.blocksmc.annihilation.game;

import me.blocksmc.annihilation.team.TeamType;
import org.bukkit.Location;

public class Nexus {

    private final Location location;
    private int health;
    private boolean destroyed = false;
    private TeamType team;

    public final int maxHealth;

    public Nexus(Location location, int health) {
        this.location = location;
        this.health = health;
        maxHealth = health;
    }

    public Location getLocation() {
        return location;
    }

    public int getHealth() {
        return health;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

}
