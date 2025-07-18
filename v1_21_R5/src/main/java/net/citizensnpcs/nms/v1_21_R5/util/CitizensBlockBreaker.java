package net.citizensnpcs.nms.v1_21_R5.util;

import net.citizensnpcs.util.AbstractBlockBreaker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.inventory.CraftItemStack;

public class CitizensBlockBreaker extends AbstractBlockBreaker {
    public CitizensBlockBreaker(org.bukkit.entity.Entity entity, org.bukkit.block.Block target,
            BlockBreakerConfiguration config) {
        super(entity, target, config);
    }

    private ItemStack getCurrentItem() {
        return configuration.item() != null ? CraftItemStack.asNMSCopy(configuration.item())
                : getHandle() instanceof LivingEntity ? ((LivingEntity) getHandle()).getMainHandItem() : null;
    }

    @Override
    protected float getDamage(int tickDifference) {
        return getStrength(getHandle().level().getBlockState(new BlockPos(x, y, z))) * (tickDifference + 1)
                * configuration.blockStrengthModifier();
    }

    private Entity getHandle() {
        return NMSImpl.getHandle(entity);
    }

    protected float getStrength(BlockState block) {
        float base = block.getDestroySpeed(null, BlockPos.ZERO);
        return base < 0.0F ? 0.0F : !isDestroyable(block) ? 1.0F / base / 100.0F : strengthMod(block) / base / 30.0F;
    }

    private boolean isDestroyable(BlockState block) {
        if (block.requiresCorrectToolForDrops())
            return true;
        else {
            ItemStack current = getCurrentItem();
            return current != null ? current.isCorrectToolForDrops(block) : false;
        }
    }

    @Override
    protected void setBlockDamage(int modifiedDamage) {
        ((ServerLevel) getHandle().level()).destroyBlockProgress(getHandle().getId(), new BlockPos(x, y, z),
                modifiedDamage);
    }

    private float strengthMod(BlockState block) {
        ItemStack itemstack = getCurrentItem();
        float f = itemstack.getDestroySpeed(block);
        if (getHandle() instanceof LivingEntity) {
            LivingEntity handle = (LivingEntity) getHandle();
            if (f > 1.0F) {
                f += (float) handle.getAttributeValue(Attributes.MINING_EFFICIENCY);
            }
            if (MobEffectUtil.hasDigSpeed(handle)) {
                f *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(handle) + 1) * 0.2F;
            }
            if (handle.hasEffect(MobEffects.MINING_FATIGUE)) {
                float f1;
                switch (handle.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                    case 0:
                        f1 = 0.3F;
                        break;
                    case 1:
                        f1 = 0.09F;
                        break;
                    case 2:
                        f1 = 0.0027F;
                        break;
                    case 3:
                    default:
                        f1 = 8.1E-4F;
                }
                f *= f1;
            }
            f *= (float) handle.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
            if (handle.isEyeInFluid(FluidTags.WATER)) {
                f *= (float) handle.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue();
            }
        }
        if (!entity.isOnGround()) {
            f /= 5.0F;
        }
        return f;
    }
}