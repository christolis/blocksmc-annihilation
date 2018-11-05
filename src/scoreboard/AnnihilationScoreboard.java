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

package me.blocksmc.annihilation.scorboard;

import com.sun.istack.internal.NotNull;
import me.blocksmc.annihilation.team.TeamType;
import me.blocksmc.annihilation.util.Util;
import net.minecraft.server.v1_8_R3.ScoreboardScore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;

/**
 * AnnihilationScoreboard.class
 *
 * @author Chris Sdogkos
 * @since 0.1
 */
public class AnnihilationScoreboard {

    public static ArrayList<AnnihilationScoreboard> scoreboards = new ArrayList<>(); //  A hash-map that stores all the client-made scoreboards.

    private Player player;                                                                 //  The owner of the scoreboard.
    private static ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();    //  Private field of the scoreboard manager; used for constructor(s).
    private Scoreboard scoreboard = scoreboardManager.getNewScoreboard();                  //  Private field of the scoreboard; used for constructor(s).
    private String scoreboardTitle;                                                        //  Private field of the scoreboard's title; used for constructor(s).
    private Objective objective;                                                           //  Private field of the objective; used for constructor(s).
    public String[] lines = new String[100];                                               //  Private field of the scoreboard's lines; used for constructor(s).
    private String objectiveID;                                                            //  Private field of the objective's ID; used for constructor(s).
    private AnniScoreboardType scoreboardType;                                             //  Private field of the objective's ID; used for constructor(s).

    /**
     * [CONSTRUCTOR] Initializes a client scoreboard.
     * NOTE: If you want to alter it, use the other methods.
     *
     * @param player The owner of the new scoreboard.
     * @throws NullPointerException Gets thrown when an object is null.
     */
    public AnnihilationScoreboard(@NotNull Player player, String scoreboardTitle) throws NullPointerException {
        this.player             = player;                                                     //  Sets the class' 'player' variable to have the value of the generated one.
        this.scoreboardTitle    = scoreboardTitle;                                            //  Sets the class' 'scoreboardTitle' variable to have the value of the generated one.
        this.objectiveID        = "s_" + Util.generateRandomString(10);                  //  Sets the class' 'scoreboardID' variable to have the value of the generated one.
        this.objective          = scoreboard.registerNewObjective(objectiveID, "dummy"); //  Registers a new objective-type of scoreboard with the player's name as a title.

        objective.setDisplaySlot(DisplaySlot.SIDEBAR); //  Sets the display slot of the objective-scoreboard we made to show in the sidebar.
        objective.setDisplayName(scoreboardTitle); //  Sets the display name of the objective-scoreboard we made to have a title with the value of the variable 'scoreboardTitle'.

        scoreboards.add(this); //  Finally, adds the constructed scoreboard to the 'scoreboards' list.

        getScoreboard().registerNewTeam("Red"); getTeam(TeamType.RED).setPrefix(ChatColor.WHITE + "[" + ChatColor.RED + "R" + ChatColor.WHITE + "] " + ChatColor.RED);
        getScoreboard().registerNewTeam("Blue"); getTeam(TeamType.BLUE).setPrefix(ChatColor.WHITE + "[" + ChatColor.BLUE + "B" + ChatColor.WHITE + "] " + ChatColor.BLUE);
        getScoreboard().registerNewTeam("Green"); getTeam(TeamType.GREEN).setPrefix(ChatColor.WHITE + "[" + ChatColor.GREEN + "G" + ChatColor.WHITE + "] " + ChatColor.GREEN);
        getScoreboard().registerNewTeam("Yellow"); getTeam(TeamType.YELLOW).setPrefix(ChatColor.WHITE + "[" + ChatColor.YELLOW + "Y" + ChatColor.WHITE + "] " + ChatColor.YELLOW);
        getScoreboard().registerNewTeam("None"); getTeam(TeamType.NONE).setPrefix(ChatColor.GRAY + "");

        System.out.println("DEBUG: Registered " + this.objectiveID);
    }

    /**
     * Gets the display name of a scoreboard.
     *
     * @return The display name of the scoreboard.
     */
    public String getDisplayName() {
        return scoreboardTitle;
    }

    /**
     * Sets the display name of a scoreboard.
     *
     * @param scoreboardTitle The new display name of the scoreboard.
     */
    public void setDisplayName(String scoreboardTitle) {
        this.scoreboardTitle = scoreboardTitle;
        objective.setDisplayName(this.scoreboardTitle);
    }


    /**
     * Sets the line of an object-oriented scoreboard.
     * NOTE: If exists, will replace.
     *
     * @param text The string to set.
     * @param line The line to set.
     */
    public void setLine(String text, int line) {
        Score score = this.objective.getScore(text); //  Add the line.
        score.setScore(line); //  Set the line's number.
        lines[line] = text; //  Set the inputted line to the variable.
    }

    /**
     * Removes a line from the object-oriented scoreboard.
     *
     * @param string The string to remove from the scoreboard (Capital/Color case)
     */
    public void removeLine(String string) {
        Score score = this.objective.getScore(string);  //  Get the scoreboard.
        int line    = score.getScore();                 //  Get the string's line.

        scoreboard.resetScores(string); //  Hides it from the scoreboard.
        lines[line] = null; //  Updates the score and line to null.
    }

    /**
     * Returns the scoreboard.
     *
     * @return The scoreboard.
     */
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    /**
     * Returns the objective's ID.
     *
     * @return The objective's ID.
     */
    public String getObjectiveID() {
        return objectiveID;
    }

    /**
     * Returns the objective.
     *
     * @return The objective.
     */
    public Objective getObjective() {
        return objective;
    }
    /**
     * Gets the owner of the scoreboard.
     *
     * @return The owner (player).
     */
    public Player getOwner() {
        return player;
    }

    /**
     * Gets the type of the scoreboard.
     *
     * @return The scoreboard's type.
     */
    public AnniScoreboardType getType() {
        return scoreboardType;
    }

    /**
     * Sets the typ of the scoreboard.
     *
     * @param scoreboardType The new scoreboard's type.
     */
    public void setType(AnniScoreboardType scoreboardType) {
        this.scoreboardType = scoreboardType;
    }

    /**
     * Gets the corresponding org.bukkit.scoreboard.Team from a TeamType.
     *
     * @param teamType The input TeamType.
     * @return The returned org.bukkit.scoreboard.Team.
     */
    public Team getTeam(TeamType teamType) {
        switch(teamType) {
            case RED: return this.getScoreboard().getTeam("Red");
            case BLUE: return this.getScoreboard().getTeam("Blue");
            case YELLOW: return this.getScoreboard().getTeam("Yellow");
            case GREEN: return this.getScoreboard().getTeam("Green");
            case NONE: return this.getScoreboard().getTeam("None");
            default: return null;
        }
    }
}
