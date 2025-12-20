package com.fiisadev.vs_logistics.event;

import com.fiisadev.vs_logistics.VSLogistics;
import com.fiisadev.vs_logistics.content.fluid_port.FluidPortBlockEntity;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpBlockEntity;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpPlayerData;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpPlayerDataProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = VSLogistics.MOD_ID)
public class FluidPumpHandler {
    private static final Map<UUID, Boolean> keyHeld = new HashMap<>();
    public static void setNozzleKey(UUID playerId, boolean value) {
        keyHeld.put(playerId, value);
    }
    public static boolean isNozzleKeyDown(UUID playerId) { return keyHeld.getOrDefault(playerId, false); }

    private static boolean preventEvent(Player player) {
        LazyOptional<FluidPumpPlayerData> cap = player.getCapability(FluidPumpPlayerDataProvider.FLUID_PUMP_PLAYER_DATA);
        return cap.resolve().map((playerData) -> playerData.getFluidPumpPos() != null).orElse(false);
    }

    @SubscribeEvent
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (preventEvent(event.getEntity()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());

        if (be instanceof FluidPumpBlockEntity || be instanceof FluidPortBlockEntity)
            return;

        if (preventEvent(event.getEntity())) {
            event.getEntity().stopUsingItem();
            event.setCanceled(true);
        }
    }
}
