package com.fiisadev.vs_logistics.event;

import com.fiisadev.vs_logistics.VSLogistics;
import com.fiisadev.vs_logistics.content.fluid_port.FluidPortBlockEntity;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpBlockEntity;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpPlayerDataProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = VSLogistics.MOD_ID)
public class NozzleUseHandler {
    private static final Map<UUID, Boolean> keyHeld = new HashMap<>();

    public static void set(UUID playerId, boolean value) {
        keyHeld.put(playerId, value);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.side != LogicalSide.SERVER) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            handle(player, player.level());
        }
    }

    private static void pushFluid(IFluidHandler from, IFluidHandler to) {
        // TODO config pump rate
        FluidStack simulatedExtract = from.drain(60, IFluidHandler.FluidAction.SIMULATE);
        if (simulatedExtract.isEmpty()) return;

        int accepted = to.fill(simulatedExtract, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) return;

        FluidStack realExtract = from.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
        if (realExtract.isEmpty()) return;

        to.fill(realExtract, IFluidHandler.FluidAction.EXECUTE);
    }

    private static void pullFluid(FluidPortBlockEntity fluidPort, IFluidHandler to) {
        IFluidHandler source;

        if (!fluidPort.getFluidTank().getFluid().isEmpty()) {
            source = fluidPort.getFluidTank();
        } else {
            source = fluidPort.getTargetTank().orElse(null);
        }

        if (source == null) return;

        // TODO config pump rate
        FluidStack simulatedExtract = source.drain(60, IFluidHandler.FluidAction.SIMULATE);
        if (simulatedExtract.isEmpty()) return;

        int accepted = to.fill(simulatedExtract, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) return;

        FluidStack realExtract = source.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
        if (realExtract.isEmpty()) return;

        to.fill(realExtract, IFluidHandler.FluidAction.EXECUTE);
    }


    private static void handle(Player player, Level level) {
        if (!keyHeld.getOrDefault(player.getUUID(), false))
            return;

        player.getCapability(FluidPumpPlayerDataProvider.FLUID_PUMP_PLAYER_DATA).ifPresent((playerData) -> {

            BlockPos fluidPumpPos = playerData.getFluidPumpPos();
            if (fluidPumpPos == null) return;
            if (!(level.getBlockEntity(fluidPumpPos) instanceof FluidPumpBlockEntity fluidPump)) return;

            HitResult result = player.pick(5f, 0f, true);

            if (result instanceof BlockHitResult hit) {
                if (!(level.getBlockEntity(hit.getBlockPos()) instanceof FluidPortBlockEntity fluidPort)) return;
                if (!fluidPort.isValid()) return;

                switch (fluidPump.getMode()) {
                    case PUSH ->
                        pushFluid(fluidPump.getFluidTank(), fluidPort.getFluidTank());
                    case PULL ->
                        pullFluid(fluidPort, fluidPump.getFluidTank());
                }
            }
        });
    }

    private static boolean preventEvent(Player player) {
        AtomicBoolean result = new AtomicBoolean(false);

        player.getCapability(FluidPumpPlayerDataProvider.FLUID_PUMP_PLAYER_DATA).ifPresent((playerData) -> {
            result.set(playerData.getFluidPumpPos() != null);
        });

        return result.get();
    }

    @SubscribeEvent
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (preventEvent(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().getBlockEntity(event.getPos()) instanceof FluidPumpBlockEntity) return;

        if (preventEvent(event.getEntity())) {
            event.getEntity().stopUsingItem();
            event.setCanceled(true);
        }
    }
}
