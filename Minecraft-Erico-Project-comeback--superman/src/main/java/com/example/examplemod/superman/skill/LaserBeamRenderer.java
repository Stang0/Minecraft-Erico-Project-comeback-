package com.example.examplemod.superman.skill;

import com.example.examplemod.ExampleMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * Renders the laser beam visual effect in the world
 * DISABLED - Using Effekseer effect from AAA Particles instead
 */
// @Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class LaserBeamRenderer {

    // Laser visual config
    private static final float BEAM_WIDTH = 0.15f;
    private static final float CORE_WIDTH = 0.05f;

    // Colors (RGBA 0-1 range)
    private static final float[] OUTER_COLOR = { 1.0f, 0.3f, 0.1f, 0.4f }; // Red-orange glow
    private static final float[] CORE_COLOR = { 1.0f, 0.9f, 0.5f, 0.9f }; // Yellow-white core

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        Player player = mc.player;
        LaserSkillHandler.LaserBeam beam = LaserSkillHandler.getActiveLaser(player);

        if (beam == null)
            return;

        // Get camera position for offset
        Vec3 cameraPos = event.getCamera().getPosition();

        // Render the beam
        renderLaserBeam(event.getPoseStack(), beam, cameraPos, event.getPartialTick());
    }

    /**
     * Render a laser beam from start to end position
     */
    private static void renderLaserBeam(PoseStack poseStack, LaserSkillHandler.LaserBeam beam,
            Vec3 cameraPos, float partialTick) {

        // Calculate fade based on remaining time
        float fade = Math.min(1.0f, beam.ticksRemaining / 5.0f); // Fade out in last 5 ticks

        // Offset positions by camera
        Vec3 start = beam.start.subtract(cameraPos);
        Vec3 end = beam.end.subtract(cameraPos);

        // Set up render state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        poseStack.pushPose();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        // Calculate beam direction and perpendicular vectors
        Vec3 direction = end.subtract(start).normalize();
        Vec3 up = new Vec3(0, 1, 0);

        // Handle edge case when looking straight up/down
        if (Math.abs(direction.y) > 0.99) {
            up = new Vec3(1, 0, 0);
        }

        Vec3 right = direction.cross(up).normalize();
        Vec3 perpUp = right.cross(direction).normalize();

        // Draw outer glow
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        drawBeamQuad(buffer, matrix, start, end, right, perpUp, BEAM_WIDTH,
                OUTER_COLOR[0], OUTER_COLOR[1], OUTER_COLOR[2], OUTER_COLOR[3] * fade);
        BufferUploader.drawWithShader(buffer.end());

        // Draw inner core
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        drawBeamQuad(buffer, matrix, start, end, right, perpUp, CORE_WIDTH,
                CORE_COLOR[0], CORE_COLOR[1], CORE_COLOR[2], CORE_COLOR[3] * fade);
        BufferUploader.drawWithShader(buffer.end());

        poseStack.popPose();

        // Restore render state
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Draw a quad representing the beam from multiple angles
     */
    private static void drawBeamQuad(BufferBuilder buffer, Matrix4f matrix,
            Vec3 start, Vec3 end, Vec3 right, Vec3 up, float width,
            float r, float g, float b, float a) {

        // Draw horizontal beam plane
        Vec3 s1 = start.add(right.scale(-width));
        Vec3 s2 = start.add(right.scale(width));
        Vec3 e1 = end.add(right.scale(-width));
        Vec3 e2 = end.add(right.scale(width));

        buffer.vertex(matrix, (float) s1.x, (float) s1.y, (float) s1.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) e1.x, (float) e1.y, (float) e1.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) e2.x, (float) e2.y, (float) e2.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) s2.x, (float) s2.y, (float) s2.z).color(r, g, b, a).endVertex();

        // Draw vertical beam plane
        s1 = start.add(up.scale(-width));
        s2 = start.add(up.scale(width));
        e1 = end.add(up.scale(-width));
        e2 = end.add(up.scale(width));

        buffer.vertex(matrix, (float) s1.x, (float) s1.y, (float) s1.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) e1.x, (float) e1.y, (float) e1.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) e2.x, (float) e2.y, (float) e2.z).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, (float) s2.x, (float) s2.y, (float) s2.z).color(r, g, b, a).endVertex();
    }
}
