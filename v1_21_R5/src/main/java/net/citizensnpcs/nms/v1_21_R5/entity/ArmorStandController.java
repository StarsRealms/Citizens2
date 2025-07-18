package net.citizensnpcs.nms.v1_21_R5.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_21_R5.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_21_R5.util.MobAI;
import net.citizensnpcs.nms.v1_21_R5.util.MobAI.ForwardingMobAI;
import net.citizensnpcs.nms.v1_21_R5.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_21_R5.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftArmorStand;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class ArmorStandController extends MobEntityController {
    public ArmorStandController() {
        super(EntityArmorStandNPC.class, EntityType.ARMOR_STAND);
    }

    @Override
    public org.bukkit.entity.ArmorStand getBukkitEntity() {
        return (org.bukkit.entity.ArmorStand) super.getBukkitEntity();
    }

    public static class ArmorStandNPC extends CraftArmorStand implements ForwardingNPCHolder {
        public ArmorStandNPC(EntityArmorStandNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }

    public static class EntityArmorStandNPC extends ArmorStand implements NPCHolder, ForwardingMobAI {
        private MobAI ai;
        private final CitizensNPC npc;

        public EntityArmorStandNPC(EntityType<? extends ArmorStand> types, Level level) {
            this(types, level, null);
        }

        public EntityArmorStandNPC(EntityType<? extends ArmorStand> types, Level level, NPC npc) {
            super(types, level);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                ai = new BasicMobAI(this);
                NMS.setStepHeight(getBukkitEntity(), 1);
            }
        }

        @Override
        public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entitytrackerentry) {
            var packet = net.citizensnpcs.nms.v1_21_R5.util.CustomEntityTraitUtil.packet(npc,entitytrackerentry,this);
            if (packet != null) {
                return packet;
            }
            return super.getAddEntityPacket(entitytrackerentry);
        }

        @Override
        public boolean broadcastToPlayer(ServerPlayer player) {
            return NMS.shouldBroadcastToPlayer(npc, () -> super.broadcastToPlayer(player));
        }

        @Override
        public MobAI getAI() {
            return ai;
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new ArmorStandNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public InteractionResult interactAt(Player entityhuman, Vec3 vec3d, InteractionHand enumhand) {
            if (npc == null)
                return super.interactAt(entityhuman, vec3d, enumhand);
            PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(
                    (org.bukkit.entity.Player) entityhuman.getBukkitEntity(), getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            return event.isCancelled() ? InteractionResult.FAIL : InteractionResult.SUCCESS;
        }

        @Override
        public boolean isPushable() {
            return npc == null ? super.isPushable()
                    : npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE, !npc.isProtected());
        }

        @Override
        protected AABB makeBoundingBox(Vec3 vec3) {
            return NMSBoundingBox.makeBB(npc, super.makeBoundingBox(vec3));
        }

        @Override
        public void push(Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.push(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        public boolean save(ValueOutput save) {
            return npc == null ? super.save(save) : false;
        }

        @Override
        public Entity teleport(TeleportTransition transition) {
            if (npc == null)
                return super.teleport(transition);
            return NMSImpl.teleportAcrossWorld(this, transition);
        }

        @Override
        public void tick() {
            super.tick();
            if (npc != null) {
                npc.update();
                ai.tickAI();
            }
        }

        @Override
        public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tagkey, double d0) {
            if (npc == null)
                return super.updateFluidHeightAndDoFluidPushing(tagkey, d0);
            Vec3 old = getDeltaMovement().add(0, 0, 0);
            boolean res = super.updateFluidHeightAndDoFluidPushing(tagkey, d0);
            if (!npc.isPushableByFluids()) {
                setDeltaMovement(old);
            }
            return res;
        }
    }
}
