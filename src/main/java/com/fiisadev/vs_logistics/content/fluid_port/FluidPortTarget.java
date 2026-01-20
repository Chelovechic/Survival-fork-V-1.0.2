package com.fiisadev.vs_logistics.content.fluid_port;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FluidPortTarget {
    public enum Mode {
        PUSH(16691293),
        PULL(5929677),
        EQUALIZE(2282554);

        public final int color;

        Mode(int color) {
            this.color = color;
        }
    }

    public static final Mode[] MODES = Mode.values();

    private final BlockPos pos;
    private Mode mode;

    public FluidPortTarget(BlockPos pos, Mode mode) {
        this.pos = pos;
        this.mode = mode;
    }

    public FluidPortTarget(BlockPos pos) {
        this(pos, Mode.PUSH);
    }

    public BlockPos getPos() {
        return pos;
    }

    public Mode getMode() { return mode; }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public @Nullable FluidPortTarget.Mode getNextMode() {
        int nextOrdinal = this.mode.ordinal() + 1;
        if (nextOrdinal >= MODES.length) return null;
        return MODES[nextOrdinal];
    }

    public @Nullable FluidPortTarget.Mode nextMode() {
        Mode mode = getNextMode();
        setMode(mode);
        return mode;
    }

    private LazyOptional<IFluidHandler> handler;

    public LazyOptional<IFluidHandler> getFluidHandler(Level level) {
        if (handler == null) {
            handler = Optional.ofNullable(level.getBlockEntity(pos)).map(be -> be.getCapability(ForgeCapabilities.FLUID_HANDLER)).orElse(LazyOptional.empty());
        }

        return handler;
    }
}
