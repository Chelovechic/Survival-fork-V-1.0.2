package com.fiisadev.vs_logistics.client.event;

import com.fiisadev.vs_logistics.VSLogistics;
import com.fiisadev.vs_logistics.client.utils.HoseUtils;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpBlock;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpBlockEntity;
import com.fiisadev.vs_logistics.content.fluid_pump.FluidPumpPlayerDataProvider;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid = VSLogistics.MOD_ID, value = Dist.CLIENT)
public class HoseRenderer {
    public static final int SEGMENTS = HoseUtils.SEGMENTS;
    public static final int RADIAL_SEGMENTS = HoseUtils.RADIAL_SEGMENTS;
    public static final float RADIUS = HoseUtils.RADIUS;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        Vec3 view = mc.gameRenderer.getMainCamera().getPosition();
        if (mc.level == null) return;

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (Player player : mc.level.players()) {
            player.getCapability(FluidPumpPlayerDataProvider.FLUID_PUMP_PLAYER_DATA).ifPresent((playerData) -> {

                BlockPos fluidPumpPos = playerData.getFluidPumpPos();
                if (fluidPumpPos == null) return;
                if (!(mc.level.getBlockEntity(fluidPumpPos) instanceof FluidPumpBlockEntity fluidPump)) return;

                float partialTicks = event.getPartialTick();

                Vec3 hoseEndPos = player
                        .getPosition(partialTicks)
                        .add(HoseUtils.getNozzleHandlePosition(player, partialTicks));

                Vector3f startDirVF = fluidPump.getBlockState().getValue(FluidPumpBlock.FACING).step().normalize();
                Vec3 startDir = new Vec3(startDirVF.x, startDirVF.y, startDirVF.z);
                Vec3 endDir = HoseUtils.getNozzleHandleDir(player, partialTicks);

                Vec3 hoseStartPos = fluidPump
                        .getBlockPos()
                        .getCenter()
                        .add(new Vec3(0, 0, 1).yRot(-(float)Math.toRadians(fluidPump.getBlockState().getValue(FluidPumpBlock.FACING).toYRot())).scale((7 / 8f) * 0.5))
                        .add(0, -0.5 + 5 / 16f, 0);

                double dist = hoseStartPos.distanceTo(hoseEndPos);

                Vec3 p1 = hoseStartPos.add(startDir.scale(dist * 0.3f));
                Vec3 p2 = hoseEndPos.subtract(endDir.scale(dist * 0.3f));

                renderCurvedHose(builder, hoseStartPos, hoseEndPos, p1, p2, dist, player);
            });
        }

        vertexBuffer.bind();
        vertexBuffer.upload(builder.end());

        PoseStack matrix = event.getPoseStack();
        matrix.pushPose();
        matrix.translate(-view.x, -view.y, -view.z);
        var shader = GameRenderer.getPositionColorShader();
        vertexBuffer.drawWithShader(matrix.last().pose(), event.getProjectionMatrix(), shader);
        matrix.popPose();
        VertexBuffer.unbind();
    }

    private static void renderCurvedHose(BufferBuilder builder, Vec3 start, Vec3 end, Vec3 p1, Vec3 p2, double dist, Player player) {
        Vec3[] centers = HoseUtils.generateHoseSegments(start, end, p1, p2, dist);

        Vec3 prevUp = new Vec3(0, 1, 0);
        Vec3[] prevRing = new Vec3[RADIAL_SEGMENTS];
        Vec3[] currRing = new Vec3[RADIAL_SEGMENTS];

        for (int i = 0; i <= SEGMENTS; i++) {
            Vec3 center = centers[i];
            Vec3 tangent = (i < SEGMENTS ? centers[i + 1].subtract(center) : center.subtract(centers[i - 1])).normalize();

            Vec3 right = tangent.cross(prevUp).normalize();
            Vec3 up = right.cross(tangent).normalize();
            prevUp = up;

            for (int j = 0; j < RADIAL_SEGMENTS; j++) {
                double angle = 2 * Math.PI * j / RADIAL_SEGMENTS;
                currRing[j] = center.add(right.scale(Math.cos(angle) * RADIUS).add(up.scale(Math.sin(angle) * RADIUS)));
            }

            if (i > 0) {
                for (int j = 0; j < RADIAL_SEGMENTS; j++) {
                    int next = (j + 1) % RADIAL_SEGMENTS;
                    Vec3 a = prevRing[j];
                    Vec3 b = prevRing[next];
                    Vec3 c = currRing[next];
                    Vec3 d = currRing[j];

                    builder.vertex(d.x, d.y, d.z).color(15, 15, 15, 255).endVertex();
                    builder.vertex(c.x, c.y, c.z).color(15, 15, 15, 255).endVertex();
                    builder.vertex(b.x, b.y, b.z).color(15, 15, 15, 255).endVertex();
                    builder.vertex(a.x, a.y, a.z).color(15, 15, 15, 255).endVertex();
                }
            }

            Vec3[] temp = prevRing;
            prevRing = currRing;
            currRing = temp;
        }
    }
}
