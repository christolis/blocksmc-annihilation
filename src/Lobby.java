package me.blocksmc.annihilation;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

/**
 * Lobby.java
 *
 * @author Chris Sdogkos
 * @since 0.0
 */
public class Lobby {

    public static Location spawnPoint = new Location(Bukkit.getWorld("world"), 118.0, 82.0, 61.0, 90, 0); //  The lobby's spawnpoint.
    public static ArrayList<Player> onlinePlayers = new ArrayList<>(); //  The total players in the lobby.

    /**
     * Teleports a player to the lobby's spawnpoint.
     *
     * @param player The player to teleport.
     */
    public static void spawnPlayer(Player player) {
        AnniPlayer anniPlayer = AnniPlayer.list.get(player.getName());
        anniPlayer.setInGame(false);
        player.teleport(spawnPoint);
    }
}
