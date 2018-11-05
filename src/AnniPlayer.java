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
package me.blocksmc.annihilation;

import me.blocksmc.annihilation.lang.Language;
import me.blocksmc.annihilation.npc.NPC;
import me.blocksmc.annihilation.scorboard.AnnihilationScoreboard;
import me.blocksmc.annihilation.team.TeamType;
import me.blocksmc.annihilation.util.BoxCharacterType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AnniPlayer {

    private Player player;
    private TeamType teamType;
    private AnnihilationScoreboard annihilationScoreboard;

    private String locale;
    private Language language;

    private boolean isInGame;

    private ArrayList<NPC> clientNPCs = new ArrayList<>();
    public ArrayList<NPC> bufferedNPCs = new ArrayList<>();

    public static HashMap<String, AnniPlayer> list = new HashMap<>();

    /**
     * Constructor for a new Annihilation player.
     *
     * @param player The CraftBukkit player.
     */
    public AnniPlayer(Player player) {
        this.player = player;

        list.put(player.getName(), this);
    }

    /**
     * Sets the annihilation player's team.
     *
     * @param teamType The team type to set the player to.
     */
    public void setTeam(TeamType teamType) {
        if(this.teamType != null) this.teamType.getMembers().remove(getPlayer());

        this.teamType = teamType;
        teamType.getMembers().add(getPlayer());
    }

    /**
     * Gets the AnniPlayer's NPCs.
     *
     * @return The clientNPCs.
     */
    public ArrayList<NPC> getNPCsInView() {
        return clientNPCs;
    }

    /**
     * Gets the annihilation player's team.
     *
     * @return The player's team type.
     */
    public TeamType getTeam() {
        return teamType;
    }

    /**
     * Gets the player's annihilation scoreboard.
     *
     * @return The player's annihilation scoreboard.
     */
    public AnnihilationScoreboard getAnnihilationScoreboard() {
        return annihilationScoreboard;
    }

    /**
     * Gets the player's language.
     *
     * @return The language.
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * Sets the player's language.
     *
     * @param language The language.
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * Gets the player's locale (Example: "en_US")
     *
     * @return The locale string.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets the player's locale (Example: "en_US")
     *
     * @param locale The locale string.
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Gets the player's annihilation scoreboard.
     *
     * @param scoreboard The new player's annihilation scoreboard.
     */
    public void setAnnihilationScoreboard(AnnihilationScoreboard scoreboard) {
        this.annihilationScoreboard = scoreboard;
    }

    /**
     * Gets the CraftBukkit player.
     * @return The CraftBukkit player.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Sets the CraftBukkit player.
     * @param newPlayer The CraftBukkit player.
     */
    public void setPlayer(Player newPlayer) {
        this.player = newPlayer;
    }

    public boolean isInGame() {
        return isInGame;
    }

    public void setInGame(boolean newIsInGame) {
        this.isInGame = newIsInGame;
    }

    /**
     * Sends a box message to the Annihilation player.
     *
     * @param type The type of the box.
     * @param bc The background color.
     * @param fc The foreground color.
     * @param line1 The first line. (Can be null)
     * @param line2 The second line. (Can be null)
     */
    public void sendBoxCharacter(BoxCharacterType type, ChatColor bc, ChatColor fc, String line1, String line2, String line3) {
        if(type.equals(BoxCharacterType.ONE)) {
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒▒▒▒"+fc+"▒▒"+bc+"▒▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒▒▒▒"+bc+"▒▒▒▒");
            player.sendMessage(bc+"▒▒▒▒"+fc+"▒▒"+bc+"▒▒▒▒");
            player.sendMessage(bc+"▒▒▒▒"+fc+"▒▒"+bc+"▒▒▒▒" + " " + line1);
            player.sendMessage(bc+"▒▒▒▒"+fc+"▒▒"+bc+"▒▒▒▒" + " " + line2);
            player.sendMessage(bc+"▒▒▒▒"+fc+"▒▒"+bc+"▒▒▒▒" + " " + line3);
            player.sendMessage(bc+"▒▒▒▒"+fc+"▒▒"+bc+"▒▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒▒▒▒▒▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
        }
        if(type.equals(BoxCharacterType.TWO)) {
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒▒▒"+fc+"▒▒▒▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒"+fc+"▒"+bc+"▒▒▒" + " " + line1);
            player.sendMessage(bc+"▒▒▒▒▒"+fc+"▒"+bc+"▒▒▒▒" + " " + line2);
            player.sendMessage(bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒▒▒▒" + " " + line3);
            player.sendMessage(bc+"▒▒▒"+fc+"▒"+bc+"▒▒▒▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒▒▒▒▒▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
        }
        if(type.equals(BoxCharacterType.THREE)) {
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒▒▒"+fc+"▒▒▒▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒▒▒"+fc+"▒▒▒"+bc+"▒▒▒" + " " + line1);
            player.sendMessage(bc+"▒▒▒▒▒▒▒"+fc+"▒"+bc+"▒▒" + " " + line2);
            player.sendMessage(bc+"▒▒▒▒▒▒▒"+fc+"▒"+bc+"▒▒" + " " + line3);
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒▒"+fc+"▒▒▒▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
        }
        if(type.equals(BoxCharacterType.FOUR)) {
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒"+fc+"▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒▒▒▒"+fc+"▒▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒▒▒"+fc+"▒"+bc+"▒"+fc+"▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒▒"+fc+"▒"+bc+"▒▒"+fc+"▒"+bc+"▒▒▒" + " " + line1);
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒"+fc+"▒"+bc+"▒▒▒" + " " + line2);
            player.sendMessage(bc+"▒▒"+fc+"▒▒▒▒▒▒"+bc+"▒▒" + " " + line3);
            player.sendMessage(bc+"▒▒▒▒▒▒"+fc+"▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒"+fc+"▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
        }
        if(type.equals(BoxCharacterType.FIVE)) {
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒▒▒▒▒▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒▒▒▒▒"+bc+"▒▒▒" + " " + line1);
            player.sendMessage(bc+"▒▒▒▒▒▒▒"+fc+"▒"+bc+"▒▒" + " " + line2);
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒" + " " + line3);
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒▒"+fc+"▒▒▒▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
        }
        if(type.equals(BoxCharacterType.R)) {
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒▒▒▒▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒▒▒▒▒"+bc+"▒▒▒" + " " + line1);
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒"+fc+"▒"+bc+"▒▒▒▒▒" + " " + line2);
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒" + " " + line3);
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒"+fc+"▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
        }
        if(type.equals(BoxCharacterType.B)) {
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒▒▒▒▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒" + " " + line1);
            player.sendMessage(bc+"▒▒"+fc+"▒▒▒▒▒"+bc+"▒▒▒" + " " + line2);
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒" + " " + line3);
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒▒▒▒▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
        }
        if(type.equals(BoxCharacterType.G)) {
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒▒▒"+fc+"▒▒▒▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒▒▒▒" + " ");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒▒▒▒" + " " + line1);
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒▒▒▒" + " " + line2);
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒"+fc+"▒▒▒"+bc+"▒▒" + " " + line3);
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒▒"+fc+"▒▒▒▒"+bc+"▒▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
        }
        if(type.equals(BoxCharacterType.Y)) {
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒"+fc+"▒"+bc+"▒▒▒▒"+fc+"▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒▒"+fc+"▒"+bc+"▒▒"+fc+"▒"+bc+"▒▒▒" + " " + line1);
            player.sendMessage(bc+"▒▒▒▒"+fc+"▒▒"+bc+"▒▒▒▒" + " " + line2);
            player.sendMessage(bc+"▒▒▒▒"+fc+"▒▒"+bc+"▒▒▒▒" + " " + line3);
            player.sendMessage(bc+"▒▒▒▒"+fc+"▒▒"+bc+"▒▒▒▒");
            player.sendMessage(bc+"▒▒▒▒"+fc+"▒▒"+bc+"▒▒▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
        }
        if(type.equals(BoxCharacterType.E)) {
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒"+fc+"▒▒▒▒▒▒▒"+bc+"▒▒");
            player.sendMessage(bc+"▒"+fc+"▒"+bc+"▒▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒"+fc+"▒"+bc+"▒▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒"+fc+"▒"+bc+"▒▒▒▒▒▒▒▒" + " " + line1);
            player.sendMessage(bc+"▒"+fc+"▒▒▒▒▒▒▒"+bc+"▒▒" + " " + line2);
            player.sendMessage(bc+"▒"+fc+"▒"+bc+"▒▒▒▒▒▒▒▒" + " " + line3);
            player.sendMessage(bc+"▒"+fc+"▒"+bc+"▒▒▒▒▒▒▒▒");
            player.sendMessage(bc+"▒"+fc+"▒▒▒▒▒▒▒"+bc+"▒▒");
            player.sendMessage(bc+"▒▒▒▒▒▒▒▒▒▒");
        }
    }
}
