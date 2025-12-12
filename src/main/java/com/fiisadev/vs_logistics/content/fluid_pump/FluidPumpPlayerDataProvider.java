package com.fiisadev.vs_logistics.content.fluid_pump;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluidPumpPlayerDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<FluidPumpPlayerData> FLUID_PUMP_PLAYER_DATA = CapabilityManager.get(new CapabilityToken<>() { });

    private FluidPumpPlayerData playerData = null;
    private final LazyOptional<FluidPumpPlayerData> optional = LazyOptional.of(this::createPlayerData);

    private FluidPumpPlayerData createPlayerData() {
        if (playerData == null)
            playerData = new FluidPumpPlayerData();

        return playerData;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == FLUID_PUMP_PLAYER_DATA) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        createPlayerData().saveNBTData(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        createPlayerData().loadNBTData(tag);
    }
}
