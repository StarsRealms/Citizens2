package net.citizensnpcs.nms.v1_21_R5.entity;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_21_R5.util.ForwardingNPCHolder;
import net.citizensnpcs.nms.v1_21_R5.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_21_R5.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.trait.versioned.EnderDragonTrait;
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
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.boss.enderdragon.DragonFlightHistory.Sample;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
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
import org.bukkit.craftbukkit.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.entity.CraftEntity;

public class EnderDragonController extends MobEntityController {
    public EnderDragonController() {
        super(EntityEnderDragonNPC.class, EntityType.ENDER_DRAGON);
    }

    @Override
    public org.bukkit.entity.EnderDragon getBukkitEntity() {
        return (org.bukkit.entity.EnderDragon) super.getBukkitEntity();
    }

    public static class EnderDragonNPC extends CraftEnderDragon implements ForwardingNPCHolder {
        public EnderDragonNPC(EntityEnderDragonNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
        }
    }

    public static class EntityEnderDragonNPC extends EnderDragon implements NPCHolder {
        private final CitizensNPC npc;

        public EntityEnderDragonNPC(EntityType<? extends EnderDragon> types, Level level) {
            this(types, level, null);
        }

        public EntityEnderDragonNPC(EntityType<? extends EnderDragon> types, Level level, NPC npc) {
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
        public void aiStep() {
            if (npc == null) {
                super.aiStep();
                return;
            }
            NMSImpl.updateMinecraftAIState(npc, this);
            npc.update();

            if (npc.useMinecraftAI()) {
                super.aiStep();
                return;
            }
            if (isDeadOrDying()) {
                setHealth(0F);
                return;
            }
            flightHistory.record(getY(), getYRot());

            float[][] pos = NMS.calculateDragonPositions(getYRot(),
                    new double[][] { toa(flightHistory.get(0, 1F)), toa(flightHistory.get(5, 1F)),
                            toa(flightHistory.get(10, 1F)), toa(flightHistory.get(12, 1F)),
                            toa(flightHistory.get(14, 1F)), toa(flightHistory.get(16, 1F)) });
            for (int j = 0; j < subEntities.length; ++j) {
                Vec3 vec3 = new Vec3(this.subEntities[j].getX(), this.subEntities[j].getY(),
                        this.subEntities[j].getZ());
                subEntities[j].setPos(this.getX() + pos[j][0], this.getY() + pos[j][1], this.getZ() + pos[j][2]);
                subEntities[j].xo = subEntities[j].xOld = vec3.x;
                subEntities[j].yo = subEntities[j].yOld = vec3.y;
                subEntities[j].zo = subEntities[j].zOld = vec3.z;
            }
            if (getFirstPassenger() != null) {
                setYRot(getFirstPassenger().getBukkitYaw() - 180);
            }
            Vec3 mot = getDeltaMovement();
            if (mot.x != 0 || mot.y != 0 || mot.z != 0) {
                if (getFirstPassenger() == null) {
                    setYRot(Util.getYawFromVelocity(getBukkitEntity(), mot.x, mot.z));
                }
                move(MoverType.SELF, mot);
                mot = mot.multiply(0.98, 0.91, 0.98);
                setDeltaMovement(mot);
            }
            if (NMSImpl.ENDERDRAGON_CHECK_WALLS != null && npc.hasTrait(EnderDragonTrait.class)
                    && npc.getOrAddTrait(EnderDragonTrait.class).isDestroyWalls()) {
                for (int i = 0; i < 3; i++) {
                    try {
                        this.inWall |= (boolean) NMSImpl.ENDERDRAGON_CHECK_WALLS.invoke(this,
                                subEntities[i].getBoundingBox());
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
            if (npc.data().get(NPC.Metadata.COLLIDABLE, false)) {
                try {
                    NMSImpl.ENDERDRAGON_KNOCKBACK.invoke(this, this.level(),
                            this.level().getEntities(this,
                                    subEntities[6].getBoundingBox().inflate(4.0, 2.0, 4.0).move(0.0, -2.0, 0.0),
                                    EntitySelector.NO_CREATIVE_OR_SPECTATOR));
                    NMSImpl.ENDERDRAGON_KNOCKBACK.invoke(this, this.level(),
                            this.level().getEntities(this,
                                    subEntities[7].getBoundingBox().inflate(4.0, 2.0, 4.0).move(0.0, -2.0, 0.0),
                                    EntitySelector.NO_CREATIVE_OR_SPECTATOR));
                    NMSImpl.ENDERDRAGON_HURT.invoke(this, this.level(), this.level().getEntities(this,
                            subEntities[0].getBoundingBox().inflate(1.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
                    NMSImpl.ENDERDRAGON_HURT.invoke(this, this.level(), this.level().getEntities(this,
                            subEntities[1].getBoundingBox().inflate(1.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR));
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
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
        public SoundEvent getAmbientSound() {
            return NMSImpl.getSoundEffect(npc, super.getAmbientSound(), NPC.Metadata.AMBIENT_SOUND);
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new EnderDragonNPC(this));
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
        protected void reallyHurt(ServerLevel level, DamageSource source, float f) {
            if (npc == null)
                return;

            Vec3 old = getDeltaMovement();
            super.reallyHurt(level, source, f);
            if (getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.HOVERING) {
                setDeltaMovement(old);
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

        private double[] toa(Sample sample) {
            return new double[] { sample.y(), sample.yRot() };
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
