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

package me.blocksmc.annihilation.npc;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import me.blocksmc.annihilation.AnniPlayer;
import me.blocksmc.annihilation.Main;
import me.blocksmc.annihilation.scorboard.AnnihilationScoreboard;
import me.blocksmc.annihilation.util.Util;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class NPC {

    private EntityPlayer npc; //  The NPC instance.
    private MinecraftServer nmsServer; //  The server's instance.
    private WorldServer nmsWorld; //  The world's instance.
    private GameProfile gameProfile; //  The NPC's game profile.
    private Location npcLocation; //  The NPC location's instance.
    private Player player;
    private int totalArmorStands = 0;

    private HashMap<EntityArmorStand, Integer> armorStands = new HashMap<>(); //  The list of the NPC's armor stands.
    private HashMap<ItemStack, Integer> equipment = new HashMap<>(); //  The NPC's equipment.

    private float yaw; //  The NPC's yaw.
    private float pitch; //  The NPC's pitch.

    public static HashMap<NPC, Player> list = new HashMap<>(); //  The list of all NPCs.

    /**
     * Initializes an NPC.
     *
     * @param npcName The NPC's username (CANNOT BE NULL)
     * @param npcLocation The NPC's location.
     * @param yaw The NPC's yaw.
     * @param pitch The NPC's pitch.
     */
    public NPC(Player player, String npcName, String uuid, Location npcLocation, float yaw, float pitch, boolean summonDefaultArmorStand) {
        MinecraftServer nmsServer                   = ((CraftServer)Bukkit.getServer()).getServer(); //  Get the server's instance.
        WorldServer nmsWorld                        = ((CraftWorld)Bukkit.getServer().getWorlds().get(0)).getHandle(); //  Get the server's world.
        GameProfile gameProfile                     = new GameProfile(UUID.fromString(uuid), npcName); //  Get the GameProfile's instance.
        EntityPlayer npc                            = new EntityPlayer(nmsServer, nmsWorld, gameProfile, new PlayerInteractManager(nmsWorld)); //  Get the NPC's instance.
        EntityArmorStand defaultArmorStand          = new EntityArmorStand(((CraftWorld) npcLocation.getWorld()).getHandle(), npcLocation.getX(), npcLocation.getY(), npcLocation.getZ()); //  The default armor stand's variable holder.
        AnniPlayer anniPlayer                       = AnniPlayer.list.get(player.getName());

        if(summonDefaultArmorStand) {

            defaultArmorStand.setGravity(false);
            defaultArmorStand.setInvisible(true);
            defaultArmorStand.setBasePlate(false);
            defaultArmorStand.setCustomName(ChatColor.GRAY + "" + ChatColor.BOLD + "PUNCH TO JOIN TEAM");
            defaultArmorStand.setCustomNameVisible(false);
            defaultArmorStand.setPosition(npcLocation.getX(), npcLocation.getY()+0.25f, npcLocation.getZ());

            armorStands.put(defaultArmorStand, defaultArmorStand.getId());
            totalArmorStands++;
        }

        npc.setLocation(npcLocation.getX(), npcLocation.getY(), npcLocation.getZ(), yaw, pitch); //  Sets the location.

        this.npc = npc; //  Sets the class' 'npc' variable to have the value of the generated one.
        this.nmsServer = nmsServer; //  Sets the class' 'nmsServer' variable to have the value of the generated one.
        this.nmsWorld = nmsWorld; //  Sets the class' 'nmsWorld' variable to have the value of the generated one.
        this.gameProfile = gameProfile; //  Sets the class' 'gameProfile' variable to have the value of the generated one.
        this.npcLocation = npcLocation; //  Sets the class' 'npcLocation' variable to have the value of the generated one.
        this.yaw = yaw; //  Sets the class' 'yaw' variable to have the value of the generated one.
        this.player = player; //  Sets the class' 'player' variable to have the value of the generated one.

        list.put(this, player); //  Adds the NPC to the list.
    }

    /**
     * Makes the NPC visible to a player.
     *
     * @param player The player to show the NPC.
     */
    public void show(Player player, boolean andArmorStands) {
        PlayerConnection connection = ((CraftPlayer) player.getPlayer()).getHandle().playerConnection; //  Gets the player's connection.
        AnniPlayer anniPlayer = AnniPlayer.list.get(player.getName());

        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, npc)); //  Sends a PlayerInfo packet.
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc)); //  Sends a NamedEntitySpawn packet.
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (getYaw() * 256.0D / 360.0D)));
        for (int i = 0; i < 10; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), () -> connection.sendPacket(new PacketPlayOutAnimation(npc, 0)), 1L);
        }

        if(andArmorStands) {
            for (EntityArmorStand armorStands : armorStands.keySet()) {
                connection.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStands));
            }
        }

        if (!anniPlayer.getNPCsInView().contains(this)) anniPlayer.getNPCsInView().add(this);
//        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(Main.class), () -> connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc)), 20L); //  Remove the NPC from the tablist.
    }

    /**
     * Makes the NPC invisible to a player.
     *
     * @param player The player to hide the NPC.
     */
    public void hide(Player player, boolean andArmorStands) {
        PlayerConnection connection = ((CraftPlayer) player.getPlayer()).getHandle().playerConnection; //  Gets the player's connection.
        AnniPlayer anniPlayer = AnniPlayer.list.get(player.getName());

        connection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, npc)); //  Sends a PlayerInfo packet.
        connection.sendPacket(new PacketPlayOutEntityDestroy(npc.getId())); //  Sends a NamedEntitySpawn packet.

        anniPlayer.getNPCsInView().remove(this);
        if(andArmorStands) {
            for (EntityArmorStand armorStands : armorStands.keySet()) {
                //  Unresolved lol, what packet is it? .-.
            }
        }
    }

    /**
     * Updates the NPC's data to a specific player.
     *
     * @param player The player to see the updated NPC's data.
     */
    public void update(Player player) {
        PlayerConnection connection = ((CraftPlayer) player.getPlayer()).getHandle().playerConnection; //  Gets the player's connection.

        connection.sendPacket(new PacketPlayOutEntityTeleport(npc.getId(), (int) getLocation().getX(), (int) getLocation().getY(), (int) getLocation().getZ(), (byte) (getYaw() * 256.0D / 360.0D), (byte) getPitch(), true)); //  Update the NPC's location data.
        connection.sendPacket(new PacketPlayOutNamedEntitySpawn(npc)); //  Respawn the NPC.
        connection.sendPacket(new PacketPlayOutEntityHeadRotation(npc, (byte) (getYaw() * 256.0D / 360.0D))); //  Set the NPC's head rotation.

        DataWatcher watcher = npc.getDataWatcher();
        watcher.watch(10, (byte) 127);
        connection.sendPacket(new PacketPlayOutEntityMetadata(npc.getId(), watcher, true));

        for (Map.Entry<ItemStack, Integer> equipments : equipment.entrySet()) {
            connection.sendPacket(new PacketPlayOutEntityEquipment(npc.getId(), equipments.getValue(), CraftItemStack.asNMSCopy(equipments.getKey())));
        }
    }

    /**
     * Sets the NPC's skin.
     *
     * @param profile The NPC's game profile.
     * @param uuid The NPC UUID to set.
     * @return False if failed, true if succeeded.
     */
    public boolean setSkin(GameProfile profile, String uuid) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false", uuid)).openConnection();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                String reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
                String skin = reply.split("\"value\":\"")[1].split("\"")[0];
                String signature = reply.split("\"signature\":\"")[1].split("\"")[0];

                profile.getProperties().put("textures", new Property("textures", skin, signature));
                return true;
            } else {
                if (connection.getResponseCode() == 429)
                    System.out.println("Connection could not be opened (Too many requests)");
                else
                    System.out.println("Connection could not be opened (Error " + connection.getResponseCode() + ")");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sets the skin. (Locally)
     *
     * @param profile The game profile to set.
     * @return False if failed, true if succeeded.
     */
    public void setSkin(GameProfile profile) {
        PropertyMap npcProperties = npc.getProfile().getProperties();

        for(Property property : profile.getProperties().values()) {
            npcProperties.put("textures", property);
        }
    }

    /**
     * Adds equipment to the NPC such as body armor and main or off hand item.
     *
     * @param slot The slot to set the item to.
     * @param item The item's ID.
     */
    public void addEquipment(int slot, ItemStack item) {
        equipment.put(item, slot);
    }

    /**
     * Removes equipment to the NPC such as body armor and main or off hand item.
     *
     * @param slot The slot to remove the item from.
     * @param item The item's ID.
     */
    public void removeEquipment(int slot, ItemStack item) {
        equipment.remove(item, slot);
    }

    /**
     * Gets the NPC's equipment.
     *
     * @return The equipment's full HashMap.
     */
    public HashMap<ItemStack, Integer> getEquipment() {
        return equipment;
    }

    /**
     * Adds an attached armor stand to the NPC.
     *
     * @param name The display name of the armor stand.
     * @param itemInHand The item in the armor stand's hand. (Deprecated)
     * @param invisible Boolean. Set accordingly to if the armor stand's model is invisible.
     * @param location The location of the armor stand.
     */
    public void addArmorStand(String name, @Deprecated ItemStack itemInHand, boolean invisible, Location location) {
        EntityArmorStand armorStand = new EntityArmorStand(((CraftWorld) location.getWorld()).getHandle(), location.getX(), location.getY(), location.getZ()); //  The default armor stand's variable holder.
        HashMap<Integer, Integer> armorStandID = new HashMap<>();

        armorStand.setGravity(false);
        armorStand.setInvisible(invisible);
        armorStand.setBasePlate(false);
        armorStand.setCustomName(name);
        if( name != null) armorStand.setCustomNameVisible(true);
        armorStand.setPosition(location.getX(), location.getY()+0.25f, location.getZ());

        armorStandID.put(totalArmorStands, armorStand.getId());
        this.armorStands.put(armorStand, armorStand.getId());
        totalArmorStands++;
    }

    public void updateArmorStands(Player player) {
        for (EntityArmorStand armorStands : armorStands.keySet()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(armorStands));
        }
    }

    public HashMap<EntityArmorStand, Integer> getArmorStands() {
        return armorStands;
    }

    /**
     * Sets the NPC's location.
     *
     * @param npcLocation The NPC's location.
     */
    public void setLocation(Location npcLocation) {
        this.npcLocation = npcLocation;
        this.npc.setLocation(npcLocation.getX(), npcLocation.getY(), npcLocation.getZ(), getYaw(), getPitch());
    }

    /**
     * Sets the NPC's yaw.
     *
     * @param yaw The NPC's yaw.
     */
    public void setYaw(float yaw) {
        this.yaw = yaw;
        this.npc.setLocation(this.npcLocation.getX(), this.npcLocation.getY(), this.npcLocation.getZ(), yaw, getPitch());
    }

    /**
     * Sets the NPC's pitch.
     *
     * @param pitch The NPC's pitch.
     */
    public void setPitch(float pitch) {
        this.pitch = pitch;
        this.npc.setLocation(this.npcLocation.getX(), this.npcLocation.getY(), this.npcLocation.getZ(), getYaw(), pitch);
    }

    /**
     * Gets the NPC's game profile.
     *
     * @return The game profile.
     */
    public GameProfile getGameProfile() {
        return gameProfile;
    }

    /**
     * Gets the NPC.
     *
     * @return The NPC.
     */
    public EntityPlayer getNpc() {
        return npc;
    }

    /**
     * Gets the NPC's server.
     *
     * @return The NMS's server.
     */
    public MinecraftServer getNmsServer() {
        return nmsServer;
    }

    /**
     * Gets the NPC's world.
     *
     * @return The NPC's world.
     */
    public WorldServer getNmsWorld() {
        return nmsWorld;
    }

    /**
     * Gets the NPC's location.
     *
     * @return The NPC's location.
     */
    public Location getLocation() {
        return npcLocation;
    }

    /**
     * Gets the NPC's yaw.
     *
     * @return The NPC's yaw.
     */
    public float getYaw() {
        return yaw;
    }

    /**
     * Gets the NPC's pitch.
     *
     * @return The NPC's pitch.
     */
    public float getPitch() {
        return pitch;
    }

    /**
     * Gets the scoreboard team of the NPC.
     *
     * @return The team of the NPC.
     */
    @Deprecated
    public static Team getScoreboardTeam() {
        return null;
    }

    /**
     * Returns a newly generated GameProfile with the texture properties of the playerProfile.
     *
     * @author Entwickler7aSv
     * @param playerProfile The player's profile you want to get the textures of.
     * @return The new generated GameProfile.
     */
    public static GameProfile getPlayerProfile(GameProfile playerProfile, String newUsername) {
        Collection<Property> texture = playerProfile.getProperties().get("textures"); //  Gets the texture properties of the player's profile.
        GameProfile newProfile; //  Variable holder for the new generated GameProfile.

        if(newUsername != null) //  If the 'newUsername' variable isn't empty/null.
            newProfile = new GameProfile(UUID.randomUUID(), newUsername); //  Generates a new GameProfile with a random UUID with the name of the value of 'newUsername' and saves it to the variable.
        else //  Else, if it is empty/null.
            newProfile = new GameProfile(UUID.randomUUID(), ChatColor.DARK_GRAY + "NPC [" + Util.generateRandomString(5) + "]"); //  Generates a new GameProfile with a random UUID with the name of the default value and saves it to the variable.

        for (Property property : texture) { //  For each property in the texture variable.
            newProfile.getProperties().put("textures", new Property("textures", property.getValue(), property.getSignature())); //  Sets the texture properties of the new generated GameProfile to the ones from the player's profile.
        }
        return newProfile; //  Finally, returns the new generated GameProfile.
    }
}
