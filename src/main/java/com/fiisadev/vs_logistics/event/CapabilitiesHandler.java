package com.fiisadev.vs_logistics.event;

import com.fiisadev.vs_logistics.VSLogistics;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpPlayerData;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpPlayerDataProvider;
import com.fiisadev.vs_logistics.network.SyncFluidPumpPlayerCapPacket;
import com.fiisadev.vs_logistics.registry.LogisticsNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = VSLogistics.MOD_ID)
public class CapabilitiesHandler {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        player.getCapability(FluidPumpPlayerDataProvider.FLUID_PUMP_PLAYER_DATA).ifPresent(cap -> {
            LogisticsNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncFluidPumpPlayerCapPacket(player, cap)
            );
        });
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(FluidPumpPlayerDataProvider.FLUID_PUMP_PLAYER_DATA).isPresent()) {
                event.addCapability(VSLogistics.asResource("fluid_pump_player_data"), new FluidPumpPlayerDataProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent event) {
        event.register(FluidPumpPlayerData.class);
    }
}
