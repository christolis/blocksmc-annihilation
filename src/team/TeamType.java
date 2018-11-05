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
package me.blocksmc.annihilation.team;

import me.blocksmc.annihilation.AnniPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;

public enum TeamType {
    RED, BLUE, GREEN, YELLOW, NONE;

    public static ArrayList<Player> redPlayers = new ArrayList<>(); //  The red team's players list.
    public static ArrayList<Player> bluePlayers = new ArrayList<>(); //  The blue team's players list.
    public static ArrayList<Player> greenPlayers = new ArrayList<>(); //  The green team's players list.
    public static ArrayList<Player> yellowPlayers = new ArrayList<>(); //  The yellow team's players list.
    public static ArrayList<Player> nonePlayers = new ArrayList<>(); //  The none team's players list.

    private ArrayList<Player> teamList; //  Responsible field that assigns the team's corresponding players ArrayList.
    private ChatColor chatColor; //  Responsible field that assigns the team's corresponding Chat Color.
    private ChatColor darkerChatColor; //  Responsible field that assigns the team's corresponding Chat Color.

    static {
        RED.teamList = redPlayers; //  Sets the RED's enum to redPlayers.
        RED.chatColor = ChatColor.RED; //  Sets the RED team's color to RED.
        RED.darkerChatColor = ChatColor.DARK_RED;

        BLUE.teamList = bluePlayers; //  Sets the BLUE's enum to bluePlayers.
        BLUE.chatColor = ChatColor.BLUE; //  Sets the BLUE team's color to BLUE.
        BLUE.darkerChatColor = ChatColor.DARK_BLUE;

        GREEN.teamList = greenPlayers; //  Sets the GREEN's enum to greenPlayers.
        GREEN.chatColor = ChatColor.GREEN; //  Sets the GREEN team's color to GREEN.
        GREEN.darkerChatColor = ChatColor.DARK_GREEN;

        YELLOW.teamList = yellowPlayers; //  Sets the YELLOW's enum to yellowPlayers.
        YELLOW.chatColor = ChatColor.YELLOW; //  Sets the YELLOW team's color to YELLOW.
        YELLOW.darkerChatColor = ChatColor.GOLD;

        NONE.teamList = nonePlayers; //  Sets the NONE's enum to yellowPlayers.
        NONE.chatColor = ChatColor.GRAY; //  Sets the NONE team's color to YELLOW.
        NONE.darkerChatColor = ChatColor.DARK_GRAY;
    }

    /**
     * Gets the members of the team.
     *
     * @return The corresponding ArrayList of the specified enum.
     */
    public ArrayList<Player> getMembers() {
        return teamList;
    }

    /**
     * Gets the team's color.
     *
     * @return The team's color.
     */
    public ChatColor getColor() {
        return chatColor;
    }

    /**
     * Gets the team's color.
     *
     * @return The team's color.
     */
    public ChatColor getDarkerColor() {
        return darkerChatColor;
    }

    /**
     * Checks if the player is in the team.
     *
     * @param player The player to check.
     * @return False if not, true if yes.
     */
    public boolean containsPlayer(Player player) {
        return teamList.contains(player);
    }

    /**
     * Checks if the player is in any team.
     * @param player The player to check.
     * @return False if not, true if yes.
     */
    public static boolean isPlayerInAnyTeam(Player player) {
        if (AnniPlayer.list.get(player.getName()).getTeam() == TeamType.NONE || AnniPlayer.list.get(player.getName()).getTeam() == null) {
            return false;
        }
        return true;
    }

    /**
     * Gets the team provided by string.
     *
     * @param string The input.
     * @return The TeamType if input is correct, null if not.
     */
    public static TeamType getTeamByString(String string) {
        switch(string) {
            case "red": return TeamType.RED;
            case "blue": return TeamType.BLUE;
            case "green": return TeamType.GREEN;
            case "yellow": return TeamType.YELLOW;
            case "none": return TeamType.NONE;
            default: return null;
        }
    }
}
