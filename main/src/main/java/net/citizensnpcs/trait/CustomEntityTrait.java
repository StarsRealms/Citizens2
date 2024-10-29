package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.Location;

@TraitName("customentity")
public class CustomEntityTrait extends Trait {

    @Persist("customentityname")
    private String customEntityName;

    public CustomEntityTrait() {
        super("customentity");
    }

    public void setCustomEntityName(String customEntityName) {
        this.customEntityName = customEntityName;
    }

    public String getCustomEntityName() {
        return customEntityName;
    }

    @Override
    public void onAttach() {
        Location location =  this.npc.getStoredLocation();
        this.getNPC().despawn();
        this.getNPC().spawn(location);
    }

    @Override
    public void onRemove() {
        Location location =  this.npc.getStoredLocation();
        this.getNPC().despawn();
        this.getNPC().spawn(location);
    }
}
