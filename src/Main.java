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

import io.netty.channel.*;
import me.blocksmc.annihilation.game.AnniWorld;
import me.blocksmc.annihilation.game.Game;
import me.blocksmc.annihilation.game.Nexus;
import me.blocksmc.annihilation.lang.Language;
import me.blocksmc.annihilation.map.MapTest;
import me.blocksmc.annihilation.npc.NPC;
import me.blocksmc.annihilation.scorboard.AnniScoreboardType;
import me.blocksmc.annihilation.scorboard.AnnihilationScoreboard;
import me.blocksmc.annihilation.team.TeamType;
import me.blocksmc.annihilation.util.BoxCharacterType;
import me.blocksmc.annihilation.util.PlayerInjector;
import me.blocksmc.annihilation.util.Util;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.collect.ComparisonChain.start;
import static me.blocksmc.annihilation.game.Game.MAX_PHASES;

public class Main extends JavaPlugin implements Listener {

    public static HashMap<Player, NPC> redTeam = new HashMap<>();
    public static HashMap<Player, NPC> blueTeam = new HashMap<>();
    public static HashMap<Player, NPC> greenTeam = new HashMap<>();
    public static HashMap<Player, NPC> yellowTeam = new HashMap<>();

    public static HashMap<Player, Boolean> canUseNPCs = new HashMap<>();

    public static HashMap<Projectile, Float> projectileDistList = new HashMap<>();
    public static ArrayList<Player> injectedPeople = new ArrayList<>();
    public static ArrayList<Player> flyingPeople = new ArrayList<>();

    public static int remainingTimerID;
    public static int instanceTickID;

    public static Game game;

    /**
     * Called when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        Logger logger = Bukkit.getLogger();

        logger.log(Level.INFO, "[Annihilation] Plugin has been enabled!");
        Bukkit.getPluginManager().registerEvents(this, this);

        //  Loads the maps.
        try {
            this.getServer().createWorld(new WorldCreator("map_cobble_v2"));
            Util.copyWorld(new File("map_cobble_v2"), new File("map_cobble_v2_game"));
            this.getServer().createWorld(new WorldCreator("map_cobble_v2_game"));
            Bukkit.getWorld("map_cobble_v2_game").setGameRuleValue("doMobSpawning", "false");

            this.getServer().createWorld(new WorldCreator("map_grass_v2"));
            Util.copyWorld(new File("map_grass_v2"), new File("map_grass_v2_game"));
            this.getServer().createWorld(new WorldCreator("map_grass_v2_game"));
            Bukkit.getWorld("map_grass_v2_game").setGameRuleValue("doMobSpawning", "false");

            this.getServer().createWorld(new WorldCreator("map_stone_v2"));
            Util.copyWorld(new File("map_stone_v2"), new File("map_stone_v2_game"));
            this.getServer().createWorld(new WorldCreator("map_stone_v2_game"));
            Bukkit.getWorld("map_stone_v2_game").setGameRuleValue("doMobSpawning", "false");

            this.getServer().createWorld(new WorldCreator("map_firstmap"));
            Util.copyWorld(new File("map_firstmap"), new File("map_firstmap_game"));
            this.getServer().createWorld(new WorldCreator("map_firstmap_game"));
            Bukkit.getWorld("map_firstmap_game").setGameRuleValue("doMobSpawning", "false");

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        game = new Game(null, 1, 120); //  Debug.
        game.setMap(new AnniWorld(Bukkit.getWorld("map_grass_v2_game"))); //  Debug.

        for (Player player : Bukkit.getOnlinePlayers()) {
            Bukkit.getServer().getPluginManager().callEvent(new PlayerQuitEvent(player, null));
            Bukkit.getServer().getPluginManager().callEvent(new PlayerJoinEvent(player, null));
        }

        remainingTimerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                int playersLeft = game.getMinPlayers() - Lobby.onlinePlayers.size();

                try {
                    Util.sendHotbarMessage(player, ChatColor.GRAY + "" + playersLeft + ChatColor.DARK_AQUA + " players remaining to start the game!", false);
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        }, 0L, 20L);

        instanceTickID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (Map.Entry<Projectile, Float> projectileEntry : projectileDistList.entrySet()) {
                System.out.println("Iterated through a projectile!");
                Projectile projectile = projectileEntry.getKey();
                Float distance = projectileEntry.getValue();

                int radius = 1;
                Location trigoLocation = new Location(
                        projectile.getWorld(),
                        projectile.getLocation().getX() + (Math.sin(distance) * radius),
                        projectile.getLocation().getY() + (Math.sin(distance) * radius),
                        projectile.getLocation().getZ() + (Math.cos(distance) * radius)
                );

                Util.spawnWorldParticle(projectile.getWorld(), trigoLocation, EnumParticle.FLAME, null, 0);
                projectileDistList.replace(projectile, distance + 0.5f);
            }
        }, 0L, 0L);
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        Util.unloadWorld(Bukkit.getWorld("map_cobble_v2_game"));
        Util.deleteWorld(new File("map_cobble_v2_game"));

        Util.unloadWorld(Bukkit.getWorld("map_grass_v2_game"));
        Util.deleteWorld(new File("map_grass_v2_game"));

        Util.unloadWorld(Bukkit.getWorld("map_stone_v2_game"));
        Util.deleteWorld(new File("map_stone_v2_game"));

        Util.unloadWorld(Bukkit.getWorld("map_firstmap_game"));
        Util.deleteWorld(new File("map_firstmap_game"));

        for (Player player : Bukkit.getOnlinePlayers()) {
            Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
            channel.eventLoop().submit(() -> {
                channel.pipeline().remove(player.getName());
                injectedPeople.remove(player);
                return null;
            });
        }

        System.setProperty("file.encoding","UTF-8");
        Field charset;
        try {
            charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null,null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when a player attempts to move.
     *
     * @param event An instance of the event.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        AnniPlayer anniPlayer = AnniPlayer.list.get(player.getName());

        for (NPC npc : anniPlayer.getNPCsInView()) {
            if (!npc.getNpc().getWorld().equals(player.getWorld())) return;
            System.out.println("What a surprise, I actually get called...");

            if (player.getLocation().distance(npc.getLocation()) > 50.0D && !anniPlayer.bufferedNPCs.contains(npc)) {
                anniPlayer.bufferedNPCs.add(npc);
                player.sendMessage("add to buffer");
            }
            if (player.getLocation().distance(npc.getLocation()) < 50.0D && anniPlayer.bufferedNPCs.contains(npc)) {
                anniPlayer.bufferedNPCs.remove(npc);
                npc.update(player);
                player.sendMessage("remove from buffer");
            }
        }
    }

    /**
     * Called when a player attempts to join the server.
     *
     * @param event An instance of the event.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) throws InvocationTargetException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        Player player = event.getPlayer();
        Inventory playerInventory = player.getInventory();
        Location playerLocation = player.getLocation();
        World playerWorld = player.getWorld();
        AnnihilationScoreboard annihilationScoreboard;
        AnniPlayer anniPlayer;

        //  If data is available for the player...
        if (AnniPlayer.list.containsKey(player.getName())) {
            anniPlayer = AnniPlayer.list.get(player.getName());
            anniPlayer.setPlayer(player);

            annihilationScoreboard = new AnnihilationScoreboard(player, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "ANNIHILATION");

            System.out.println("NOTE: The player's data existed when he/she joined!");
        } else { //  Data wasn't available for the player...
            anniPlayer = new AnniPlayer(player);
            annihilationScoreboard = new AnnihilationScoreboard(player, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "ANNIHILATION");

            anniPlayer.setTeam(TeamType.NONE);
            Util.setNameTagName(player, TeamType.NONE);
            System.out.println("NOTE: Made the player because no data was available for him/her.");
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            try {
                anniPlayer.setLanguage(new Language(Util.getLanguage(player)));
                System.out.println("Set " + anniPlayer.getPlayer().getName() + "'s language to: " + anniPlayer.getLanguage().getLocale());
                player.sendMessage(anniPlayer.getLanguage().string("LANGUAGE_SET"));
            } catch (InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }

            //  So now that we have the player's scoreboard 100%, we want to add EVERYONE in that new scoreboard so the player can see the player's teams visually.
            for (Player players : Bukkit.getOnlinePlayers()) {
                AnniPlayer anniPlayers = AnniPlayer.list.get(players.getName());

                System.out.println(Util.capitalize(anniPlayers.getTeam().toString().toLowerCase()));

                if(annihilationScoreboard == null) System.out.println("a");
                if(annihilationScoreboard.getScoreboard() == null) System.out.println("b");
                if(annihilationScoreboard.getScoreboard().getTeam(Util.capitalize(anniPlayers.getTeam().toString().toLowerCase())) == null)System.out.println("c");

                if(game.hasStarted()) annihilationScoreboard.getScoreboard().getTeam(Util.capitalize(anniPlayers.getTeam().toString().toLowerCase())).addEntry(players.getName());
                else annihilationScoreboard.getScoreboard().getTeam("None").addEntry(players.getName());
            }

            Lobby.onlinePlayers.add(player);  // Adds the player to the online players list.
            //TODO event.setJoinMessage(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "➦ " + ChatColor.DARK_AQUA + player.getName() + ChatColor.GRAY + " has joined annihilation! " + "(" + Lobby.onlinePlayers.size() + "/" + "120)"); // Sets our join message to null.
            Lobby.spawnPlayer(player); //  Teleports and spawns the player to the lobby.
            player.setGameMode(GameMode.ADVENTURE); //  Sets his gamemode to adventure.
            player.setHealth(20.0f); //  Restores his health to full.
            player.setFoodLevel(20); //  Restores his food to full.
            ((PlayerInventory) playerInventory).setArmorContents(null);  // Clears his armor inventory.
            playerInventory.clear();  // Clears his inventory.
            if (!game.hasStarted()) player.setExp(0); //  If the game hasn't started yet, set his exp to zero.
            canUseNPCs.put(player, true); //  Make the player able to interact with the NPCs.

            //  Checks if the player is not injected.
            if (!injectedPeople.contains(player)) {
                new PlayerInjector(player); //  Makes a new injector for the player.
                injectedPeople.add(player); //  Puts him to the injected list.
            }

            //  If the minimum players are surpassed and the game hasn't started.
            if(Lobby.onlinePlayers.size() >= game.getMinPlayers() && !game.getStartingCountdown() && !game.hasStarted()) {
                Bukkit.getScheduler().cancelTask(remainingTimerID); //  Cancel the remaining timer ID.
                try {
                    game.setStartCountdown(true); //  Start the game countdown.
                } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            //===========================================================================================
            //                                      SCOREBOARD STUFF
            //===========================================================================================
            if(game.hasStarted()) annihilationScoreboard.setType(AnniScoreboardType.GAME); else annihilationScoreboard.setType(AnniScoreboardType.LOBBY);

            //  Initializes some scoreboard types, purely design and should most likely be ignored if you are looking for any bugs here.
            if (annihilationScoreboard.getType() == AnniScoreboardType.LOBBY) {
                annihilationScoreboard.setLine(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "-------------------" + ChatColor.RESET, 13);
                annihilationScoreboard.setLine(ChatColor.WHITE + "✦ " + ChatColor.GRAY + anniPlayer.getLanguage().string("SCOREBOARD_LOBBY_PLAYERSONLINE") + ChatColor.AQUA + (Bukkit.getOnlinePlayers().size()), 12);
                annihilationScoreboard.setLine("", 11);
                annihilationScoreboard.setLine(ChatColor.WHITE + "✦ " + ChatColor.GRAY + anniPlayer.getLanguage().string("SCOREBOARD_LOBBY_KILLS") + ChatColor.GREEN + "0", 10);
                annihilationScoreboard.setLine(ChatColor.WHITE + "✦ " + ChatColor.GRAY + anniPlayer.getLanguage().string("SCOREBOARD_LOBBY_WINS") + ChatColor.GREEN + "0", 9);
                annihilationScoreboard.setLine(ChatColor.WHITE + "✦ " + ChatColor.GRAY + anniPlayer.getLanguage().string("SCOREBOARD_LOBBY_NEXUSDMG") + ChatColor.GREEN + "0", 8);
                annihilationScoreboard.setLine(" ", 7);
                annihilationScoreboard.setLine(ChatColor.GRAY + "      Level - " + ChatColor.GREEN + "0" + ChatColor.WHITE + "/" + ChatColor.GOLD + "1k", 6);
                annihilationScoreboard.setLine(ChatColor.WHITE + "   <" + ChatColor.GREEN + "" + ChatColor.GRAY + "■■■■■■■■■■" + ChatColor.WHITE + " >", 5);
                annihilationScoreboard.setLine("  ", 4);
                annihilationScoreboard.setLine(ChatColor.WHITE + "✦ " + ChatColor.GRAY + "Kit: " + ChatColor.LIGHT_PURPLE + "None", 3);
                annihilationScoreboard.setLine(ChatColor.WHITE + "✦ " + ChatColor.GRAY + anniPlayer.getLanguage().string("SCOREBOARD_LOBBY_TEAM") + ChatColor.RED + "None", 2);
                annihilationScoreboard.setLine(ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH + "-------------------", 1);
            } else {
                annihilationScoreboard.setDisplayName(ChatColor.DARK_AQUA + "" + anniPlayer.getLanguage().string("SCOREBOARD_GAME_MAP"));
                annihilationScoreboard.setLine(ChatColor.GRAY + " " + ChatColor.STRIKETHROUGH + "-------------------", 8);
                annihilationScoreboard.setLine(ChatColor.RED + anniPlayer.getLanguage().string("SCOREBOARD_GAME_REDNEXUS") + ChatColor.GRAY + ": " + ChatColor.WHITE + game.getMap().getTeamNexus(TeamType.RED).getHealth() + ChatColor.RED + " ❤", 7);
                annihilationScoreboard.setLine(ChatColor.BLUE + anniPlayer.getLanguage().string("SCOREBOARD_GAME_BLUENEXUS") + ChatColor.GRAY + ": " + ChatColor.WHITE + game.getMap().getTeamNexus(TeamType.BLUE).getHealth() +  ChatColor.RED + " ❤",6);
                annihilationScoreboard.setLine(ChatColor.GREEN + anniPlayer.getLanguage().string("SCOREBOARD_GAME_GREENNEXUS") + ChatColor.GRAY + ": " + ChatColor.WHITE + game.getMap().getTeamNexus(TeamType.GREEN).getHealth() + ChatColor.RED + " ❤", 5);
                annihilationScoreboard.setLine(ChatColor.YELLOW + anniPlayer.getLanguage().string("SCOREBOARD_GAME_YELLOWNEXUS") + ChatColor.GRAY + ": " + ChatColor.WHITE + game.getMap().getTeamNexus(TeamType.YELLOW).getHealth() + ChatColor.RED + " ❤", 4);
                annihilationScoreboard.setLine(" ", 3);
                if(annihilationScoreboard.lines[2] != null) annihilationScoreboard.removeLine(annihilationScoreboard.lines[2]);
                annihilationScoreboard.setLine(ChatColor.DARK_PURPLE + "  Phase:   I II III IV V", 2);
                annihilationScoreboard.setLine(ChatColor.GRAY + " " + ChatColor.STRIKETHROUGH + "-------------------" + ChatColor.RESET, 1);

                //  Translates the phase line.
                for (AnnihilationScoreboard annihilationScoreboards : AnnihilationScoreboard.scoreboards) {
                    if (annihilationScoreboards.getType() == AnniScoreboardType.GAME) {
                        int line = 2;
                        int s = 0;
                        StringBuilder phaseString = new StringBuilder();

                        for (int i = 1; i <= MAX_PHASES; i++) {
                            String newString;

                            if (i == game.getPhase()) newString = ChatColor.translateAlternateColorCodes('&', "&5" + Util.intToLatinCharacter(i) + " ");
                            else newString = ChatColor.translateAlternateColorCodes('&', "&7" + Util.intToLatinCharacter(i) + " ");

                            phaseString.insert(s, newString);
                            s += newString.length();
                        }

                        try {
                            annihilationScoreboards.removeLine(annihilationScoreboards.lines[line]);
                            annihilationScoreboards.setLine(ChatColor.DARK_PURPLE + "  Phase:   " + phaseString.toString(), line);
                        } catch (Exception e) {
                            System.out.println("Error in changing phase in scoreboard! :(");
                        }
                    }
                }
            }

            anniPlayer.setAnnihilationScoreboard(annihilationScoreboard);
            player.setScoreboard(annihilationScoreboard.getScoreboard());

            for (AnnihilationScoreboard annihilationScoreboards : AnnihilationScoreboard.scoreboards) {
                if (annihilationScoreboards.getType() == AnniScoreboardType.LOBBY) {
                    annihilationScoreboards.removeLine(ChatColor.WHITE + "✦ " + ChatColor.GRAY + anniPlayer.getLanguage().string("SCOREBOARD_LOBBY_PLAYERSONLINE") + ChatColor.AQUA + (Bukkit.getOnlinePlayers().size() - 1));
                    annihilationScoreboards.setLine(ChatColor.WHITE + "✦ " + ChatColor.GRAY + anniPlayer.getLanguage().string("SCOREBOARD_LOBBY_PLAYERSONLINE") + ChatColor.AQUA + (Bukkit.getOnlinePlayers().size()), 12);
                }

                player.setScoreboard(annihilationScoreboards.getScoreboard());
            }

            if (annihilationScoreboard.getType() == AnniScoreboardType.GAME) {
                System.out.println(annihilationScoreboard.getType());
                for(TeamType teamTypes : TeamType.values()) {
                    if(teamTypes == TeamType.NONE) continue;

                    boolean alive = false;

                    int line;
                    switch (teamTypes) {
                        case RED: { line = 7; break; }
                        case BLUE: { line = 6; break; }
                        case GREEN: { line = 5; break; }
                        case YELLOW: { line = 4; break; }
                        case NONE: { line = 0; break; }
                        default: {line = 0; break; }
                    }

                    for (Player players : teamTypes.getMembers()) {
                        AnniPlayer anniPlayers = AnniPlayer.list.get(players.getName());

                        if (anniPlayers != null) {
                            if (anniPlayers.isInGame()) {
                                alive = true;
                                break;
                            }
                        }
                    }

                    if(game.getMap().getTeamNexus(teamTypes).isDestroyed()) {
                        annihilationScoreboard.removeLine(annihilationScoreboard.lines[line]);

                        if (!alive) annihilationScoreboard.setLine(ChatColor.GRAY + "  " + ChatColor.STRIKETHROUGH + Util.capitalize(teamTypes.toString().toLowerCase()) + " Nexus" + ": " + game.getMap().getTeamNexus(teamTypes).getHealth() + " ❤", line);
                        if (alive) annihilationScoreboard.setLine(ChatColor.GRAY + "  " + Util.capitalize(teamTypes.toString().toLowerCase()) + " Nexus" + ": " + game.getMap().getTeamNexus(teamTypes).getHealth() + " ❤", line);
                    }
                }
            }
            //===========================================================================================

            //===========================================================================================
            //                               NON-PLAYABLE-CHARACTER STUFF
            //===========================================================================================
            int aa = 1;

            //  Red team.
            redTeam.put(player, new NPC(player, ChatColor.RED + anniPlayer.getLanguage().string("NPC_LOBBY_REDTEAM"), player.getUniqueId().toString(), new Location(playerWorld, 105.5, 79.0, 55.5), 0, 0, true)); //  Red team NPC.
            for (String suffix : Arrays.asList("Helmet", "Chestplate", "Leggings", "Boots")) {
                ItemStack armorPiece = new ItemStack(Material.valueOf("LEATHER_" + suffix.toUpperCase()));
                LeatherArmorMeta meta = (LeatherArmorMeta) armorPiece.getItemMeta();
                meta.setColor(Color.RED);
                armorPiece.setItemMeta(meta);
                redTeam.get(player).addEquipment(aa, armorPiece);
                aa++;
            }
            aa = 1;
            redTeam.get(player).addArmorStand(ChatColor.BOLD + anniPlayer.getLanguage().string("NPC_LOBBY_CLICK2JOIN"), null, true, new Location(redTeam.get(player).getLocation().getWorld(), redTeam.get(player).getLocation().getX(), redTeam.get(player).getLocation().getY() - 0.10f, redTeam.get(player).getLocation().getZ()));

            //  Yellow team.
            yellowTeam.put(player, new NPC(player, ChatColor.YELLOW + anniPlayer.getLanguage().string("NPC_LOBBY_YELLOWTEAM"), player.getUniqueId().toString(), new Location(playerWorld, 98.5, 79.0, 55.5), 0, 0, true)); //  Yellow team NPC.
            for (String suffix : Arrays.asList("Helmet", "Chestplate", "Leggings", "Boots")) {
                ItemStack armorPiece = new ItemStack(Material.valueOf("LEATHER_" + suffix.toUpperCase()));
                LeatherArmorMeta meta = (LeatherArmorMeta) armorPiece.getItemMeta();
                meta.setColor(Color.YELLOW);
                armorPiece.setItemMeta(meta);
                yellowTeam.get(player).addEquipment(aa, armorPiece);
                aa++;
            }
            aa = 1;
            yellowTeam.get(player).addArmorStand(ChatColor.BOLD + anniPlayer.getLanguage().string("NPC_LOBBY_CLICK2JOIN"), null, true, new Location(yellowTeam.get(player).getLocation().getWorld(), yellowTeam.get(player).getLocation().getX(), yellowTeam.get(player).getLocation().getY() - 0.10f, yellowTeam.get(player).getLocation().getZ()));

            //  Green team.
            greenTeam.put(player, new NPC(player, ChatColor.GREEN + anniPlayer.getLanguage().string("NPC_LOBBY_GREENTEAM"), player.getUniqueId().toString(), new Location(playerWorld, 94.5, 79.0, 61.5), -90, 0, true)); //  Green team NPC.
            for (String suffix : Arrays.asList("Helmet", "Chestplate", "Leggings", "Boots")) {
                ItemStack armorPiece = new ItemStack(Material.valueOf("LEATHER_" + suffix.toUpperCase()));
                LeatherArmorMeta meta = (LeatherArmorMeta) armorPiece.getItemMeta();
                meta.setColor(Color.GREEN);
                armorPiece.setItemMeta(meta);
                greenTeam.get(player).addEquipment(aa, armorPiece);
                aa++;
            }
            aa = 1;
            greenTeam.get(player).addArmorStand(ChatColor.BOLD + anniPlayer.getLanguage().string("NPC_LOBBY_CLICK2JOIN"), null, true, new Location(greenTeam.get(player).getLocation().getWorld(), greenTeam.get(player).getLocation().getX(), greenTeam.get(player).getLocation().getY() - 0.10f, greenTeam.get(player).getLocation().getZ()));

            //  Blue team.
            blueTeam.put(player, new NPC(player, ChatColor.BLUE + anniPlayer.getLanguage().string("NPC_LOBBY_BLUETEAM"), player.getUniqueId().toString(), new Location(playerWorld, 96.5, 79.0, 67.5), -90, 0, true)); //  Blue team NPC.
            for (String suffix : Arrays.asList("Helmet", "Chestplate", "Leggings", "Boots")) {
                ItemStack armorPiece = new ItemStack(Material.valueOf("LEATHER_" + suffix.toUpperCase()));
                LeatherArmorMeta meta = (LeatherArmorMeta) armorPiece.getItemMeta();
                meta.setColor(Color.BLUE);
                armorPiece.setItemMeta(meta);
                blueTeam.get(player).addEquipment(aa, armorPiece);
                aa++;
            }
            blueTeam.get(player).addArmorStand(ChatColor.BOLD + anniPlayer.getLanguage().string("NPC_LOBBY_CLICK2JOIN"), null, true, new Location(blueTeam.get(player).getLocation().getWorld(), blueTeam.get(player).getLocation().getX(), blueTeam.get(player).getLocation().getY() - 0.10f, blueTeam.get(player).getLocation().getZ()));

            anniPlayer.getNPCsInView().add(redTeam.get(player));
            anniPlayer.getNPCsInView().add(blueTeam.get(player));
            anniPlayer.getNPCsInView().add(greenTeam.get(player));
            anniPlayer.getNPCsInView().add(yellowTeam.get(player));

            //  For every NPC in the player's view...
            for (NPC npcs : anniPlayer.getNPCsInView()) {
                PlayerConnection connection = ((CraftPlayer) player.getPlayer()).getHandle().playerConnection; //  Gets the player's connection.

                npcs.setSkin(((CraftPlayer) player).getProfile()); //  Generates and sets the NPC's skin.
                npcs.show(player, true); //  Show it to the player.
                npcs.update(player); //  And finally update.
            }
            //===========================================================================================

            //===========================================================================================
            //                               COSMETIC DETAILS AND STUFF
            //===========================================================================================

            //  If the game has started and the player's existing data show that he is in a team.
            if (!(AnniPlayer.list.get(player.getName()).getTeam() == TeamType.NONE || AnniPlayer.list.get(player.getName()).getTeam() == null) && !game.getMap().getTeamNexus(anniPlayer.getTeam()).isDestroyed())
                game.teleportPlayerToArena(player); //  Teleport him to the arena back.
                player.damage(500.0f); //  Damage the player to prevent bugs I guess?

            //  Set the bukkit teams to each player if the game has started.
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                if(game.hasStarted()) {
                    if (anniPlayer.getTeam() != null) {
                        Util.setNameTagName(player, anniPlayer.getTeam()); //  Set his name tag to whatever was chosen.
                    } else { //  If the player's team is null, IT SHOULD NOT AND IS A FAT ERROR.
                        player.sendMessage(ChatColor.YELLOW + "NOTE: " + ChatColor.WHITE + "Your team is " + ChatColor.GRAY + "null" + ChatColor.WHITE + ", you should report this to the developer(s).");
                    }
                } else {
                    Util.setNameTagName(player, TeamType.NONE); //  Set his name tag to whatever was chosen.
                }
            }, 5L);

            //  Invisibility bug fix.
            for(Player players : Bukkit.getOnlinePlayers()) {
                players.hidePlayer(player);
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> players.showPlayer(player), 5L);
            }
        }, 5L);
    }

    /**
     * Called when a player attempts to quit the server.
     *
     * @param event An instance of the event.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) throws InvocationTargetException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        Player player = event.getPlayer();
        AnniPlayer anniPlayer = AnniPlayer.list.get(player.getName());

        AnniPlayer.list.get(player.getName()).getNPCsInView().clear();
        Lobby.onlinePlayers.remove(player);

        event.setQuitMessage(ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "➦ " + ChatColor.DARK_AQUA + player.getName() + ChatColor.GRAY + " has quit annihilation! " + "(" + Lobby.onlinePlayers.size() + "/" + "120)");

        for (AnnihilationScoreboard annihilationScoreboard : AnnihilationScoreboard.scoreboards) {
            int playersOnline = Bukkit.getOnlinePlayers().size(); //  Total players online.
            if (annihilationScoreboard.getType() == AnniScoreboardType.LOBBY) {
                annihilationScoreboard.setLine(ChatColor.WHITE + "✦ " + ChatColor.GRAY + AnniPlayer.list.get(annihilationScoreboard.getOwner().getName()).getLanguage().string("SCOREBOARD_LOBBY_PLAYERSONLINE") + ChatColor.AQUA + (playersOnline - 1), 12);
                annihilationScoreboard.removeLine(ChatColor.WHITE + "✦ " + ChatColor.GRAY + AnniPlayer.list.get(annihilationScoreboard.getOwner().getName()).getLanguage().string("SCOREBOARD_LOBBY_PLAYERSONLINE") + ChatColor.AQUA + (playersOnline));
            }
        }
        for(NPC npcs : AnniPlayer.list.get(player.getName()).getNPCsInView()) {
            npcs.hide(event.getPlayer(), true);
            AnniPlayer.list.get(player.getName()).getNPCsInView().remove(npcs);
        }

        if(!game.hasStarted()) {
            if(AnniPlayer.list.get(player.getName()).getAnnihilationScoreboard() != null)
                AnnihilationScoreboard.scoreboards.remove(AnniPlayer.list.get(player.getName()).getAnnihilationScoreboard());
            if (!(AnniPlayer.list.get(player.getName()).getTeam() == TeamType.NONE || AnniPlayer.list.get(player.getName()).getTeam() == null)) {
                AnniPlayer.list.get(player.getName()).getTeam().getMembers().remove(player);
            }
            redTeam.remove(player);
            blueTeam.remove(player);
            greenTeam.remove(player);
            yellowTeam.remove(player);
            AnniPlayer.list.remove(player.getName());
            System.out.println("Removed from if");
        }
        else if (game.hasStarted() && AnniPlayer.list.get(player.getName()).getTeam() == TeamType.NONE || AnniPlayer.list.get(player.getName()).getTeam() == null) {
            if(AnniPlayer.list.get(player.getName()).getAnnihilationScoreboard() != null)
                AnnihilationScoreboard.scoreboards.remove(AnniPlayer.list.get(player.getName()).getAnnihilationScoreboard());
            redTeam.remove(player);
            blueTeam.remove(player);
            greenTeam.remove(player);
            yellowTeam.remove(player);
            AnniPlayer.list.remove(player.getName());
            System.out.println("Removed from if");
        }
        else if (game.hasStarted() && AnniPlayer.list.get(player.getName()).getTeam() != TeamType.NONE || AnniPlayer.list.get(player.getName()).getTeam() != null) {
            for (ItemStack inventoryItemStack : player.getInventory().getContents()) {
                if (inventoryItemStack != null) game.getMap().getWorld().dropItem(player.getLocation().add(0.0, 1.0, 0.0), inventoryItemStack);
            }
        }
    }

    /**
     * Called when a projectile is thrown.
     *
     * @param event An instance of the event.
     */
    @EventHandler
    public void projectileLaunch(ProjectileLaunchEvent event) {
        Entity shooter = (Entity) event.getEntity().getShooter();

        System.out.println(shooter.getName() + " fired a projectile!");
        projectileDistList.put(event.getEntity(), 0f);
    }

    /**
     * Called when a projectile is thrown.
     *
     * @param event An instance of the event.
     */
    @EventHandler
    public void projectileHit(ProjectileHitEvent event) {
        Entity shooter = (Entity) event.getEntity().getShooter();

        System.out.println(shooter.getName() + "'s projectile landed!");
        projectileDistList.remove(event.getEntity());
    }

    /**
     * Called when a player interacts.
     *
     * @param event An instance of the event.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action == Action.PHYSICAL) {
            Block block = event.getClickedBlock();

            if (block == null) return;
            if (block.getType() == Material.SOIL) {
                event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerFish(final PlayerFishEvent event) {
        Player player = event.getPlayer();
        PlayerFishEvent.State state = event.getState();
        FishHook hook = event.getHook();

        if((state == PlayerFishEvent.State.IN_GROUND || state == PlayerFishEvent.State.FAILED_ATTEMPT) && Util.isBlockTouchedByBlock(hook.getLocation())) {
            Util.pullEntityToLocation(player, hook.getLocation());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
    }

    @EventHandler
    public void onPlayerPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material[] blockedItems = {
                Material.COAL_ORE,
                Material.IRON_ORE,
                Material.GOLD_ORE,
                Material.DIAMOND_ORE,
                Material.EMERALD_ORE,
                Material.LAPIS_ORE,
                Material.REDSTONE_ORE,
                Material.GLOWING_REDSTONE_ORE,
                Material.MELON_BLOCK,
                Material.GRAVEL,
                Material.ANVIL,
                Material.LOG,
                Material.LOG_2
        };

        if(player.getGameMode() != GameMode.CREATIVE) {
            for (Material blockedItem : blockedItems) {
                if (blockedItem.equals(block.getType())) {
                    event.setCancelled(true);
                    event.setBuild(false);
                    player.sendMessage(ChatColor.RED + AnniPlayer.list.get(player.getName()).getLanguage().string("ERROR_FORBIDDENBLOCK"));
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerBreak(BlockBreakEvent event) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        Player player = event.getPlayer();
        AnniPlayer anniPlayer = AnniPlayer.list.get(player.getName());
        Block brokenBlock = event.getBlock();
        byte brokenBlockData = brokenBlock.getData();
        World brokenBlockWorld = brokenBlock.getWorld();

        if (!anniPlayer.isInGame() && !player.isOp()) {
            event.setCancelled(true);
            return;
        }

        HashMap<Material, Integer> materialsXP = new HashMap<>();
        materialsXP.put(Material.COAL_ORE, 3);
        materialsXP.put(Material.IRON_ORE, 6);
        materialsXP.put(Material.LAPIS_ORE, 8);
        materialsXP.put(Material.GOLD_ORE, 6);
        materialsXP.put(Material.DIAMOND_ORE, 10);
        materialsXP.put(Material.EMERALD_ORE, 15);
        materialsXP.put(Material.GLOWING_REDSTONE_ORE, 8);
        materialsXP.put(Material.REDSTONE_ORE, 8);
        materialsXP.put(Material.MELON_BLOCK, 2);
        materialsXP.put(Material.LOG, 2);
        materialsXP.put(Material.LOG_2, 2);
        materialsXP.put(Material.GRAVEL, 2);

        if (anniPlayer.isInGame() && !player.getGameMode().equals(GameMode.CREATIVE)) {
            game.brokenBlocks.put(brokenBlock.getLocation(), brokenBlock);
            Block localBlock = game.brokenBlocks.get(brokenBlock.getLocation());
            Material localMaterial = game.brokenBlocks.get(brokenBlock.getLocation()).getType();
            boolean isReplacyBlock = false;

            if (materialsXP.containsKey(brokenBlock.getType())) {
                isReplacyBlock = true;
            }

            if (isReplacyBlock) {
                float[] orbSounds = {0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1f};
                int orbRandom = new Random().nextInt(orbSounds.length);
                player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, orbSounds[orbRandom]);

                if (brokenBlock.getType() == Material.GRAVEL) {
                    Material[] items = {Material.FLINT, Material.FEATHER, Material.ARROW, Material.STRING, Material.BONE};
                    int random = new Random().nextInt(items.length);
                    int randomAmount = new Random().nextInt(3) + 1;

                    event.setCancelled(true);
                    player.getInventory().addItem(new ItemStack(items[random], randomAmount));
                    player.giveExp(materialsXP.get(brokenBlock.getType()));
                } else if(brokenBlock.getType() == Material.MELON_BLOCK) {
                    int randomAmount = new Random().nextInt(6);
                    if(randomAmount == 0) randomAmount++;

                    event.setCancelled(true);
                    player.getInventory().addItem(new ItemStack(Material.MELON, randomAmount));
                    player.giveExp(materialsXP.get(brokenBlock.getType()));
                } else {
                    event.setCancelled(true);
                    for (ItemStack itemStack : brokenBlock.getDrops()) {
                        player.getInventory().addItem(itemStack);
                    }
                    player.giveExp(materialsXP.get(brokenBlock.getType()));
                }

                switch (localMaterial) {
                    case COAL_ORE: { brokenBlock.setType(Material.COBBLESTONE); break; }
                    case GOLD_ORE: { brokenBlock.setType(Material.COBBLESTONE); break; }
                    case IRON_ORE: { brokenBlock.setType(Material.COBBLESTONE); break; }
                    case LAPIS_ORE: { brokenBlock.setType(Material.COBBLESTONE); break; }
                    case REDSTONE_ORE: { brokenBlock.setType(Material.COBBLESTONE); break; }
                    case GLOWING_REDSTONE_ORE: { brokenBlock.setType(Material.COBBLESTONE); break; }
                    case DIAMOND_ORE: { brokenBlock.setType(Material.COBBLESTONE); break; }
                    case EMERALD_ORE: { brokenBlock.setType(Material.COBBLESTONE); break; }
                    case GRAVEL: { brokenBlock.setType(Material.COBBLESTONE); break; }
                    default: brokenBlock.setType(Material.AIR);
                }

                long respawnTime;
                switch (localMaterial) {
                    case COAL_ORE: { respawnTime = 20L * 15; break; }
                    case GOLD_ORE: { respawnTime = 20L * 20; break; }
                    case IRON_ORE: { respawnTime = 20L * 20; break; }
                    case LAPIS_ORE: { respawnTime = 20L * 20; break; }
                    case REDSTONE_ORE: { respawnTime = 20L * 20; break; }
                    case GLOWING_REDSTONE_ORE: { respawnTime = 20L * 20; break; }
                    case DIAMOND_ORE: { respawnTime = 20L * 30; break; }
                    case EMERALD_ORE: { respawnTime = 20L * 40; break; }
                    case LOG: { respawnTime = 20L * 15; break; }
                    case LOG_2: { respawnTime = 20L * 15; break; }
                    case GRAVEL: { respawnTime = 20L * 15; break; }
                    case MELON_BLOCK: { respawnTime = 20L * 15; break; }
                    default: { respawnTime = 20L * 1; break; }
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                    brokenBlock.setType(localMaterial);
                    brokenBlock.setData(brokenBlockData);

                    brokenBlockWorld.playEffect(brokenBlock.getLocation(), Effect.STEP_SOUND, brokenBlock.getTypeId());
                    game.brokenBlocks.remove(brokenBlock.getLocation());
                }, respawnTime);
            } else {
                if(brokenBlock.getType().equals(Material.ENDER_STONE)) {
                    if(game.hasStarted()) {
                        for (TeamType teamType : TeamType.values()) {
                            Nexus teamNexus;
                            if (game.getMap().getTeamNexus(teamType) != null) {
                                teamNexus = game.getMap().getTeamNexus(teamType);
                                if (teamNexus.getLocation().equals(event.getBlock().getLocation())) {
                                    if (anniPlayer.getTeam() != teamType) {
                                        if (game.getPhase() > 1) {
                                            event.setCancelled(true);

                                            if (teamNexus.getHealth() > 1) {
                                                if (game.getPhase() >= MAX_PHASES)
                                                    teamNexus.setHealth(teamNexus.getHealth() - 1);
                                                teamNexus.setHealth(teamNexus.getHealth() - 1);
                                                for (Player players : Bukkit.getOnlinePlayers()) {
                                                    AnniPlayer anniPlayers = AnniPlayer.list.get(players.getName());

                                                    if (anniPlayers.getTeam() != null && anniPlayer.getTeam() != null) {
                                                        if (anniPlayers.getTeam().equals(anniPlayer.getTeam())) {
                                                            players.sendMessage(anniPlayers.getTeam().getColor() + player.getName() + ChatColor.GRAY + " has damaged " + teamType.getColor() + Util.capitalize(teamType.toString().toLowerCase()) + " team's nexus" + ChatColor.GRAY + "!");
                                                        }
                                                    }
                                                }
                                                brokenBlockWorld.playSound(brokenBlock.getLocation(), Sound.ANVIL_LAND, 1.4f, 0.5f);
                                                for(Player players : teamType.getMembers()) players.playSound(players.getLocation(), Sound.NOTE_PIANO, 1f, 2f);

                                                for (AnnihilationScoreboard annihilationScoreboards : AnnihilationScoreboard.scoreboards) {
                                                    if (annihilationScoreboards.getType() == AnniScoreboardType.GAME) {
                                                        int line;
                                                        switch (teamType) {
                                                            case RED: { line = 7;break; }
                                                            case BLUE: { line = 6;break; }
                                                            case GREEN: { line = 5;break; }
                                                            case YELLOW: { line = 4;break; }
                                                            case NONE: { line = 0;break; }
                                                            default: { line = 0;break; }
                                                        }
                                                        annihilationScoreboards.removeLine(annihilationScoreboards.lines[line]);
                                                        annihilationScoreboards.setLine(teamType.getColor() + "  " + Util.capitalize(teamType.toString().toLowerCase()) + " Nexus" + ChatColor.GRAY + ": " + ChatColor.WHITE + game.getMap().getTeamNexus(teamType).getHealth() + ChatColor.RED + " ❤", line);
                                                    }
                                                }
                                            } else {
                                                teamNexus.setHealth(0);
                                                teamNexus.setDestroyed(true);
                                                brokenBlock.setType(Material.BEDROCK);
                                                brokenBlockWorld.playSound(brokenBlock.getLocation(), Sound.ANVIL_LAND, 1.4f, 0.5f);
                                                for(Player players : Bukkit.getOnlinePlayers()) players.playSound(players.getLocation(), Sound.EXPLODE, 2f, 1f);
                                                game.getMap().totalNexusesDestroyed++;

                                                BoxCharacterType teamBox;
                                                switch (teamType) {
                                                    case RED: { teamBox = BoxCharacterType.R;break; }
                                                    case BLUE: { teamBox = BoxCharacterType.B;break; }
                                                    case GREEN: { teamBox = BoxCharacterType.G;break; }
                                                    case YELLOW: { teamBox = BoxCharacterType.Y;break; }
                                                    default: { teamBox = BoxCharacterType.ONE;break; }
                                                }
                                                for (Player players : Bukkit.getOnlinePlayers()) {
                                                    AnniPlayer anniPlayers = AnniPlayer.list.get(players.getName());

                                                    anniPlayers.sendBoxCharacter(teamBox, ChatColor.GRAY, teamType.getDarkerColor(), teamType.getColor() + Util.capitalize(teamType.toString().toLowerCase()) + " team's nexus has been destroyed!", "", "");
                                                }

                                                for (AnnihilationScoreboard annihilationScoreboards : AnnihilationScoreboard.scoreboards) {
                                                    if (annihilationScoreboards.getType() == AnniScoreboardType.GAME) {
                                                        int line;
                                                        switch (teamType) {
                                                            case RED: { line = 7;break;}
                                                            case BLUE: { line = 6;break; }
                                                            case GREEN: { line = 5;break; }
                                                            case YELLOW: {line = 4;break; }
                                                            case NONE: { line = 0;break; }
                                                            default: { line = 0;break; }
                                                        }
                                                        annihilationScoreboards.removeLine(annihilationScoreboards.lines[line]);

                                                        boolean alive = false;
                                                        for (Player players : teamType.getMembers()) {
                                                            AnniPlayer anniPlayers = AnniPlayer.list.get(players.getName());

                                                            if (anniPlayers.isInGame()) {
                                                                alive = true;
                                                                break;
                                                            }
                                                        }

                                                        if (!alive) {
                                                            annihilationScoreboards.setLine(ChatColor.GRAY + "  " + ChatColor.STRIKETHROUGH + Util.capitalize(teamType.toString().toLowerCase()) + " Nexus" + ": " + game.getMap().getTeamNexus(teamType).getHealth() + " ❤", line);
                                                            if (game.getMap().totalNexusesDestroyed == 3) {
                                                                TeamType winnerTeam = null;
                                                                for (TeamType teamTypes : TeamType.values()) {
                                                                    if (!game.getMap().getTeamNexus(teamTypes).isDestroyed()) {
                                                                        winnerTeam = teamTypes;
                                                                        break;
                                                                    }
                                                                }
                                                                game.win(winnerTeam);
                                                            }
                                                        }
                                                        if (alive)
                                                            annihilationScoreboards.setLine(ChatColor.GRAY + "  " + Util.capitalize(teamType.toString().toLowerCase()) + " Nexus" + ": " + game.getMap().getTeamNexus(teamType).getHealth() + " ❤", line);
                                                    }
                                                }
                                            }
                                            break;
                                        } else {
                                            event.setCancelled(true);
                                            player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("ERROR_NEXUSBREAK_PHASEONE"));
                                        }
                                    } else {
                                        event.setCancelled(true);
                                        player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("ERROR_NEXUSBREAK_OWNNEXUS"));
                                    }

                                }
                            }
                        }
                    } else {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("ERROR_NEXUSBREAK_WRONGPOINT"));
                    }
                }
            }
        }
    }

    /**
     * Called when a player drops an item.
     *
     * @param event An instance of the event.
     */
    @EventHandler
    public void onPlayerThrow(PlayerDropItemEvent event) {
    }

    /**
     * Called when a player clicks an item in his inventory.
     *
     * @param event
     */
    @EventHandler
    public void onPlayerClick(InventoryClickEvent event) {
    }

    /**
     *
     */
    @EventHandler
    public void onPlayerInventoryClose(InventoryCloseEvent event) {
    }

    /**
     * Called when a map initializes.
     *
     * @param event An instance of the event.
     */
    @EventHandler
    public void onMapInitialise(MapInitializeEvent event){
        MapView view = event.getMap();

        for (MapRenderer m : view.getRenderers()){
            view.removeRenderer(m);
        }
        view.addRenderer(new MapTest());

    }

    /**
     * Called when an entity attempts to damage an entity.
     *
     * @param event An instance of the event.
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        AnniPlayer anniPlayer = AnniPlayer.list.get(entity.getName());
        AnniPlayer anniDamager = AnniPlayer.list.get(damager.getName());

        if (entity instanceof Player && damager instanceof Player) {
            Player player = ((Player) entity).getPlayer();
            Player attacker = ((Player) damager).getPlayer();

            /*event.setDamage(Math.abs(attacker.getItemInHand().getDurability()));
            if(!attacker.isOnGround()) event.setDamage(event.getDamage() + 0.5);*/

            if (anniPlayer.isInGame()) {
                if (player.getHealth() <= event.getDamage()) {
                    long start = System.currentTimeMillis();
                    player.setHealth(20.0f);
                    if (anniPlayer.isInGame()) {
                        for (ItemStack inventoryItemStack : player.getInventory().getContents()) {
                            if (inventoryItemStack != null) game.getMap().getWorld().dropItem(player.getLocation().add(0.0, 1.0, 0.0), inventoryItemStack);
                        }

                        event.setCancelled(true);
                        if(game.justSpawned.contains(player)) return;
                        game.justSpawned.add(player);
                        Lobby.spawnPlayer(player);
                        game.teleportPlayerToArena(player);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), () -> game.justSpawned.remove(player), 20L);

                        for (PotionEffect effect : player.getActivePotionEffects()) {
                            System.out.println(effect);
                            player.removePotionEffect(effect.getType());
                        }

                        player.setFireTicks(0);
                        player.setExp(0f);
                        player.setLevel(0);
                        player.setFoodLevel(20);
                        player.setSaturation(8);
                        player.getInventory().clear();
                        player.getInventory().setArmorContents(null);
                        player.setFireTicks(0);
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            players.sendMessage(anniPlayer.getTeam().getColor() + player.getName() + ChatColor.GRAY + anniPlayer.getLanguage().string("EXECUTION_KILLEDBY") + anniDamager.getTeam().getColor() + anniDamager.getPlayer().getName() + ChatColor.GRAY + ".");
                        }
                        if (game.getMap().getTeamNexus(anniPlayer.getTeam()).isDestroyed()) {
                            Lobby.spawnPlayer(player);
                            anniPlayer.setInGame(false);
                            for(NPC npcs : anniPlayer.getNPCsInView()) {
                                PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

                                npcs.setSkin(((CraftPlayer) player).getProfile());
                                npcs.show(player, true);
                                npcs.update(player);

                                //remember to debug here
                                if(!npcs.getNpc().getName().equals(player.getName())) {
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), () -> connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npcs.getNpc())), 20L); //  Remove the NPC from the tablist.
                                }
                            }
                            for (AnnihilationScoreboard annihilationScoreboards : AnnihilationScoreboard.scoreboards) {
                                if (annihilationScoreboards.getType() == AnniScoreboardType.GAME) {
                                    TeamType teamType = anniPlayer.getTeam();
                                    boolean alive = false;

                                    int line;
                                    switch (teamType) {
                                        case RED: { line = 7; break; }
                                        case BLUE: { line = 6; break; }
                                        case GREEN: { line = 5; break; }
                                        case YELLOW: { line = 4; break; }
                                        case NONE: { line = 0; break; }
                                        default: {line = 0; break; }
                                    }
                                    annihilationScoreboards.removeLine(annihilationScoreboards.lines[line]);

                                    for (Player players : anniPlayer.getTeam().getMembers()) {
                                        AnniPlayer anniPlayers = AnniPlayer.list.get(players.getName());

                                        if (anniPlayers.isInGame()) {
                                            alive = true;
                                            break;
                                        }
                                    }
                                    if (!alive) {
                                        annihilationScoreboards.setLine(ChatColor.GRAY + "   " + ChatColor.STRIKETHROUGH + "" + game.getMap().getTeamNexus(teamType).getHealth() + "  ❤ " + Util.capitalize(teamType.toString().toLowerCase()) + " Nexus", line);
                                        if(game.getMap().totalNexusesDestroyed == 3) {
                                            game.win(anniDamager.getTeam());
                                        }
                                    }
                                    if (alive) annihilationScoreboards.setLine(ChatColor.GRAY + "   " + "" + game.getMap().getTeamNexus(teamType).getHealth() + "  ❤ " + Util.capitalize(teamType.toString().toLowerCase()) + " Nexus", line);
                                }
                            }

                        }
                        return;
                    } else {
                        event.setCancelled(true);
                        Lobby.spawnPlayer(player);
                        player.setGameMode(GameMode.ADVENTURE);
                    }
                    long stop = System.currentTimeMillis();
                    player.sendMessage(stop - start + "ms");
                }
            }
            if (!anniPlayer.isInGame()) {
                event.setCancelled(true);
            } else {
                if (anniPlayer.getTeam() == anniDamager.getTeam()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Called when an entity attempts to damage an entity.
     *
     *
     * @param event An instance of the event.
     */
    @EventHandler
    public void onEntityDamaged(EntityDamageEvent event) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        Entity entity = event.getEntity();
        AnniPlayer anniPlayer = AnniPlayer.list.get(entity.getName());

        if (entity instanceof Player) {
            Player player = ((Player) entity).getPlayer();

            if(event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                if(game.justSpawned.contains(player)) {
                    event.setCancelled(true);
                }
            }
            if(event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                if(!anniPlayer.isInGame()) {
                    Lobby.spawnPlayer(player);
                }
            }
            if (anniPlayer.isInGame()) {
                if (player.getHealth() <= event.getDamage()) {
                    for (ItemStack inventoryItemStack : player.getInventory().getContents()) {
                        if (inventoryItemStack != null) game.getMap().getWorld().dropItem(player.getLocation().add(0.0, 1.0, 0.0), inventoryItemStack);
                    }

                    if(game.justSpawned.contains(player)) {
                        player.setHealth(20.0f);
                        event.setCancelled(true);
                        return;
                    }
                    game.justSpawned.add(player);
                    player.setHealth(20.0f);
                    event.setCancelled(true);
                    Lobby.spawnPlayer(player);
                    game.teleportPlayerToArena(player);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), () -> game.justSpawned.remove(player), 40L);

                    for(AnniPlayer anniPlayers : AnniPlayer.list.values()) {
                        String deathMessage = "died.";
                        if(event.getCause() != null) {
                            switch(event.getCause()) {
                                case VOID: {deathMessage = anniPlayer.getLanguage().string("EXECUTION_CAUSE_VOID"); break;}
                                case FALL: {deathMessage = anniPlayer.getLanguage().string("EXECUTION_CAUSE_FALL"); break;}
                                case POISON: {deathMessage = anniPlayer.getLanguage().string("EXECUTION_CAUSE_POISON"); break;}
                                case SUICIDE: {deathMessage = anniPlayer.getLanguage().string("EXECUTION_CAUSE_SUICIDE"); break;}
                                case BLOCK_EXPLOSION: {deathMessage = anniPlayer.getLanguage().string("EXECUTION_CAUSE_BLOCKEXPLOSION"); break;}
                                case LAVA: {deathMessage = anniPlayer.getLanguage().string("EXECUTION_CAUSE_LAVA"); break;}
                                case FIRE_TICK: {deathMessage = anniPlayer.getLanguage().string("EXECUTION_CAUSE_FIRETICK"); break;}
                            }
                            anniPlayers.getPlayer().sendMessage(anniPlayer.getTeam().getColor() + player.getName() + ChatColor.GRAY + " " + deathMessage);
                        }
                    }
                    if (anniPlayer.isInGame()) {
                        for (PotionEffect effect : player.getActivePotionEffects()) {
                            player.removePotionEffect(effect.getType());
                        }
                        player.setExp(0f);
                        player.setLevel(0);
                        player.setFoodLevel(20);
                        player.setSaturation(8);
                        player.getInventory().clear();
                        player.getInventory().setArmorContents(null);
                        player.setFireTicks(0);
                        if (game.getMap().getTeamNexus(anniPlayer.getTeam()).isDestroyed()) {
                            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

                            Lobby.spawnPlayer(player);
                            anniPlayer.setInGame(false);
                            for(NPC npcs : anniPlayer.getNPCsInView()) {
                                npcs.setSkin(((CraftPlayer) player).getProfile());
                                npcs.show(player, true);
                                npcs.update(player);

                                //remember to debug here
                                if(!npcs.getNpc().getName().equals(player.getName())) {
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), () -> connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npcs.getNpc())), 20L); //  Remove the NPC from the tablist.
                                }
                            }
                            for (AnnihilationScoreboard annihilationScoreboards : AnnihilationScoreboard.scoreboards) {
                                if (annihilationScoreboards.getType() == AnniScoreboardType.GAME) {
                                    TeamType teamType = anniPlayer.getTeam();
                                    boolean alive = false;

                                    int line;
                                    switch (teamType) {
                                        case RED: { line = 7; break; }
                                        case BLUE: { line = 6; break; }
                                        case GREEN: { line = 5; break; }
                                        case YELLOW: { line = 4; break; }
                                        case NONE: { line = 0; break; }
                                        default: {line = 0; break; }
                                    }
                                    annihilationScoreboards.removeLine(annihilationScoreboards.lines[line]);

                                    for (Player players : anniPlayer.getTeam().getMembers()) {
                                        AnniPlayer anniPlayers = AnniPlayer.list.get(players.getName());

                                        if (anniPlayers.isInGame()) {
                                            alive = true;
                                            break;
                                        }
                                    }
                                    if (!alive) {
                                        annihilationScoreboards.setLine(ChatColor.GRAY + "   " + ChatColor.STRIKETHROUGH + "" + game.getMap().getTeamNexus(teamType).getHealth() + "  ❤ " + Util.capitalize(teamType.toString().toLowerCase()) + " Nexus", line);
                                        if(game.getMap().totalNexusesDestroyed == 3) {
                                            TeamType winnerTeam = null;
                                            for(TeamType teamTypes : TeamType.values()) {
                                                if(!game.getMap().getTeamNexus(teamTypes).isDestroyed()) {
                                                    winnerTeam = teamTypes;
                                                    break;
                                                }
                                            }
                                            game.win(winnerTeam);
                                        }
                                    }
                                    if (alive) annihilationScoreboards.setLine(ChatColor.GRAY + "   " + "" + game.getMap().getTeamNexus(teamType).getHealth() + "  ❤ " + Util.capitalize(teamType.toString().toLowerCase()) + " Nexus", line);
                                }
                            }
                        }
                    } else {
                        event.setCancelled(true);
                        Lobby.spawnPlayer(player);
                        player.setGameMode(GameMode.ADVENTURE);
                    }
                }
            }
            if (!anniPlayer.isInGame()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFire(EntityCombustByBlockEvent event) {
        Entity entity = event.getEntity();

        if(entity instanceof Player && game.justSpawned.contains(entity)) {
            event.setCancelled(true);
            event.setDuration(0);
        }
    }

    /**
     * Called when a player's food level is updated.
     *
     * @param event An instance of the event.
     */
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        Entity entity = event.getEntity();

        if(entity instanceof Player) {
            AnniPlayer anniPlayer = AnniPlayer.list.get(entity.getName());

            if (!anniPlayer.isInGame()) {
                event.setFoodLevel(20);
            }
        }
    }

    /**
     * Called when the weather is changed.
     *
     * @param event An instance of the event.
     */
    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    /**
     * Called when a player attempts to send a message to the public chat.
     *
     * @param event An instance of the event.
     */
    @EventHandler
    public void playerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Player playerSender = event.getPlayer();
        String mainMessage = message.replaceAll("(?i)<3", ChatColor.RED + "❤" + ChatColor.RESET).
                replaceAll("(?i)o_o", ChatColor.BOLD + "ಠ_ಠ" + ChatColor.RESET);
        String afterPrefix = (playerSender.getName()) + ": " + ChatColor.WHITE;
        String prefix;

        boolean bold = false;
        for(int i = 0; i < mainMessage.length(); i++) {
            if(mainMessage.toCharArray()[i] == '*') {
            }
        }

        event.setCancelled(true);

        if (!TeamType.isPlayerInAnyTeam(playerSender)) {
            prefix = ChatColor.WHITE + "(Lobby) " + ChatColor.GRAY + "";
        } else if (message.startsWith("!") && TeamType.isPlayerInAnyTeam(playerSender)) {
            mainMessage = Util.removeCharacter(mainMessage, 0);
            prefix = ChatColor.WHITE + "[ALL] " + AnniPlayer.list.get(playerSender.getName()).getTeam().getColor();
        } else if (TeamType.isPlayerInAnyTeam(playerSender)) {
            prefix = ChatColor.WHITE + "[TEAM] " + AnniPlayer.list.get(playerSender.getName()).getTeam().getColor() + "";
        } else {
            prefix = "error 0x1";
        }

        //  Chat determinator.
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (TeamType.isPlayerInAnyTeam(playerSender)) {
                if (message.startsWith("!")) {
                    player.sendMessage(prefix + afterPrefix + mainMessage);
                } else if (AnniPlayer.list.get(player.getName()).getTeam() == AnniPlayer.list.get(playerSender.getName()).getTeam()) {
                    player.sendMessage(prefix + afterPrefix + mainMessage);
                }
            } else {
                player.sendMessage(prefix + afterPrefix + mainMessage);
            }
        }
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        console.sendMessage(prefix + afterPrefix + mainMessage);
    }

    /**
     * Called when a command is being sent generally.
     *
     * @param sender The sender of the command (can be any entity, even console).
     * @param cmd The command the player wrote.
     * @param commandLabel The command label.
     * @param args Tokenized command arguments.
     * @return A boolean on if the command succeeded.
     */
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        CommandSender commandSender = sender; //  The player.
        String input                = cmd.getName().toLowerCase(); //  The command that the player wrote.

        if (input.equals("annirestart")) {
            if (!(sender instanceof Player)) {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    Bukkit.getServer().dispatchCommand(commandSender, "kick " + players.getName() + " " + ChatColor.RED + AnniPlayer.list.get(players.getName()).getLanguage().string("SERVER_RESTARTING"));
                }
                Bukkit.getServer().dispatchCommand(commandSender, "reload");
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + AnniPlayer.list.get(((Player) sender).getName()).getLanguage().string("ERROR_CONSOLECMDONLY"));
                return true;
            }
        }

        if(commandSender instanceof Player) { //  If the sender of the command is a player.
            Player player = (Player) sender;
            AnniPlayer anniPlayer = AnniPlayer.list.get(player.getName());
            Location playerLocation = player.getLocation(); // The player's location.

            if (input.equals("fly")) { //  If command is: /fly
                if (player.isOp()) {
                    if (!flyingPeople.contains(player)) {
                        player.setAllowFlight(true);
                        flyingPeople.add(player);
                        player.sendMessage(ChatColor.GREEN + anniPlayer.getLanguage().string("CMD_FLY_ON"));
                        player.playSound(playerLocation, Sound.ORB_PICKUP, 1, 1);
                    } else {
                        player.setAllowFlight(false);
                        flyingPeople.remove(player);
                        player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("CMD_FLY_OFF"));
                        player.playSound(playerLocation, Sound.ORB_PICKUP, 1f, 0.8f);
                    }
                } else {
                    player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("CMD_NOTAUTHORIZED"));
                }
            }

            if (input.equals("win")) { //  If command is: /win
                if(!player.isOp()) {
                    player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("CMD_NOTAUTHORIZED"));
                    return true;
                }
                if(!game.hasStarted()) {
                    player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("CMD_WIN_GAMENOTSTARTED"));
                    return true;
                }
                if(game.teamWon != null) {
                    player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("CMD_WIN_GAMEHASWINNER"));
                    return true;
                }

                if(anniPlayer.getTeam() != TeamType.NONE || anniPlayer.getTeam() != null) {
                    try {
                        game.win(anniPlayer.getTeam());
                    } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        game.win(TeamType.RED);
                    } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            if(input.equals("start")) { //  If command is: /start
                if(!player.isOp()) {
                    player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("CMD_NOTAUTHORIZED"));
                    return true;
                }
                if(game.hasStarted()) {
                    player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("CMD_START_ALREADYSTARTED"));
                    return true;
                }

                try {
                    game.setStartCountdown(false);
                    game.start();
                } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            if (input.equals("ping")) { //  If command is: /ping
                int ping = ((CraftPlayer) player).getHandle().ping;
                player.sendMessage(ChatColor.GREEN + anniPlayer.getLanguage().string("CMD_PING_YOURPING") + ChatColor.YELLOW + ping + "ms");
            }

            if (input.equals("spawn")) { //  If command is: /spawn
                Lobby.spawnPlayer(player);
                player.sendMessage(ChatColor.GOLD + anniPlayer.getLanguage().string("CMD_SPAWN_TELEPORTED"));
                player.playSound(playerLocation, Sound.ORB_PICKUP, 1, 1);
                AnniPlayer.list.get(player.getName()).setInGame(false);
            }

            if (input.equals("team")) { //  If command is: /team
                if (args.length == 0) { //  If the first argument is empty.
                    player.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "---------" + ChatColor.GRAY + "["+ChatColor.DARK_AQUA+"Teams"+ChatColor.GRAY+"]" + ChatColor.STRIKETHROUGH + "---------");
                    player.sendMessage(ChatColor.GRAY + "Team " + ChatColor.RED + "Red " + ChatColor.GRAY + "has " + TeamType.RED.getMembers().size() + " players.");
                    player.sendMessage(ChatColor.GRAY + "Team " + ChatColor.BLUE + "Blue " + ChatColor.GRAY + "has " + TeamType.BLUE.getMembers().size() + " players.");
                    player.sendMessage(ChatColor.GRAY + "Team " + ChatColor.GREEN + "Green " + ChatColor.GRAY + "has " + TeamType.GREEN.getMembers().size() + " players.");
                    player.sendMessage(ChatColor.GRAY + "Team " + ChatColor.YELLOW + "Yellow " + ChatColor.GRAY + "has " + TeamType.YELLOW.getMembers().size() + " players.");
                    player.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "------------------------");
                    return true;
                }
                if (game.teamWon != null) {
                    player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("CMD_TEAM_GAMEENDED"));
                    return true;
                }
                if (game.hasStarted() && !(AnniPlayer.list.get(player.getName()).getTeam() == TeamType.NONE || AnniPlayer.list.get(player.getName()).getTeam() == null)) {
                    player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("CMD_TEAM_NO_TEAM_CHANGE_WHEN_GAME_STARTED"));
                    return true;
                }
                if (args.length == 1) {
                    switch (args[0]) {
                        case "join": {
                            player.sendMessage(ChatColor.GOLD + anniPlayer.getLanguage().string("CMD_TEAM_USAGE") + ChatColor.WHITE + "/team " + args[0] + " <color>");
                            break;
                        }
                        case "leave": {
                            if (TeamType.isPlayerInAnyTeam(player)) {
                                AnnihilationScoreboard annihilationScoreboard = AnniPlayer.list.get(player.getName()).getAnnihilationScoreboard();

                                player.sendMessage(ChatColor.GOLD + "You left " + AnniPlayer.list.get(player.getName()).getTeam().toString().toLowerCase() + " team!");
                                AnniPlayer.list.get(player.getName()).getTeam().getMembers().remove(player);
                                AnniPlayer.list.get(player.getName()).setTeam(TeamType.NONE);
                                player.setPlayerListName(ChatColor.GRAY + player.getName());
                                Util.setNameTagName(player, TeamType.NONE);

                                annihilationScoreboard.removeLine(annihilationScoreboard.lines[2]);
                                annihilationScoreboard.setLine(ChatColor.WHITE + "✦ " + ChatColor.GRAY + "Team: " + ChatColor.RED + "None", 2);

                                player.playSound(playerLocation, Sound.ORB_PICKUP, 5f, 0.8f);
                            } else {
                                player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("CMD_TEAM_NOTEAM"));
                            }
                            break;
                        }
                        case "random": {
                            String[] teamsSTR = {"red", "blue", "green", "yellow"};
                            int random = new Random().nextInt(teamsSTR.length);

                            player.performCommand("team join " + teamsSTR[random]);
                            break;
                        }
                        default:
                            player.sendMessage("Unknown argument.");
                            break;
                    }
                }
                if (args.length == 2) {
                    if (TeamType.getTeamByString(args[1].toLowerCase()) != null) {
                        if (AnniPlayer.list.get(player.getName()).getTeam() != TeamType.getTeamByString(args[1].toLowerCase())) {
                            AnnihilationScoreboard annihilationScoreboard = AnniPlayer.list.get(player.getName()).getAnnihilationScoreboard();
                            if (game.getMap().getTeamNexus(TeamType.getTeamByString(args[1].toLowerCase())).isDestroyed()) {
                                player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("CMD_TEAM_INVALID_TEAM_NEXUS"));
                                return true;
                            }

                            //  Team balancing algorithm.
                            int r = 4;
                            int biggestTeamNum = 0; for (TeamType teams : TeamType.values()) if(teams.getMembers().size() >= biggestTeamNum && teams != TeamType.NONE) biggestTeamNum = teams.getMembers().size();
                            int averageTeamNum = (TeamType.RED.getMembers().size() + TeamType.BLUE.getMembers().size() + TeamType.YELLOW.getMembers().size() + TeamType.GREEN.getMembers().size()) / 4;

                            System.out.println("Biggest - average = " + (Math.abs(biggestTeamNum - averageTeamNum) + TeamType.getTeamByString(args[1].toLowerCase()).getMembers().size()));
                            System.out.println("R = " + r);

                            /*if ((Math.abs(biggestTeamNum - averageTeamNum)) >= r) {
                                player.sendMessage(ChatColor.RED + "Unbalanced teams, please join an other team.");
                                for(Player players : Bukkit.getOnlinePlayers()) players.sendMessage("[DEBUG]: " + player.getName() + " failed to join " + TeamType.getTeamByString(args[1].toLowerCase()).getColor() + TeamType.getTeamByString(args[1].toLowerCase()));
                                return true;
                            }*/
                            /////////////////////////////

                            anniPlayer.setTeam(TeamType.getTeamByString(args[1].toLowerCase()));
                            for(Player players : Bukkit.getOnlinePlayers()) players.sendMessage("[DEBUG]: " + player.getName() + " joined " + anniPlayer.getTeam().getColor() + anniPlayer.getTeam().toString());
                            ChatColor teamColor = anniPlayer.getTeam().getColor();
                            String tablistName;

//                            tablistName = teamColor + "" + "[" + ChatColor.BOLD + anniPlayer.getTeam().toString().toCharArray()[0] + teamColor + "] " + player.getName();
                            tablistName = teamColor + "" + ChatColor.WHITE + "[" + teamColor + anniPlayer.getTeam().toString().toCharArray()[0] + ChatColor.WHITE + "] " + teamColor + player.getName();

                            if(game.hasStarted()) {
                                Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> Util.setNameTagName(player, anniPlayer.getTeam()), 10L);
                            }

                            switch(anniPlayer.getTeam()) {
                                case RED: { anniPlayer.sendBoxCharacter(BoxCharacterType.R, ChatColor.GRAY, ChatColor.DARK_RED, anniPlayer.getLanguage().string("CMD_TEAM_YOU_JOINED") + teamColor + Util.capitalize(args[1].toLowerCase()) + teamColor + ChatColor.GRAY + " team!", "", ""); break; }
                                case BLUE: { anniPlayer.sendBoxCharacter(BoxCharacterType.B, ChatColor.GRAY, ChatColor.DARK_BLUE, anniPlayer.getLanguage().string("CMD_TEAM_YOU_JOINED") + teamColor + Util.capitalize(args[1].toLowerCase()) + teamColor + ChatColor.GRAY + " team!", "", ""); break; }
                                case GREEN: { anniPlayer.sendBoxCharacter(BoxCharacterType.G, ChatColor.GRAY, ChatColor.DARK_GREEN, anniPlayer.getLanguage().string("CMD_TEAM_YOU_JOINED") + teamColor + Util.capitalize(args[1].toLowerCase()) + teamColor + ChatColor.GRAY + " team!", "", ""); break; }
                                case YELLOW: { anniPlayer.sendBoxCharacter(BoxCharacterType.Y, ChatColor.GRAY, ChatColor.GOLD, anniPlayer.getLanguage().string("CMD_TEAM_YOU_JOINED") + teamColor + Util.capitalize(args[1].toLowerCase()) + teamColor + ChatColor.GRAY + " team!", "", ""); break; }
                            }

                            player.playSound(playerLocation, Sound.ORB_PICKUP, 5f, 1f);

                            if (game.hasStarted()) {
                                game.teleportPlayerToArena(player);
                                player.setFoodLevel(20);
                                player.setSaturation(8);
                            }

                            if (annihilationScoreboard.getType() == AnniScoreboardType.LOBBY) {
                                annihilationScoreboard.removeLine(annihilationScoreboard.lines[2]);
                                annihilationScoreboard.setLine(ChatColor.WHITE + "✦ " + ChatColor.GRAY + "Team: " + anniPlayer.getTeam().getColor() + Util.capitalize(args[1].toLowerCase()), 2);
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("CMD_TEAM_ALREADY_IN") + args[1].toLowerCase() + " team!");
                            player.playSound(playerLocation, Sound.ORB_PICKUP, 5f, 0.5f);
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + anniPlayer.getLanguage().string("CMD_TEAM_UNKNOWNTEAM"));
                    }
                }
            }
        }
        return false;
    }
}