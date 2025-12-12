package com.fiisadev.vs_logistics.event;

import com.fiisadev.vs_logistics.VSLogistics;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpBlockEntity;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpPlayerDataProvider;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VSLogistics.MOD_ID)
public class FluidPumpEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player.level() instanceof ServerLevel level)) return;

        event.player.getCapability(FluidPumpPlayerDataProvider.FLUID_PUMP_PLAYER_DATA).ifPresent((playerData) -> {
            if (playerData.getFluidPumpPos() == null)
                return;

            if (!(level.getBlockEntity(playerData.getFluidPumpPos()) instanceof FluidPumpBlockEntity)) {
                FluidPumpUtils.dropNozzle(event.player);
                return;
            }

            if (event.player.position().distanceToSqr(playerData.getFluidPumpPos().getCenter()) > Math.pow(24, 2)) {
                FluidPumpUtils.dropNozzle(event.player);
            }
        });
    }
}
