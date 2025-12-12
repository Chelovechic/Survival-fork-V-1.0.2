package com.fiisadev.vs_logistics.client.event;

import com.fiisadev.vs_logistics.VSLogistics;
import com.fiisadev.vs_logistics.network.NozzleUsePacket;
import com.fiisadev.vs_logistics.registry.LogisticsNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VSLogistics.MOD_ID, value = Dist.CLIENT)
public class InputEvent {
    public static boolean isUseHeld = false;

    private static boolean isDropHeld = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.screen != null) return;

        if (!isUseHeld && mc.options.keyUse.isDown()) {
            isUseHeld = true;
            LogisticsNetwork.CHANNEL.sendToServer(new NozzleUsePacket(true, false));
        }

        if (isUseHeld && !mc.options.keyUse.isDown()) {
            isUseHeld = false;
            LogisticsNetwork.CHANNEL.sendToServer(new NozzleUsePacket(false, false));
        }

        if (mc.options.keyDrop.isDown() && !isDropHeld) {
            LogisticsNetwork.CHANNEL.sendToServer(new NozzleUsePacket(isUseHeld, true));
            isDropHeld = true;
        }

        if (!mc.options.keyDrop.isDown()) {
            isDropHeld = false;
        }
    }
}
