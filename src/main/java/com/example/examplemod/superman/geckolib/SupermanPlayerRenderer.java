package com.example.examplemod.superman.geckolib;

import com.example.examplemod.superman.SupermanFlightHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

/**
 * GeckoLib renderer for Superman player animation
 * Uses player's actual skin texture
 */
public class SupermanPlayerRenderer extends GeoReplacedEntityRenderer<AbstractClientPlayer, SupermanGeckoPlayer> {

    // Singleton-like static animatable to share state
    private static final SupermanGeckoPlayer staticAnimatable = new SupermanGeckoPlayer();

    // Store current player for texture resolution
    private AbstractClientPlayer currentPlayer;

    public SupermanPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new SupermanPlayerModel(), staticAnimatable);
    }

    public static SupermanGeckoPlayer getStaticAnimatable() {
        return staticAnimatable;
    }

    /**
     * Override to use the player's skin texture
     */
    @Override
    public ResourceLocation getTextureLocation(SupermanGeckoPlayer animatable) {
        if (currentPlayer != null) {
            return currentPlayer.getSkinTextureLocation();
        }
        return super.getTextureLocation(animatable);
    }

    /**
     * Render a player with GeckoLib animation
     */
    /**
     * Prevent GeckoLib from re-applying rotations, since the RenderLayer already
     * provides a rotated PoseStack
     */
    @Override
    public void applyRotations(SupermanGeckoPlayer animatable, PoseStack poseStack, float ageInTicks, float rotationYaw,
            float partialTick) {
        // Do nothing! The PoseStack is already rotated by the vanilla PlayerRenderer
    }

    public void renderAnimatedPlayer(AbstractClientPlayer player, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, float partialTick) {

        this.currentPlayer = player;
        staticAnimatable.setCurrentPlayer(player);

        // Required to sync the model with the entity (even if static)
        // We use player.getId() to give unique animation states per player
        staticAnimatable.getAnimatableInstanceCache().getManagerForId(player.getId());

        // Set the entity so GeckoLib doesn't crash
        this.currentEntity = player;

        poseStack.pushPose();

        // Get model
        BakedGeoModel model = getGeoModel().getBakedModel(getGeoModel().getModelResource(staticAnimatable));

        // Use player's skin
        ResourceLocation texture = player.getSkinTextureLocation();
        RenderType renderType = RenderType.entityTranslucent(texture);
        VertexConsumer buffer = bufferSource.getBuffer(renderType);

        // This triggers the full GeckoLib render pipeline
        this.actuallyRender(poseStack, staticAnimatable, model, renderType, bufferSource, buffer,
                false, partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);

        poseStack.popPose();

        this.currentPlayer = null;
    }
}
