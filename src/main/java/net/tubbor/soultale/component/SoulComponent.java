
package net.tubbor.soultale.component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.entity.RespawnableComponent;

public class SoulComponent implements AutoSyncedComponent, RespawnableComponent<SoulComponent> {
    private final PlayerEntity player;
    private String soul;

    public SoulComponent(PlayerEntity player) {
        this.player = player;
        this.soul = null;
    }

    public void setSoul(String soul) {
        this.soul = soul;
        SoulTaleComponents.SOUL.sync(player);
    }

    public String getSoul() {
        return soul;
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (tag.contains("soul", NbtElement.STRING_TYPE)) {
            this.soul = tag.getString("soul");
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        if (soul != null) {
            tag.putString("soul", soul);
        }
    }

    @Override
    public boolean shouldCopyForRespawn(boolean lossless, boolean keepInventory, boolean sameCharacter) {
        // Always copy the soul component when the player respawns
        return true;
    }

    public void copyForRespawn(SoulComponent original, boolean lossless, boolean keepInventory, boolean sameCharacter) {
        // Copy the soul data from the original component
        this.soul = original.soul;
    }
}