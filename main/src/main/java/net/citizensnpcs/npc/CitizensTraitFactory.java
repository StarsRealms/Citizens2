package net.citizensnpcs.npc;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitFactory;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.TraitTemplateParser;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.api.trait.trait.MobType;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.trait.trait.PlayerFilter;
import net.citizensnpcs.api.trait.trait.Spawned;
import net.citizensnpcs.trait.*;
import net.citizensnpcs.trait.text.Text;
import net.citizensnpcs.trait.waypoint.Waypoints;
import net.citizensnpcs.util.EntityPacketTracker;
import net.citizensnpcs.util.NMS;

public class CitizensTraitFactory implements TraitFactory {
    private final List<TraitInfo> defaultTraits = Lists.newArrayList();
    private final Map<String, TraitInfo> registered = Maps.newHashMap();

    public CitizensTraitFactory(Citizens plugin) {
        registerTrait(TraitInfo.create(Age.class));
        registerTrait(TraitInfo.create(ArmorStandTrait.class));
        registerTrait(TraitInfo.create(AttributeTrait.class));
        registerTrait(TraitInfo.create(Anchors.class).optInToStats());
        registerTrait(TraitInfo.create(BatTrait.class));
        registerTrait(TraitInfo.create(BehaviorTrait.class).optInToStats()
                .withTemplateParser(BehaviorTrait.createTemplateParser()));
        registerTrait(TraitInfo.create(BoundingBoxTrait.class));
        registerTrait(TraitInfo.create(ClickRedirectTrait.class));
        registerTrait(TraitInfo.create(ChunkTicketTrait.class));
        registerTrait(TraitInfo.create(CommandTrait.class).optInToStats());
        registerTrait(TraitInfo.create(Controllable.class).optInToStats());
        registerTrait(TraitInfo.create(CurrentLocation.class));
        registerTrait(TraitInfo.create(DropsTrait.class).optInToStats());
        registerTrait(TraitInfo.create(EnderCrystalTrait.class));
        registerTrait(TraitInfo.create(EndermanTrait.class));
        registerTrait(TraitInfo.create(EntityPoseTrait.class));
        registerTrait(TraitInfo.create(Equipment.class));
        registerTrait(TraitInfo.create(FollowTrait.class).optInToStats());
        registerTrait(TraitInfo.create(ForcefieldTrait.class).optInToStats());
        registerTrait(TraitInfo.create(GameModeTrait.class));
        registerTrait(TraitInfo.create(Gravity.class));
        registerTrait(TraitInfo.create(HomeTrait.class).optInToStats());
        registerTrait(TraitInfo.create(HorseModifiers.class));
        registerTrait(TraitInfo.create(HologramTrait.class).optInToStats());
        registerTrait(TraitInfo.create(Inventory.class));
        registerTrait(TraitInfo.create(ItemFrameTrait.class));
        registerTrait(TraitInfo.create(LookClose.class).optInToStats());
        registerTrait(TraitInfo.create(PaintingTrait.class));
        registerTrait(TraitInfo.create(MirrorTrait.class).optInToStats());
        registerTrait(TraitInfo.create(MountTrait.class));
        registerTrait(TraitInfo.create(MobType.class).asDefaultTrait());
        registerTrait(TraitInfo.create(OcelotModifiers.class));
        registerTrait(TraitInfo.create(Owner.class));
        registerTrait(TraitInfo.create(PacketNPC.class).optInToStats());
        registerTrait(TraitInfo.create(PausePathfindingTrait.class).optInToStats());
        registerTrait(
                TraitInfo.create(PlayerFilter.class).optInToStats().withSupplier(() -> new PlayerFilter((p, e) -> {
                    EntityPacketTracker ept = NMS.getPacketTracker(e);
                    if (ept != null) {
                        ept.unlink(p);
                    }
                }, (p, e) -> {
                    EntityPacketTracker ept = NMS.getPacketTracker(e);
                    if (ept != null) {
                        ept.link(p);
                    }
                })));
        registerTrait(TraitInfo.create(Poses.class).optInToStats());
        registerTrait(TraitInfo.create(Powered.class));
        registerTrait(TraitInfo.create(RabbitType.class));
        registerTrait(TraitInfo.create(RotationTrait.class));
        registerTrait(TraitInfo.create(Saddle.class));
        registerTrait(TraitInfo.create(ScaledMaxHealthTrait.class));
        registerTrait(TraitInfo.create(ScoreboardTrait.class));
        registerTrait(TraitInfo.create(SitTrait.class).optInToStats());
        registerTrait(
                TraitInfo.create(ShopTrait.class).optInToStats().withSupplier(() -> new ShopTrait(plugin.getShops())));
        registerTrait(TraitInfo.create(SleepTrait.class));
        registerTrait(TraitInfo.create(SheepTrait.class));
        registerTrait(TraitInfo.create(SkinLayers.class));
        registerTrait(TraitInfo.create(SkinTrait.class));
        registerTrait(TraitInfo.create(SneakTrait.class));
        registerTrait(TraitInfo.create(SlimeSize.class));
        registerTrait(TraitInfo.create(Spawned.class));
        registerTrait(TraitInfo.create(Text.class).optInToStats());
        registerTrait(TraitInfo.create(TargetableTrait.class));
        registerTrait(TraitInfo.create(Waypoints.class).optInToStats());
        registerTrait(TraitInfo.create(WitherTrait.class));
        registerTrait(TraitInfo.create(WoolColor.class));
        registerTrait(TraitInfo.create(WolfModifiers.class));
        registerTrait(TraitInfo.create(VillagerProfession.class));
        registerTrait(TraitInfo.create(CustomEntityTrait.class));
    }

    @Override
    public void addDefaultTraits(NPC npc) {
        for (TraitInfo info : defaultTraits) {
            npc.addTrait(create(info));
        }
    }

    private <T extends Trait> T create(TraitInfo info) {
        return info.tryCreateInstance();
    }

    @Override
    public void deregisterTrait(TraitInfo info) {
        Objects.requireNonNull(info, "info cannot be null");
        registered.remove(info.getTraitName());
    }

    @Override
    public Collection<TraitInfo> getRegisteredTraits() {
        return registered.values();
    }

    @Override
    public TraitTemplateParser getTemplateParser(String name) {
        TraitInfo info = registered.get(name.toLowerCase(Locale.ROOT));
        return info == null ? null : info.getParser();
    }

    @Override
    public <T extends Trait> T getTrait(Class<T> clazz) {
        for (TraitInfo entry : registered.values()) {
            if (clazz == entry.getTraitClass())
                return create(entry);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Trait> T getTrait(String name) {
        TraitInfo info = registered.get(name.toLowerCase(Locale.ROOT));
        if (info == null)
            return null;
        return (T) create(info);
    }

    @Override
    public Class<? extends Trait> getTraitClass(String name) {
        TraitInfo info = registered.get(name.toLowerCase(Locale.ROOT));
        return info == null ? null : info.getTraitClass();
    }

    @Override
    public void registerTrait(TraitInfo info) {
        Objects.requireNonNull(info, "info cannot be null");
        info.checkValid();
        if (registered.containsKey(info.getTraitName()))
            throw new IllegalArgumentException("Trait name " + info.getTraitName() + " already registered");
        registered.put(info.getTraitName(), info);
        if (info.isDefaultTrait()) {
            defaultTraits.add(info);
        }
        info.registerListener(CitizensAPI.getPlugin());
    }

    public boolean trackStats(Trait trait) {
        return registered.get(trait.getName()).shouldTrackStats();
    }
}