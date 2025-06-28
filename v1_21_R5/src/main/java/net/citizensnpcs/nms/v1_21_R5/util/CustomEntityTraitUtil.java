package net.citizensnpcs.nms.v1_21_R5.util;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.CustomEntityTrait;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import javax.annotation.Nullable;

public class CustomEntityTraitUtil {

    @Nullable
    public static ClientboundAddEntityPacket packet(NPC npc, ServerEntity entitytrackerentry, Entity entity) {
        CustomEntityTrait customEntityTrait = npc.getTraitNullable(CustomEntityTrait.class);
        if (customEntityTrait != null && customEntityTrait.getCustomEntityName() != null) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(ResourceLocation.parse(customEntityTrait.getCustomEntityName()));
            return new ClientboundAddEntityPacket(
                    entity.getId(),
                    entity.getUUID(),
                    entitytrackerentry.getPositionBase().x(),
                    entitytrackerentry.getPositionBase().y(),
                    entitytrackerentry.getPositionBase().z(),
                    entitytrackerentry.getLastSentXRot(),
                    entitytrackerentry.getLastSentYRot(),
                    entityType,
                    0,
                    entitytrackerentry.getLastSentMovement(),
                    entitytrackerentry.getLastSentYHeadRot()
            );
        }
        return null;
    }
}
