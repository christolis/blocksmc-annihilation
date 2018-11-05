package me.blocksmc.annihilation.util;

import me.blocksmc.annihilation.scorboard.AnnihilationScoreboard;
import me.blocksmc.annihilation.team.TeamType;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Util.java
 *
 * @author Chris Sdogkos
 * @since 0.0
 */
public class Util {

    private static final String CHAR_LIST = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    /**
     * Converts an integer to a latin character (only works for the first ten numbers).
     *
     * @param input The number to convert.
     * @return The converted number.
     */
    public static String intToLatinCharacter(int input) {
        switch(input) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            case 7: return "VII";
            case 8: return "VIII";
            case 9: return "IX";
            case 10: return "X";
            default: return "undefined";
        }
    }

    /**
     * Calculates a cube-shaped array of blocks and adds it into an ArrayList.
     *
     * @param location The location to calculate the cube.
     * @param radius The radius (size) of the cube.
     * @return The calculated ArrayList of blocks.
     */
    public static ArrayList<Location> squareRadius(Location location, int radius) {
        ArrayList<Location> chosenLocations = new ArrayList<>();

        for (int ax = (location.getBlockX() - radius); ax < (location.getBlockX() + radius); ax++) {
            for (int ay = (location.getBlockY() - radius); ay < (location.getBlockY() + radius); ay++) {
                for (int az = (location.getBlockZ() - radius); az < (location.getBlockZ() + radius); az++) {
                    chosenLocations.add(new Location(location.getWorld(), ax, ay, az));
                }
            }
        }
        return chosenLocations;
    }

    /**
     * Sends a hot-bar message (message above the items' hud) to a specific player.
     *
     * @param player The player to send the hot-bar message.
     * @param message The message of the action-bar.
     * @param permanent Set this parameter to false if you want it to show one time and fade out, true if you want it to show all the time.
     *
     * @return 0 if permanent is set to false, tasks' ID if it is set to true.
     */
    public static int sendHotbarMessage(Player player, String message, boolean permanent) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2);
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        if (!permanent) {
            playerConnection.sendPacket(packet);
        } else {
            return Bukkit.getScheduler().scheduleSyncRepeatingTask(Bukkit.getPluginManager().getPlugin("Annihilation"), () -> {
                    playerConnection.sendPacket(packet);
            }, 10L, 10L);
        }
        return 0;
    }

    /**
     * Sends a title message to the player.
     *
     * @param player The player.
     * @param message The message.
     * @param chatColor The coloor.
     * @param type The type.
     * @param fadeIn The fade in seconds.
     * @param time The stay seconds.
     * @param fadeOut The fade out seconds.
     */
    public static void sendTitleMessage(Player player, String message, ChatColor chatColor, PacketPlayOutTitle.EnumTitleAction type, int fadeIn, int time, int fadeOut) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        IChatBaseComponent _json = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\",color:" + chatColor.name().toLowerCase() + "}");
        PacketPlayOutTitle packetTitle = new PacketPlayOutTitle(type, _json);
        PacketPlayOutTitle packetLength = new PacketPlayOutTitle(fadeIn, time, fadeOut);

        playerConnection.sendPacket(packetTitle);
        playerConnection.sendPacket(packetLength);
    }

    /**
     * Capitalizes a string.
     *
     * @param line The string to capitalize.
     * @return The capitalized string.
     */
    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    /**
     * Removes a character from a string.
     *
     * @param string The string to edit.
     * @param characterArray The character array size.
     * @return The edited string.
     */
    public static String removeCharacter(String string, int characterArray) {
        String newString = "";
        int iterator = 0;
        for (char character : string.toCharArray()) {
            if (iterator != characterArray) {
                newString = newString + character;
            }
            iterator++;
        }
        return newString;
    }

    /**
     * Deletes a world from the server's directory.
     *
     * @param path The path to remove.
     * @return False if the path failed to get deleted, true if the path succeeded to delete.
     */
    public static boolean deleteWorld(File path) {
        if (path.exists()) {
            File files[] = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteWorld(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    /**
     * Unloads a world.
     *
     * @param world The world's name to unload.
     */
    public static void unloadWorld(World world) {
        if(world != null) {
            Bukkit.getServer().unloadWorld(world, true);
        }
    }

    /**
     * Copies a new world.
     *
     * @param source The source world.
     * @param target The copied world's save location.
     */
    public static void copyWorld(File source, File target){
        try {
            ArrayList<String> ignore = new ArrayList<String>(Arrays.asList("uid.dat", "session.dat"));
            if(!ignore.contains(source.getName())) {
                if(source.isDirectory()) {
                    if(!target.exists())
                        target.mkdirs();
                    String files[] = source.list();
                    for (String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        copyWorld(srcFile, destFile);
                    }
                } else {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(target);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = in.read(buffer)) > 0)
                        out.write(buffer, 0, length);
                    in.close();
                    out.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the inputted class file inside the 'net.minecraft.server' directory.
     * (Useful for reflections)
     *
     * @param nmsClassString The class to be returned.
     * @return The class of the input.
     */
    @Deprecated
    public static Class<?> getNMSClass(String nmsClassString) {
        try {
            String version    = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "."; //  Returns the version.
            String name       = "net.minecraft.server." + version + nmsClassString; //  Returns the directory of the inputted class.

            return Class.forName(name);
        } catch (ClassNotFoundException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the player's handle connection.
     *
     * @param player The player to return its connection.
     * @return The player's connection as Object.
     * @throws SecurityException Security Exception.
     * @throws NoSuchMethodException No such method exception.
     * @throws NoSuchFieldException No such field exception.
     * @throws IllegalArgumentException Illegal argument exception.
     * @throws IllegalAccessException Illegal access exception.
     * @throws InvocationTargetException Invocation target exception.
     */
    public static Object getPlayerConnection(Player player) throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method getHandle = player.getClass().getMethod("getHandle"); //  The handle of the player.
        Object nmsPlayer = getHandle.invoke(player); //  Run an instance of the NMS player.
        Field conField   = nmsPlayer.getClass().getField("playerConnection"); //  Get the 'playerConnection' as a field.

        return conField.get(nmsPlayer); //  Return it to the method.
    }

    /**
     * Generates a random string.
     *
     * @param size The size of the string.
     * @return The generated string.
     */
    public static String generateRandomString(int size) {
        StringBuffer randStr = new StringBuffer();

        for (int i = 0; i < size; i++) {
            int number = generateRandomNumber();
            char ch = CHAR_LIST.charAt(number);
            randStr.append(ch);
        }
        return randStr.toString();
    }

    /**
     * Generates a random number.
     *
     * @return The generated number.
     */
    public static int generateRandomNumber() {
        int randomInt = 0;
        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(CHAR_LIST.length());
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }

    /**
     * Sets the player's new name tag.
     *
     * @param player The player to set its name tag to.
     * @param teamType The new prefix to set.
     */
    public static void setNameTagName(Player player, TeamType teamType) {
        for (AnnihilationScoreboard annihilationScoreboard : AnnihilationScoreboard.scoreboards) {
            Team team = annihilationScoreboard.getScoreboard().getTeam(capitalize(teamType.toString().toLowerCase()));

            team.removeEntry(player.getName());
            team.addEntry(player.getName());
        }
    }

    public static void spawnWorldParticle(World world, Location location, EnumParticle particle, Vector3f offset, int count) {
        for(Player players : Bukkit.getOnlinePlayers()) {
            if(players.getWorld() != world) continue;

            PlayerConnection playerConnection = ((CraftPlayer) players).getHandle().playerConnection;
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particle,
                    true,
                    (float) (location.getX()),
                    (float) (location.getY()),
                    (float) (location.getZ()),
                    offset == null ? 0f : offset.getX(),
                    offset == null ? 0f : offset.getY(),
                    offset == null ? 0f : offset.getZ(),
                    1,
                    count);

            playerConnection.sendPacket(packet);
        }
    }

    public static ItemStack createItem(Material material, int amount, int data, String title, String... l) {
        ItemStack I = new ItemStack(material, amount, (short) data);
        ItemMeta Im = I.getItemMeta();
        if(title != null) Im.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));
        ArrayList<String> lore = new ArrayList<>();
        lore.addAll(Arrays.asList(l));
        Im.setLore(lore);
        I.setItemMeta(Im);
        return I;
    }

    public static boolean isBlockTouchedByBlock(Location location) {
        org.bukkit.block.Block block = location.getBlock();

        for (BlockFace blockFace : BlockFace.values()) {
            if(block.getRelative(blockFace).getType() != Material.AIR && !block.getRelative(blockFace).isLiquid()) return true;
        }
        return false;
    }

    /**
     * Pulls an entity to a specific location.
     *
     * @param entity The entity to pull.
     * @param location The location for the entity to get pulled at.
     */
    public static void pullEntityToLocation(final org.bukkit.entity.Entity entity, Location location){
        Location entityLoc = entity.getLocation();

        entityLoc.setY(entityLoc.getY()+0.5);
        entity.teleport(entityLoc);

        double g = -0.08;
        double d = location.distance(entityLoc);
        double t = d;
        double v_x = (1.0+0.07*t) * (location.getX()-entityLoc.getX())/t;
        double v_y = (1.0+0.03*t) * (location.getY()-entityLoc.getY())/t -0.5*g*t;
        double v_z = (1.0+0.07*t) * (location.getZ()-entityLoc.getZ())/t;

        Vector v = entity.getVelocity();
        v.setX(v_x);
        v.setY(v_y);
        v.setZ(v_z);
        entity.setVelocity(v);
    }

    public static void setHeaderForPlayer(Player player, String header) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PlayerConnection playerConnection = craftPlayer.getHandle().playerConnection;

        IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + header + "\"}");
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();

        try {
            Field headerField = packet.getClass().getDeclaredField("a");
            headerField.setAccessible(true);
            headerField.set(packet, component);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        playerConnection.sendPacket(packet);
    }

    public static void setFooterForPlayer(Player player, String footer) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        PlayerConnection playerConnection = craftPlayer.getHandle().playerConnection;

        IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + footer + "\"}");
        PacketPlayOutPlayerListHeaderFooter packet = new PacketPlayOutPlayerListHeaderFooter();

        try {
            Field headerField = packet.getClass().getDeclaredField("b");
            headerField.setAccessible(true);
            headerField.set(packet, component);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        playerConnection.sendPacket(packet);
    }

    public static String getLanguage(Player p) throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Object ep = getMethod("getHandle", p.getClass()).invoke(p, (Object[]) null);
        Field f = ep.getClass().getDeclaredField("locale");

        f.setAccessible(true);
        String language = (String) f.get(ep);
        return language;
    }

    private static Method getMethod(String name, Class<?> clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(name))
                return m;
        }
        return null;
    }
}
