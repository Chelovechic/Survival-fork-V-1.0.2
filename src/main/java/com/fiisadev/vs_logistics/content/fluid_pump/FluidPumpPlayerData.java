package com.fiisadev.vs_logistics.content.fluid_pump;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public class FluidPumpPlayerData {
    private @Nullable BlockPos fluidPumpPos = null;

    public @Nullable BlockPos getFluidPumpPos() {
        return fluidPumpPos;
    }

    public void setFluidPumpPos(@Nullable BlockPos pos) {
        fluidPumpPos = pos;
    }

    public void saveNBTData(CompoundTag tag) {
        if (fluidPumpPos == null) {
            tag.remove("BoundTo");
        } else {
            tag.putLong("BoundTo", fluidPumpPos.asLong());
        }
    }

    public void loadNBTData(CompoundTag tag) {
        if (tag.contains("BoundTo")) {
            fluidPumpPos = BlockPos.of(tag.getLong("BoundTo"));
        } else {
            fluidPumpPos = null;
        }
    }
}
