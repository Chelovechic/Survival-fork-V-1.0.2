package com.fiisadev.vs_logistics.event;

import com.fiisadev.vs_logistics.VSLogistics;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpPlayerData;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpPlayerDataProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VSLogistics.MOD_ID)
public class RegisterCapabilitiesEvent {
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
