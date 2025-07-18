package net.citizensnpcs.nms.v1_21_R5.entity;

import com.mojang.authlib.GameProfile;
import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_21_R5.util.NMSImpl;
import net.citizensnpcs.npc.AbstractEntityController;
import net.citizensnpcs.npc.skin.Skin;
import net.citizensnpcs.trait.ScoreboardTrait;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class HumanController extends AbstractEntityController {
    public HumanController() {
    }

    @Override
    protected Entity createEntity(final Location at, final NPC npc) {
        final ServerLevel nmsWorld = ((CraftWorld) at.getWorld()).getHandle();
        String coloredName = npc.getFullName();
        String name = coloredName.length() > 16 ? coloredName.substring(0, 16) : coloredName;
        UUID uuid = npc.getMinecraftUniqueId();
        String teamName = Util.getTeamName(uuid);
        if (npc.requiresNameHologram()) {
            name = teamName;
        }
        if (Setting.USE_SCOREBOARD_TEAMS.asBoolean()) {
            npc.getOrAddTrait(ScoreboardTrait.class).createTeam(name);
        }
        final GameProfile profile = new GameProfile(uuid, name);
        final EntityHumanNPC handle = new EntityHumanNPC(MinecraftServer.getServer(), nmsWorld, profile,
                ClientInformation.createDefault(), npc);
        Skin skin = handle.getSkinTracker().getSkin();
        if (skin != null) {
            skin.apply(handle);
        }
        if (NMSImpl.MOONRISE_IS_REAL_PLAYER != null) {
            try {
                NMSImpl.MOONRISE_IS_REAL_PLAYER.invoke(handle, !npc.shouldRemoveFromPlayerList());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(CitizensAPI.getPlugin(), () -> {
            if (getBukkitEntity() == null || !getBukkitEntity().isValid()
                    || getBukkitEntity() != handle.getBukkitEntity())
                return;
            NMS.addOrRemoveFromPlayerList(getBukkitEntity(), npc.shouldRemoveFromPlayerList());
        }, 20);
        handle.getBukkitEntity().setSleepingIgnored(true);
        return handle.getBukkitEntity();
    }

    @Override
    public Player getBukkitEntity() {
        return (Player) super.getBukkitEntity();
    }
}
