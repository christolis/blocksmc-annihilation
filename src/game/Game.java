package me.blocksmc.annihilation.game;

import me.blocksmc.annihilation.AnniPlayer;
import me.blocksmc.annihilation.Lobby;
import me.blocksmc.annihilation.Main;
import me.blocksmc.annihilation.scorboard.AnniScoreboardType;
import me.blocksmc.annihilation.scorboard.AnnihilationScoreboard;
import me.blocksmc.annihilation.team.TeamType;
import me.blocksmc.annihilation.util.BoxCharacterType;
import me.blocksmc.annihilation.util.Util;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Game {
    private AnniWorld world;
    private final int minPlayers;
    private final int maxPlayers;
    private int phase = 0;
    private long elapsedSeconds = 0;
    private long elapsedMinutes = elapsedSeconds / 60;
    private long elapsedHours = elapsedMinutes / 60;

    private long totalSeconds = 0;
    private long totalMinutes = totalSeconds / 60;
    private long totalHours = totalMinutes / 60;

    private final int phaseDurationMin = 0;

    private boolean startingCountdown = false;
    private int startingCountdownNumber = 30;

    private boolean endingCountdown = false;
    private int endingCountdownMinutes = 2;
    private int endingCountdownSeconds = 0;
    public TeamType teamWon = null;

    private int startedTitleID = 0;
    private int startedTitleID2 = 0;
    private int startedTitleIterator = 0;
    private int startedTitleIterator2 = 0;

    private boolean hasStarted = false;
    private int countdownTimerID = 0;
    private int endingTimerID = 0;
    private int tickTimerID = 0;

    public static final int MAX_PHASES = 5;

    public HashMap<Location, Block> brokenBlocks = new HashMap<>();
    public ArrayList<Player> justSpawned = new ArrayList<>(); // NOTE: PROBABLY TEMPORARY FIX.

    public Game(AnniWorld world, int minPlayers, int maxPlayers) {
        this.world = world;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public AnniWorld getMap() {
        return world;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean getStartingCountdown() {
        return startingCountdown;
    }

    public boolean getEndingCountdown() {
        return endingCountdown;
    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public int getPhase() {
        return this.phase;
    }

    public void setPhase(int newPhase) {
        this.phase = newPhase;
    }

    public void setMap(AnniWorld newWorld) {
        if (this.hasStarted) return;
        this.world = newWorld;

        if (world.getWorld().equals(Bukkit.getWorld("map_grass_v2_game"))) {
            Main.game.getMap().addSpawnpoint(TeamType.BLUE, new Location(Main.game.getMap().getWorld(), -34.5, 51.0, -40.5));
            Main.game.getMap().addSpawnpoint(TeamType.BLUE, new Location(Main.game.getMap().getWorld(), -34.5, 51.0, -34.5));
            Main.game.getMap().addSpawnpoint(TeamType.BLUE, new Location(Main.game.getMap().getWorld(), -40.5, 51.0, -34.5));
            Main.game.getMap().setTeamNexus(TeamType.BLUE, new Nexus(new Location(this.getMap().getWorld(), -49, 52, -49), 100));

            Main.game.getMap().addSpawnpoint(TeamType.RED, new Location(Main.game.getMap().getWorld(), -40.5, 51.0, 34.5));
            Main.game.getMap().addSpawnpoint(TeamType.RED, new Location(Main.game.getMap().getWorld(), -34.5, 51.0, 34.5));
            Main.game.getMap().addSpawnpoint(TeamType.RED, new Location(Main.game.getMap().getWorld(), -34.5, 51.0, 40.5));
            Main.game.getMap().setTeamNexus(TeamType.RED, new Nexus(new Location(this.getMap().getWorld(), -49, 52, 48), 100));

            Main.game.getMap().addSpawnpoint(TeamType.GREEN, new Location(Main.game.getMap().getWorld(), 40.5, 51.0, -34.5));
            Main.game.getMap().addSpawnpoint(TeamType.GREEN, new Location(Main.game.getMap().getWorld(), 34.5, 51.0, -34.5));
            Main.game.getMap().addSpawnpoint(TeamType.GREEN, new Location(Main.game.getMap().getWorld(), 34.5, 51.0, -40.5));
            Main.game.getMap().setTeamNexus(TeamType.GREEN, new Nexus(new Location(this.getMap().getWorld(), 48, 52, -49), 100));

            Main.game.getMap().addSpawnpoint(TeamType.YELLOW, new Location(Main.game.getMap().getWorld(), 34.5, 51.0, 40.5));
            Main.game.getMap().addSpawnpoint(TeamType.YELLOW, new Location(Main.game.getMap().getWorld(), 34.5, 51.0, 34.5));
            Main.game.getMap().addSpawnpoint(TeamType.YELLOW, new Location(Main.game.getMap().getWorld(), 40.5, 51.0, 34.5));
            Main.game.getMap().setTeamNexus(TeamType.YELLOW, new Nexus(new Location(this.getMap().getWorld(), 48, 52, 48), 100));

        } else if (world.getWorld().equals(Bukkit.getWorld("map_firstmap_game"))) {
            Main.game.getMap().addSpawnpoint(TeamType.BLUE, new Location(Main.game.getMap().getWorld(), -105.5, 73.0, -109.5));
            Main.game.getMap().setTeamNexus(TeamType.BLUE, new Nexus(new Location(this.getMap().getWorld(), -113, 70, -114), 100));

            Main.game.getMap().addSpawnpoint(TeamType.RED, new Location(Main.game.getMap().getWorld(), -109.5, 73.0, 231.5));
            Main.game.getMap().setTeamNexus(TeamType.RED, new Nexus(new Location(this.getMap().getWorld(), -114, 70, 238), 100));

            Main.game.getMap().addSpawnpoint(TeamType.GREEN, new Location(Main.game.getMap().getWorld(), 235.5, 73.0, -105.5));
            Main.game.getMap().setTeamNexus(TeamType.GREEN, new Nexus(new Location(this.getMap().getWorld(), 239, 70, -113), 100));

            Main.game.getMap().addSpawnpoint(TeamType.YELLOW, new Location(Main.game.getMap().getWorld(), 231.5, 73.0, 235.5));
            Main.game.getMap().setTeamNexus(TeamType.YELLOW, new Nexus(new Location(this.getMap().getWorld(), 238, 70, 239), 100));
        }
    }
    public void setStartCountdown(boolean newStartingCountdown) throws InvocationTargetException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        this.startingCountdown = newStartingCountdown;

        if (!newStartingCountdown) {
            Bukkit.getScheduler().cancelTask(countdownTimerID);
            return;
        }

        if (newStartingCountdown) {
            countdownTimerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(Main.class), () -> {
                if (startingCountdownNumber < 0) {
                    try {
                        setStartCountdown(false);
                        start();
                    } catch (InvocationTargetException | NoSuchMethodException | NoSuchFieldException | InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    startingCountdownNumber--;
                    return;
                }
                if (startingCountdownNumber <= 10) {
                    for (Player players : Lobby.onlinePlayers) {
                        AnniPlayer anniPlayer = AnniPlayer.list.get(players.getName());

                        players.playSound(players.getLocation(), Sound.CLICK, 1, 1);
                        try {Util.sendHotbarMessage(players, ChatColor.GREEN + anniPlayer.getLanguage().string("GAME_START_STARTTIMER")+startingCountdownNumber, false);} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchFieldException e) {e.printStackTrace();}
                    }
                    startingCountdownNumber--;
                    return;
                }
                if (startingCountdownNumber <= 30) {
                    for (Player players : Lobby.onlinePlayers) {
                        try {
                            AnniPlayer anniPlayer = AnniPlayer.list.get(players.getName());

                            Util.sendHotbarMessage(players, ChatColor.GREEN + anniPlayer.getLanguage().string("GAME_START_STARTTIMER")+startingCountdownNumber, false);
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchFieldException e) {e.printStackTrace();}
                    }
                    startingCountdownNumber--;
                    return;
                }
                startingCountdownNumber--;
            }, 0L, 20L);
        }
    }

    public void setEndingCountdown(boolean newEndingCountdown) throws InvocationTargetException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        this.endingCountdown = newEndingCountdown;

        if (!newEndingCountdown) {
            Bukkit.getScheduler().cancelTask(endingTimerID);
            return;
        }

        if(newEndingCountdown) {
            endingTimerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(Main.class), () -> {
                if (endingCountdownMinutes == 0 && endingCountdownSeconds == 0) {
                    try {
                        setEndingCountdown(false);
                        for (Player players : Bukkit.getOnlinePlayers()) {
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "kick " + players.getName() + " " + ChatColor.RED + AnniPlayer.list.get(players.getName()).getLanguage().string("SERVER_RESTARTING"));
                        }
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "reload");
                    } catch (InvocationTargetException | NoSuchMethodException | NoSuchFieldException | InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                for (Player players : Lobby.onlinePlayers) {
                    try {
                        AnniPlayer anniPlayer = AnniPlayer.list.get(players.getName());
                        String ht = totalHours < 10 ? "0" + totalHours : "" + totalHours;
                        String mt = totalMinutes < 10 ? "0" + totalMinutes : "" + totalMinutes;
                        String st = totalSeconds < 10 ? "0" + totalSeconds : "" + totalSeconds;
                        String totalTime = ht + ":" + mt + ":" + st;

                        String mr = endingCountdownMinutes < 10 ? "0" + endingCountdownMinutes : "" + endingCountdownMinutes;
                        String sr = endingCountdownSeconds < 10 ? "0" + endingCountdownSeconds : "" + endingCountdownSeconds;
                        String restartingTime = mr + ":" + sr;

                        Util.sendHotbarMessage(players, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + anniPlayer.getLanguage().string("GAME_ENDING_TOTALTIME") + totalTime + anniPlayer.getLanguage().string("GAME_ENDING_RESTARTINGIN") + restartingTime + "", false);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchFieldException e) {e.printStackTrace();}
                }
                endingCountdownSeconds--;
                if(endingCountdownSeconds < 0) {
                    endingCountdownSeconds = 59;
                    endingCountdownMinutes--;
                }
            }, 0L, 20L);
        }
    }

    public void start() throws InvocationTargetException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        this.hasStarted = true;
        for (AnniPlayer anniPlayers : AnniPlayer.list.values()) { anniPlayers.getAnnihilationScoreboard().setType(AnniScoreboardType.GAME); }
        setPhase(0);
        elapsedSeconds = 0;
        elapsedMinutes = 0;
        elapsedHours = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            AnniPlayer anniPlayer = AnniPlayer.list.get(player.getName());
            String title = anniPlayer.getLanguage().string("GAME_START_TITLE");
            String subtitle = anniPlayer.getLanguage().string("GAME_START_SUBTITLE");

            Util.sendTitleMessage(player, title, ChatColor.GREEN, PacketPlayOutTitle.EnumTitleAction.TITLE, 20, 40, 20);
            Util.sendTitleMessage(player, subtitle, ChatColor.DARK_AQUA, PacketPlayOutTitle.EnumTitleAction.SUBTITLE, 20, 40, 20);
        }

        for (AnniPlayer anniPlayers : AnniPlayer.list.values()) {
            Player players = anniPlayers.getPlayer();
            AnnihilationScoreboard annihilationScoreboard = anniPlayers.getAnnihilationScoreboard();

            if (!(anniPlayers.getTeam() == TeamType.NONE || anniPlayers.getTeam() == null)) {
                ChatColor teamColor = anniPlayers.getTeam().getColor();
                String tablistName = teamColor + "" + ChatColor.WHITE + "[" + teamColor + anniPlayers.getTeam().toString().toCharArray()[0] + ChatColor.WHITE + "] " + teamColor + players.getName();
//
//                players.setPlayerListName(tablistName);
//                players.setCustomName(tablistName);
//                players.setDisplayName(tablistName);

                Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), () -> {
                    if (anniPlayers.getTeam() != null) {
                        Util.setNameTagName(anniPlayers.getPlayer(), anniPlayers.getTeam()); //  Set his name tag to whatever was chosen.
                    } else { //  If the player's team is null, IT SHOULD NOT AND IS A FAT ERROR.
                        anniPlayers.getPlayer().sendMessage(ChatColor.YELLOW + "NOTE: " + ChatColor.WHITE + "Your team is " + ChatColor.GRAY + "null" + ChatColor.WHITE + ", you should report this to the developer(s).");
                    }
                }, 5L);

                this.teleportPlayerToArena(players);
                players.setFoodLevel(20);
                players.setSaturation(8);
            }

            // Scoreboard change.
            for(int l = 0; l < annihilationScoreboard.lines.length; l++) {
                if(annihilationScoreboard.lines[l] != null) {
                    annihilationScoreboard.removeLine(annihilationScoreboard.lines[l]);
                }
            }

            annihilationScoreboard.setDisplayName(ChatColor.DARK_AQUA + "" + AnniPlayer.list.get(annihilationScoreboard.getOwner().getName()).getLanguage().string("SCOREBOARD_GAME_MAP"));
            annihilationScoreboard.setLine(ChatColor.GRAY + " " + ChatColor.STRIKETHROUGH + "-------------------", 8);
            annihilationScoreboard.setLine(ChatColor.RED + "  Red Nexus" + ChatColor.GRAY + ": " + ChatColor.WHITE + this.getMap().getTeamNexus(TeamType.RED).getHealth() + ChatColor.RED + " ❤", 7);
            annihilationScoreboard.setLine(ChatColor.BLUE + "  Blue Nexus" + ChatColor.GRAY + ": " + ChatColor.WHITE + this.getMap().getTeamNexus(TeamType.BLUE).getHealth() +  ChatColor.RED + " ❤",6);
            annihilationScoreboard.setLine(ChatColor.GREEN + "  Green Nexus" + ChatColor.GRAY + ": " + ChatColor.WHITE + this.getMap().getTeamNexus(TeamType.GREEN).getHealth() + ChatColor.RED + " ❤", 5);
            annihilationScoreboard.setLine(ChatColor.YELLOW + "  Yellow Nexus" + ChatColor.GRAY + ": " + ChatColor.WHITE + this.getMap().getTeamNexus(TeamType.YELLOW).getHealth() + ChatColor.RED + " ❤", 4);
            annihilationScoreboard.setLine(" ", 3);
            if(annihilationScoreboard.lines[2] != null) annihilationScoreboard.removeLine(annihilationScoreboard.lines[2]);
            annihilationScoreboard.setLine(ChatColor.DARK_PURPLE + "  Phase:   I II III IV V", 2);
            annihilationScoreboard.setLine(ChatColor.GRAY + " " + ChatColor.STRIKETHROUGH + "-------------------" + ChatColor.RESET, 1);
        }

        tickTimerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(Main.class), () -> {
            try {
                tick();
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }, 0L, 20L);
    }

    public void tick() throws InvocationTargetException, NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        //  Nexus's particles.

        /**************************************
         * Particle system for the nexus block.
         *************************************/
        for (TeamType teamType : TeamType.values()) {
            if(teamType == TeamType.NONE) continue; //  If the team's type is NONE then skip it (Because team NONE has no nexuses and NullPointerException will get called as a result).
            int count = 80; //  The population of the particles.

            for(int i = 0; i < count; i++) { //  Iterating through the count of the particles.
                int radius = 10; //  The distance between the target block and the random generated position.
                Location location = new Location(
                        getMap().getWorld(),
                        getMap().getTeamNexus(teamType).getLocation().getX() + 0.5,
                        getMap().getTeamNexus(teamType).getLocation().getY() + 1.5,
                        getMap().getTeamNexus(teamType).getLocation().getZ() + 0.5
                ); //  The location of the nexus (centered).
                Vector3f offset = new Vector3f(
                        (float) (-radius + new Random().nextInt(radius * 2)) / 2,
                      (float) (-radius + new Random().nextInt(radius * 2)) / 2,
                      (float) (-radius + new Random().nextInt(radius * 2)) / 2
                ); //  The offset position.

                Util.spawnWorldParticle(getMap().getWorld(), location, EnumParticle.ENCHANTMENT_TABLE, offset, 0); //  Finally, spawn the particle to the world.
            }
        }

        if (getPhase() < 5) { // Greater than 5.
            if (elapsedSeconds <= 0 && elapsedMinutes <= 0 && elapsedHours <= 0) {
                setPhase(getPhase() + 1);

                for(AnniPlayer anniPlayer : AnniPlayer.list.values()) {
                    BoxCharacterType boxType;
                    String newPhaseDesc;
                    String newPhaseDesc2 = "";
                    switch (getPhase()) {
                        case 1: { boxType = BoxCharacterType.ONE; newPhaseDesc = ChatColor.GOLD + "Nexus blocks" + ChatColor.GRAY + anniPlayer.getLanguage().string("GAME_PHASE_ONE_LINE1"); break; }
                        case 2: { boxType = BoxCharacterType.TWO; newPhaseDesc = ChatColor.GOLD + "Nexus blocks" + ChatColor.GRAY + anniPlayer.getLanguage().string("GAME_PHASE_TWO_LINE1"); break; }
                        case 3: { boxType = BoxCharacterType.THREE; newPhaseDesc = ChatColor.GOLD + "Diamond ores" + ChatColor.GRAY + anniPlayer.getLanguage().string("GAME_PHASE_THREE_LINE1"); newPhaseDesc2 = anniPlayer.getLanguage().string("GAME_PHASE_THREE_LINE2"); break; }
                        case 4: { boxType = BoxCharacterType.FOUR; newPhaseDesc = ChatColor.GOLD + "Blaze powder" + ChatColor.GRAY + anniPlayer.getLanguage().string("GAME_PHASE_FOUR_LINE1"); newPhaseDesc2 = anniPlayer.getLanguage().string("GAME_PHASE_FOUR_LINE2"); break; }
                        case 5: { boxType = BoxCharacterType.FIVE; newPhaseDesc = ChatColor.GOLD + "Nexus blocks" + ChatColor.GRAY + anniPlayer.getLanguage().string("GAME_PHASE_FIVE_LINE1"); break; }
                        default: boxType = BoxCharacterType.B; newPhaseDesc = null;
                    }

                    anniPlayer.sendBoxCharacter(boxType, ChatColor.GRAY, ChatColor.RED, ChatColor.GOLD + "Phase " + Util.intToLatinCharacter(getPhase()) + ChatColor.GRAY + anniPlayer.getLanguage().string("GAME_PHASE_CHANGE"), newPhaseDesc, newPhaseDesc2);
                }
                for (AnnihilationScoreboard annihilationScoreboards : AnnihilationScoreboard.scoreboards) {
                    if (annihilationScoreboards.getType() == AnniScoreboardType.GAME) {
                        int line = 2;
                        int s = 0;
                        StringBuilder phaseString = new StringBuilder();

                        for (int i = 1; i <= MAX_PHASES; i++) {
                            String newString;

                            if (i == getPhase()) newString = ChatColor.translateAlternateColorCodes('&', "&5" + Util.intToLatinCharacter(i) + " ");
                            else newString = ChatColor.translateAlternateColorCodes('&', "&7" + Util.intToLatinCharacter(i) + " ");

                            phaseString.insert(s, newString);
                            s += newString.length();
                        }

                        try {
                            annihilationScoreboards.removeLine(annihilationScoreboards.lines[line]);
                            annihilationScoreboards.setLine(ChatColor.DARK_PURPLE  + "  Phase:   " + phaseString.toString(), line);
                        } catch (Exception e) {
                            System.out.println("Error in changing phase in scoreboard! :(");
                        }
                    }
                }
                if(phase != 5) {
                    elapsedHours = 0;
                    elapsedMinutes = phaseDurationMin;
                    elapsedSeconds = 59;
                } else {
                    elapsedHours = 0;
                    elapsedMinutes = 59;
                    elapsedSeconds = 0;
                }
            }
            else {
                elapsedSeconds--;
                if (elapsedSeconds < 0) {
                    elapsedSeconds = 59;
                    elapsedMinutes--;
                }
                if (elapsedMinutes < 0) {
                    elapsedSeconds = 59;
                    elapsedMinutes--;
                }
            }
        } else { // Bigger than 5.
            elapsedSeconds++;
            if (elapsedSeconds > 59) {
                elapsedSeconds = 0;
                elapsedMinutes++;
            }
            if (elapsedMinutes > 59) {
                elapsedSeconds = 0;
                elapsedMinutes = 0;
                elapsedHours++;
            }
        }

        //  Total time system
        totalSeconds++;
        if (totalSeconds > 59) {
            totalSeconds = 0;
            totalMinutes++;
        }
        if (totalMinutes > 59) {
            totalSeconds = 0;
            totalMinutes = 0;
            totalHours++;
        }

        ChatColor col;
        switch(getPhase()) {
            case 1: {col = ChatColor.BLUE; break;}
            case 2: {col = ChatColor.GREEN; break;}
            case 3: {col = ChatColor.YELLOW; break;}
            case 4: {col = ChatColor.GOLD; break;}
            case 5: {col = ChatColor.RED; break;}
            default: col = ChatColor.WHITE;
        }
        String h = elapsedHours < 10 ? "0" + elapsedHours : "" + elapsedHours;
        String m = elapsedMinutes < 10 ? "0" + elapsedMinutes : "" + elapsedMinutes;
        String s = elapsedSeconds < 10 ? "0" + elapsedSeconds : "" + elapsedSeconds;
        String hourRender = h + ":" + m + ":" + s;
        for(Player players : Bukkit.getOnlinePlayers()) {
            Util.sendHotbarMessage(players, col + "Phase " + Util.intToLatinCharacter(getPhase()) + " - " + hourRender, false);
        }
    }

    public void teleportPlayerToArena(Player player) {
        int random = new Random().nextInt(world.getSpawnpoints(AnniPlayer.list.get(player.getName()).getTeam()).size());

        player.teleport(world.getSpawnpoints(AnniPlayer.list.get(player.getName()).getTeam()).get(random)); // here you will change the teleport for teams and blabla

        player.setGameMode(GameMode.SURVIVAL);
        AnniPlayer.list.get(player.getName()).setInGame(true);
    }

    public void win(TeamType teamType) throws NoSuchMethodException, NoSuchFieldException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if(this.teamWon == null) {
            for (Player players : Bukkit.getOnlinePlayers()) {
                AnniPlayer anniPlayers = AnniPlayer.list.get(players.getName());

                anniPlayers.sendBoxCharacter(BoxCharacterType.E, ChatColor.GRAY, ChatColor.RED, anniPlayers.getLanguage().string("GAME_WIN_ANNOUNCEMENT_1") + teamType.getColor() + Util.capitalize(teamType.toString().toLowerCase()) + ChatColor.GRAY + anniPlayers.getLanguage().string("GAME_WIN_ANNOUNCEMENT_2"), anniPlayers.getLanguage().string("GAME_WIN_ANNOUNCEMENT_3"), "");
            }
            Bukkit.getScheduler().cancelTask(tickTimerID);
            this.teamWon = teamType;
            sendConsoleInfo(Util.capitalize(teamWon.toString().toLowerCase()) + " won the game!");
            setEndingCountdown(true);
            for(AnnihilationScoreboard annihilationScoreboard : AnnihilationScoreboard.scoreboards) {
                if(annihilationScoreboard.getType() == AnniScoreboardType.GAME) {
                    annihilationScoreboard.removeLine(annihilationScoreboard.lines[2]);
                    annihilationScoreboard.setLine(ChatColor.DARK_PURPLE + AnniPlayer.list.get(annihilationScoreboard.getOwner().getName()).getLanguage().string("GAME_WIN_ANNOUNCEMENT_4"), 2);
                }
            }

            Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(Main.class), () -> {
                for(TeamType team : TeamType.values()) {
                    if (team == TeamType.NONE) continue;

                    for (Location locations : getMap().getSpawnpoints(team)) {
                        int offsetY = 5;
                        Location location = new Location(locations.getWorld(), locations.getX(), locations.getY() + offsetY, locations.getZ());
                        Firework firework = (Firework) getMap().getWorld().spawnEntity(location, EntityType.FIREWORK);
                        FireworkMeta fireworkMeta = firework.getFireworkMeta();

                        Color color;
                        switch(teamType) {
                            case RED: { color = Color.RED; break; }
                            case BLUE: { color = Color.BLUE; break; }
                            case YELLOW: { color = Color.YELLOW; break; }
                            case GREEN: { color = Color.GREEN; break; }
                            default: { color = Color.WHITE; break; }
                        }
                        FireworkEffect fireworkEffect = FireworkEffect.builder().flicker(true).trail(true).with(FireworkEffect.Type.BURST).withColor(color).build();
                        fireworkMeta.addEffect(fireworkEffect);
                        fireworkMeta.setPower(0);
                        firework.setFireworkMeta(fireworkMeta);
                    }
                }
            }, 0L, 40L);
        } else {
            System.out.println("[CODE ERROR]: Tried to win the game when the team is not null.");
        }
    }

    public void sendConsoleInfo(String message) {
        Bukkit.getServer().getConsoleSender().sendMessage("[GAME]: " + message);
    }
}
