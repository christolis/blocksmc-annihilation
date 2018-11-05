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

package me.blocksmc.annihilation.util;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import me.blocksmc.annihilation.AnniPlayer;
import me.blocksmc.annihilation.Main;
import me.blocksmc.annihilation.npc.NPC;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

/**
 *
 */
public class PlayerInjector extends ChannelDuplexHandler {

    private Player player;
    private boolean isInjected = false;

    /**
     * Constructor for the player injector.
     * @param player The player to inject.
     */
    public PlayerInjector(Player player) {
        this.player = player;

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", player.getName(), this);

        this.isInjected = true;
		System.out.println("[PACKET_INJECTOR]: Successfully injected " + player.getName() + ".");
    }

    /**
     * This gets called whenever a packet comes into the server (inbound packet).
     *
     * @param channelHandlerContext The channel handler context.
     * @param packet The packet object (NMS).
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
        Field actionField = PacketPlayInUseEntity.class.getDeclaredField("a"); //  Gets the declared field "a" (the UseAction).
        actionField.setAccessible(true); //  Makes the declared field accessible to use.

        //  Example number 1 (Listening to the packet whenever the player interacts with an NPC).
        if (packet instanceof PacketPlayInUseEntity) { //  If the packet that the server read is a PacketPlayInUseEntity (Packet that interacts).
            PacketPlayInUseEntity.EnumEntityUseAction action = ((PacketPlayInUseEntity) packet).a();

            if (action == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK || action == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT) { //  If the action is attack or interact (right-click).
            }
        }
        super.channelRead(channelHandlerContext, packet); //  Reads the incoming packet.
    }

    /**
     * This gets called whenever a packet comes out of the server (outbound packet).
     *
     * @param channelHandlerContext The channel handler context.
     * @param packet The packet object (NMS).
     * @param channelPromise The channel promise.
     * @throws Exception
     */
    @Override
    public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
        super.write(channelHandlerContext, packet, channelPromise);
    }

    /**
     * Sees if the player is injected or not.
     *
     * @param player The player to check.
     * @return True if the player is injected, false if not.
     */
    public boolean isInjected(Player player) {
        return this.isInjected;
    }
}