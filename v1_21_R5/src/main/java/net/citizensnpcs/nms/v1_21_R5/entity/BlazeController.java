package net.citizensnpcs.nms.v1_21_R5.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_21_R5.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_21_R5.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_21_R5.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftBlaze;
import org.bukkit.craftbukkit.entity.CraftEntity;

public class BlazeController extends MobEntityController {
    public BlazeController() {
        super(EntityBlazeNPC.class, EntityType.BLAZE);
    }

    @Override
    public org.bukkit.entity.Blaze getBukkitEntity() {
        return (org.bukkit.entity.Blaze) super.getBukkitEntity();
    }

    public static class BlazeNPC extends CraftBlaze implements ForwardingNPCHolder {
        public BlazeNPC(EntityBlazeNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }

    public static class EntityBlazeNPC extends Blaze implements NPCHolder {
        private final CitizensNPC npc;

        public EntityBlazeNPC(EntityType<? extends Blaze> types, Level level) {
            this(types, level, null);
        }

        public EntityBlazeNPC(EntityType<? extends Blaze> types, Level level, NPC npc) {
            super(types, level);
            this.npc = (CitizensNPC) npc;
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
        protected boolean canRide(Entity entity) {
            if (npc != null && (entity instanceof Boat || entity instanceof AbstractMinecart))
                return !npc.isProtected();
            return super.canRide(entity);
        }

        @Override
        public void checkDespawn() {
            if (npc == null) {
                super.checkDespawn();
            }
        }

        @Override
        public void customServerAiStep(ServerLevel level) {
            if (npc != null) {
                NMSImpl.updateMinecraftAIState(npc, this);
                npc.update();
            }
        }

        @Override
        public SoundEvent getAmbientSound() {
            return NMSImpl.getSoundEffect(npc, super.getAmbientSound(), NPC.Metadata.AMBIENT_SOUND);
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new BlazeNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public SoundEvent getDeathSound() {
            return NMSImpl.getSoundEffect(npc, super.getDeathSound(), NPC.Metadata.DEATH_SOUND);
        }

        @Override
        public SoundEvent getHurtSound(DamageSource damagesource) {
            return NMSImpl.getSoundEffect(npc, super.getHurtSound(damagesource), NPC.Metadata.HURT_SOUND);
        }

        @Override
        public float getJumpPower() {
            return NMS.getJumpPower(npc, super.getJumpPower());
        }

        @Override
        public int getMaxFallDistance() {
            return NMS.getFallDistance(npc, super.getMaxFallDistance());
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public PushReaction getPistonPushReaction() {
            return Util.callPistonPushEvent(npc) ? PushReaction.IGNORE : super.getPistonPushReaction();
        }

        @Override
        public boolean isLeashed() {
            return NMSImpl.isLeashed(npc, super::isLeashed, this);
        }

        @Override
        public boolean isPushable() {
            return npc == null ? super.isPushable()
                    : npc.data().<Boolean> get(NPC.Metadata.COLLIDABLE, !npc.isProtected());
        }

        @Override
        public void knockback(double strength, double dx, double dz) {
            NMS.callKnockbackEvent(npc, (float) strength, dx, dz, evt -> super.knockback((float) evt.getStrength(),
                    evt.getKnockbackVector().getX(), evt.getKnockbackVector().getZ()));
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
