/**--------------------------------------------------------------------------------
 * THESE WARRANTIES GIVE YOU SPECIFIC LEGAL RIGHTS AND YOU MAY ALSO HAVE OTHER RIGHTS
 * WHICH VARY FROM STATE TO STATE OR JURISDICTION TO JURISDICTION.
 *
 * WARRANTY DISCLAIMER. EXCEPT AS STATED HEREIN, CHECK POINT MAKES NO WARRANTIES WITH
 * RESPECT TO ANY HARDWARE PRODUCT, LICENSE OR SERVICE AND DISCLAIMS ALL STATUTORY OR
 * IMPLIED WARRANTIES, INCLUDING WITHOUT LIMITATION WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR ARISING FROM A COURSE OF DEALING OR USAGE OF
 * TRADE AND ANY WARRANTIES OF NONINFRINGEMENT. CHECK POINT DOES NOT WARRANT THAT
 * THE CHECK POINT HARDWARE PRODUCT(S) WILL MEET ANY REQUIREMENTS OR THAT THE
 * OPERATION OF CHECK POINT HARDWARE PRODUCTS WILL BE UNINTERRUPTED OR ERROR FREE.
 --------------------------------------------------------------------------------**/
package me.blocksmc.annihilation.game;

import me.blocksmc.annihilation.team.TeamType;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;

public class AnniWorld {

    private World world; //  The world instance of the Annihilation world.
    private ArrayList<Location> blueSpawnpoints = new ArrayList<>(); //  The blue's spawnpoints.
    private ArrayList<Location> redSpawnpoints = new ArrayList<>(); //  The red's spawnpoints.
    private ArrayList<Location> greenSpawnpoints = new ArrayList<>(); //  The green's spawnpoints.
    private ArrayList<Location> yellowSpawnpoints = new ArrayList<>(); //  The yellow's spawnpoints.

    private Nexus blueNexus; // The blue's nexus.
    private Nexus redNexus; // The red's nexus.
    private Nexus greenNexus; //  The green's nexus.
    private Nexus yellowNexus; //  The yellow's nexus.

    public int totalNexusesDestroyed = 0; //  Total nexuses destroyed.

    public AnniWorld(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public ArrayList<Location> getSpawnpoints(TeamType teamType) {
        switch (teamType) {
            case BLUE: return blueSpawnpoints;
            case RED: return redSpawnpoints;
            case YELLOW: return yellowSpawnpoints;
            case GREEN: return greenSpawnpoints;
            default: return null;
        }
    }

    public void addSpawnpoint(TeamType teamType, Location location) {
        getSpawnpoints(teamType).add(location);
    }

    public Nexus getTeamNexus(TeamType teamType) {
        switch (teamType) {
            case BLUE: return blueNexus;
            case RED: return redNexus;
            case YELLOW: return yellowNexus;
            case GREEN: return greenNexus;
            default: return null;
        }
    }
    public void setTeamNexus(TeamType teamType, Nexus nexus) {
        switch (teamType) {
            case RED: { redNexus = nexus; break; }
            case BLUE: { blueNexus = nexus; break; }
            case GREEN: { greenNexus = nexus; break; }
            case YELLOW: { yellowNexus = nexus; break; }
            default: break;
        }
    }
}
