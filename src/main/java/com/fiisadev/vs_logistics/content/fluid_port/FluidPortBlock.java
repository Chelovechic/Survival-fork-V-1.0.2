package com.fiisadev.vs_logistics.content.fluid_port;

import com.fiisadev.vs_logistics.registry.LogisticsBlockEntities;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.Nullable;

public class FluidPortBlock extends DirectionalBlock implements IBE<FluidPortBlockEntity> {
    public FluidPortBlock(Properties properties) {
        super(properties);
    }

    public static boolean isFluidTank(BlockEntity target) {
        if (target == null) return false;
        if (target instanceof FluidPortBlockEntity) return false;
        return target.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
    }

    @Override
    public Class<FluidPortBlockEntity> getBlockEntityClass() {
        return FluidPortBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FluidPortBlockEntity> getBlockEntityType() {
        return LogisticsBlockEntities.FLUID_PORT.get();
    }
}
