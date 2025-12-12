package com.fiisadev.vs_logistics.content.fluid_port;

import com.fiisadev.vs_logistics.registry.LogisticsBlocks;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

public class FluidPortItem extends BlockItem {
    public FluidPortItem(Block block, Properties properties) {
        super(block, properties);
    }

    private @Nullable BlockPos getTarget(ItemStack item) {
        CompoundTag tag = item.getOrCreateTag();
        long pos = tag.getLong("BlockPos");
        if (pos == 0) return null;
        return BlockPos.of(pos);
    }

    private void setTarget(ItemStack item, @Nullable BlockPos pos) {
        CompoundTag tag = item.getOrCreateTag();
        tag.putLong("BlockPos", pos == null ? 0 : pos.asLong());
    }

    @Override
    public void inventoryTick(@NotNull ItemStack item, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (!isSelected || !level.isClientSide) return;

        BlockPos pos = getTarget(item);
        if (pos == null) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!FluidPortBlock.isFluidTank(be)) return;

        AABB box = AABB.unitCubeFromLowerCorner(Vec3.ZERO).move(pos);

        if (be instanceof IMultiBlockEntityContainer.Fluid multiBlock) {
            if (level.getBlockEntity(multiBlock.getController()) instanceof IMultiBlockEntityContainer.Fluid controller) {
                box = new AABB(0, 0, 0, controller.getWidth(), controller.getHeight(), controller.getWidth())
                    .move(multiBlock.getController());
            }
    }

        Outliner.getInstance()
                .showAABB(Pair.of("selectedFluidTank", pos), box)
                .colored(new Color(255, 238, 140))
                .lineWidth(1 / 16f);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        if (level.isClientSide) return InteractionResultHolder.pass(item);

        if (player.isShiftKeyDown()) {
            setTarget(item, null);
            return InteractionResultHolder.fail(item);
        }

        return InteractionResultHolder.pass(item);
    }

    private static boolean isOnShip(@NotNull Level level, @Nullable BlockPos pos) {
        return pos != null && VSGameUtilsKt.getShipManagingPos(level, pos) != null;
    }

    private static boolean isOnSameShip(@NotNull Level level, @Nullable BlockPos a, @Nullable BlockPos b) {
        if (a == null || b == null) return false;

        Ship shipA = VSGameUtilsKt.getShipManagingPos(level, a);
        Ship shipB = VSGameUtilsKt.getShipManagingPos(level, b);

        return shipA != null && shipB != null && shipA.getId() == shipB.getId();
    }

    private void invalidConnection(Player player, String message) {
        player.displayClientMessage(
                Component.literal(message)
                        .withStyle(style -> style.withColor(Color.RED.getRGB())),
                true
        );
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        ItemStack item = ctx.getItemInHand();
        BlockEntity be = ctx.getLevel().getBlockEntity(ctx.getClickedPos());

        if (player == null) return InteractionResult.FAIL;

        if (player.isShiftKeyDown()) {
            setTarget(item, null);
            return InteractionResult.FAIL;
        }

        if (FluidPortBlock.isFluidTank(be)) {
            if (!isOnShip(ctx.getLevel(), ctx.getClickedPos())) {
                invalidConnection(player, be.getBlockState().getBlock().getName().getString() + " must be placed on a ship.");
                return InteractionResult.FAIL;
            }

            setTarget(item, ctx.getClickedPos());
            return InteractionResult.SUCCESS;
        }

        BlockPos target = getTarget(item);

        if (target == null) {
            return InteractionResult.FAIL;
        }

        if (!isOnSameShip(ctx.getLevel(), target, ctx.getClickedPos())) {
            String name = ctx.getLevel().getBlockState(target).getBlock().getName().getString();
            invalidConnection(player, "Fluid Port must be placed on the same ship as " + name + ".");
            return InteractionResult.FAIL;
        }

        return super.useOn(ctx);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(@NotNull BlockPos pos, Level level, @Nullable Player player, @NotNull ItemStack item, @NotNull BlockState state) {
        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof FluidPortBlockEntity fluidPort) {
            BlockPos blockPos = getTarget(item);
            if (blockPos == null) return false;

            fluidPort.setTarget(blockPos);
            setTarget(item, null);
            return true;
        }

        return false;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack item) {
        return getTarget(item) != null;
    }
}
