package com.example.examplemod.superman.geckolib;

import com.example.examplemod.superman.SupermanFlightHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Render layer that renders the GeckoLib model for flying players.
 * Handles smooth transitions by continuing to render during blend-out period.
 */
@OnlyIn(Dist.CLIENT)
public class SupermanAnimationLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private SupermanPlayerRenderer renderer;

    // Timer tracks visual state: > 0 means we should render GeckoLib model
    private static final Map<UUID, Integer> transitionTimers = new HashMap<>();
    private static final int TRANSITION_DURATION = 20; // 1 second (matches controller)

    public SupermanAnimationLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    /**
     * Check if a player is currently in the visual transition period (flying OR
     * blending out)
     */
    public static boolean isVisualFlying(AbstractClientPlayer player) {
        return SupermanFlightHandler.isFlying(player) ||
                transitionTimers.getOrDefault(player.getUUID(), 0) > 0;
    }

    private SupermanPlayerRenderer getRenderer() {
        if (renderer == null) {
            Minecraft mc = Minecraft.getInstance();
            EntityRendererProvider.Context context = new EntityRendererProvider.Context(
                    mc.getEntityRenderDispatcher(),
                    mc.getItemRenderer(),
                    mc.getBlockRenderer(),
                    mc.gameRenderer.itemInHandRenderer,
                    mc.getResourceManager(),
                    mc.getEntityModels(),
                    mc.font);
            renderer = new SupermanPlayerRenderer(context);
        }
        return renderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        boolean isFlying = SupermanFlightHandler.isFlying(player);
        UUID uuid = player.getUUID();
        int timer = transitionTimers.getOrDefault(uuid, 0);

        if (isFlying) {
            // Flying: Keep timer at max
            timer = TRANSITION_DURATION;
        } else if (timer > 0) {
            // Not flying but still blending out: Decrease timer
            timer--;
        }

        // Save timer state
        transitionTimers.put(uuid, timer);

        // Render if timer > 0 (flying or blending out)
        // The GeckoLib controller handles the actual animation state based on isFlying
        // - When isFlying=true: Controller plays fly animation
        // - When isFlying=false: Controller returns STOP, which triggers blend-out
        // We just need to KEEP RENDERING during the blend-out period so the user can
        // SEE it
        if (timer > 0) {
            getRenderer().renderAnimatedPlayer(player, poseStack, buffer, packedLight, partialTicks);
        }
        // No reset logic needed - GeckoLib handles transitions internally
    }
}
