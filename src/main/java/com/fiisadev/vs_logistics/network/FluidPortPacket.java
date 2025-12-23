package com.fiisadev.vs_logistics.network;

import com.fiisadev.vs_logistics.content.fluid_port.FluidPortBlockEntity;
import com.fiisadev.vs_logistics.content.fluid_port.FluidPortTarget;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FluidPortPacket {

    private final BlockPos fluidPortPos;
    private final BlockPos targetPos;

    public FluidPortPacket(BlockPos fluidPortPos, BlockPos targetPos) {
        this.fluidPortPos = fluidPortPos;
        this.targetPos = targetPos;
    }

    public FluidPortPacket(FriendlyByteBuf buf) {
        this.fluidPortPos = buf.readBlockPos();
        this.targetPos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(fluidPortPos);
        buf.writeBlockPos(targetPos);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();

        ctx.enqueueWork(() -> {
            Player sender = ctx.getSender();
            if (sender == null) return;

            if (sender.level().getBlockEntity(fluidPortPos) instanceof FluidPortBlockEntity fluidPort) {
                BlockPos pos = targetPos;
                if (sender.level().getBlockEntity(targetPos) instanceof IMultiBlockEntityContainer.Fluid multiBlock)
                    pos = multiBlock.getController();

                FluidPortTarget target = fluidPort.getTargets().get(pos);

                if (target == null)
                    fluidPort.addTarget(pos);
                else if (target.nextMode() == null)
                    fluidPort.getTargets().remove(pos);

                target = fluidPort.getTargets().get(pos);

                if (target != null) {
                    FluidPortTarget.Mode mode = target.getMode();

                    if (mode != null) {
                        sender.displayClientMessage(
                                CreateLang.builder()
                                        .text("Target mode now set to ")
                                        .text(mode.color, mode.name().toUpperCase())
                                        .component(),
                                true
                        );
                    }
                }

                fluidPort.setChanged();
                fluidPort.sendDataImmediately();
            }
        });

        ctx.setPacketHandled(true);
    }
}
