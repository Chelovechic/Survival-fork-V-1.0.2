package com.fiisadev.vs_logistics.content.fluid_pump;

import com.fiisadev.vs_logistics.network.SyncFluidPumpPlayerCapPacket;
import com.fiisadev.vs_logistics.registry.LogisticsNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class FluidPumpUtils {
    public static void startUsing(Player player, @NotNull BlockPos fluidPumpPos) {
        player.getCapability(FluidPumpPlayerDataProvider.FLUID_PUMP_PLAYER_DATA).ifPresent((playerData) -> {
            if (!(player.level().getBlockEntity(fluidPumpPos) instanceof FluidPumpBlockEntity fluidPump)) return;

            fluidPump.usedBy = player.getUUID();
            playerData.setFluidPumpPos(fluidPumpPos);
            fluidPump.notifyUpdate();

            LogisticsNetwork.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new SyncFluidPumpPlayerCapPacket(SyncFluidPumpPlayerCapPacket.Type.NEW_POSITION, fluidPumpPos, player.getUUID())
            );
        });
    }

    private static void stopUsing(Player player, boolean destroyed) {
        player.getCapability(FluidPumpPlayerDataProvider.FLUID_PUMP_PLAYER_DATA).ifPresent((playerData) -> {
            BlockPos fluidPumpPos = playerData.getFluidPumpPos();
            if (fluidPumpPos == null) return;

            playerData.setFluidPumpPos(null);

            if (player.level().getBlockEntity(fluidPumpPos) instanceof FluidPumpBlockEntity fluidPump) {
                fluidPump.usedBy = null;
                fluidPump.notifyUpdate();
            }

            LogisticsNetwork.CHANNEL.send(
                PacketDistributor.ALL.noArg(),
                new SyncFluidPumpPlayerCapPacket(
                    destroyed ? SyncFluidPumpPlayerCapPacket.Type.DESTROYED : SyncFluidPumpPlayerCapPacket.Type.NULL,
                    null,
                    player.getUUID()
                )
            );
        });
    }

    public static void stopUsing(Player player) {
        stopUsing(player, false);
    }

    public static void dropNozzle(Player player) {
        stopUsing(player, true);
    }
}
