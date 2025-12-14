package com.example.examplemod.superman.geckolib;

import com.example.examplemod.superman.SupermanFlightHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Render layer that triggers GeckoLib rendering during flight.
 * Handles smooth visual transitions both when starting and stopping flight.
 */
@OnlyIn(Dist.CLIENT)
public class SupermanAnimationLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private SupermanPlayerRenderer renderer;
    
    // Visual state tracking per player
    private static final Map<UUID, VisualState> visualStates = new HashMap<>();
    
    // Must match the transitionLength in SupermanGeckoPlayer's AnimationController
    private static final int TRANSITION_TICKS = 10;

    private static class VisualState {
        boolean wasFlying = false;
        int transitionTimer = 0;
        boolean inTransitionOut = false;
    }

    public SupermanAnimationLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    /**
     * Check if player should have GeckoLib model visible (flying or transitioning)
     */
    public static boolean isVisualFlying(AbstractClientPlayer player) {
        UUID uuid = player.getUUID();
        VisualState state = visualStates.get(uuid);
        
        if (SupermanFlightHandler.isFlying(player)) {
            return true;
        }
        
        return state != null && state.transitionTimer > 0;
    }

    private VisualState getVisualState(UUID uuid) {
        return visualStates.computeIfAbsent(uuid, k -> new VisualState());
    }

    private SupermanPlayerRenderer getRenderer() {
        if (renderer == null) {
            Minecraft mc = Minecraft.getInstance();
            EntityRendererProvider.Context ctx = new EntityRendererProvider.Context(
                    mc.getEntityRenderDispatcher(),
                    mc.getItemRenderer(),
                    mc.getBlockRenderer(),
                    mc.gameRenderer.itemInHandRenderer,
                    mc.getResourceManager(),
                    mc.getEntityModels(),
                    mc.font);
            renderer = new SupermanPlayerRenderer(ctx);
        }
        return renderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light,
            AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        UUID uuid = player.getUUID();
        VisualState state = getVisualState(uuid);
        boolean isFlying = SupermanFlightHandler.isFlying(player);
        
        boolean shouldRender = false;
        
        if (isFlying) {
            shouldRender = true;
            state.transitionTimer = TRANSITION_TICKS;
            state.inTransitionOut = false;
            
        } else if (state.wasFlying && !isFlying) {
            state.inTransitionOut = true;
            state.transitionTimer = TRANSITION_TICKS;
            shouldRender = true;
            
        } else if (state.inTransitionOut && state.transitionTimer > 0) {
            state.transitionTimer--;
            shouldRender = true;
            
            if (state.transitionTimer <= 0) {
                state.inTransitionOut = false;
            }
        }
        
        state.wasFlying = isFlying;
        
        if (shouldRender) {
            getRenderer().renderAnimatedPlayer(player, poseStack, buffer, light, partialTicks);
        }
    }
    
    public static void clearPlayerState(UUID playerId) {
        visualStates.remove(playerId);
    }
}
