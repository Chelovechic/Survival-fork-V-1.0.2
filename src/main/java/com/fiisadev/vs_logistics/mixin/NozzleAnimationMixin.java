package com.fiisadev.vs_logistics.mixin;

import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpPlayerDataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class NozzleAnimationMixin<T extends LivingEntity> {

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("TAIL")
    )
    private void disableRightArmWalk(T entity,
                                     float limbSwing,
                                     float limbSwingAmount,
                                     float ageInTicks,
                                     float netHeadYaw,
                                     float headPitch,
                                     CallbackInfo ci) {

        if (entity instanceof Player player) {
            player.getCapability(FluidPumpPlayerDataProvider.FLUID_PUMP_PLAYER_DATA).ifPresent((playerData) -> {
                if (playerData.getFluidPumpPos() == null) return;

                PlayerModel<?> model = (PlayerModel<?>)(Object)this;

                model.rightArm.xRot = (float)Math.toRadians(-25);
                model.rightSleeve.xRot = (float)Math.toRadians(-25);

                model.rightArm.zRot = (float)Math.toRadians(5);
                model.rightSleeve.zRot = (float)Math.toRadians(5);
            });
        }
    }
}
