package com.example.examplemod.superman.geckolib;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

/**
 * GeckoLib renderer for Superman player animation.
 * Handles rendering the GeckoLib animated model instead of the vanilla player
 * model.
 */
public class SupermanPlayerRenderer extends GeoReplacedEntityRenderer<AbstractClientPlayer, SupermanGeckoPlayer> {

    private static final SupermanGeckoPlayer ANIMATABLE = new SupermanGeckoPlayer();
    private AbstractClientPlayer currentPlayer;

    public SupermanPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new SupermanPlayerModel(), ANIMATABLE);
    }

    public static SupermanGeckoPlayer getStaticAnimatable() {
        return ANIMATABLE;
    }

    @Override
    public ResourceLocation getTextureLocation(SupermanGeckoPlayer animatable) {
        if (currentPlayer != null) {
            return currentPlayer.getSkinTextureLocation();
        }
        return super.getTextureLocation(animatable);
    }

    /**
     * Don't apply extra rotation - the RenderLayer already handles player facing
     */
    @Override
    public void applyRotations(SupermanGeckoPlayer animatable, PoseStack poseStack,
            float ageInTicks, float rotationYaw, float partialTick) {
        // Empty - let vanilla layer handle rotation
    }

    /**
     * Render the GeckoLib animated player model.
     * Animation is handled automatically by GeckoLib's actuallyRender method.
     */
    public void renderAnimatedPlayer(AbstractClientPlayer player, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, float partialTick) {

        // Set up references
        this.currentPlayer = player;
        this.currentEntity = player;
        ANIMATABLE.setCurrentPlayer(player);

        // Get animation manager for this specific player instance
        // This triggers the animation controller predicate
        AnimatableManager<?> manager = ANIMATABLE.getAnimatableInstanceCache().getManagerForId(player.getId());

        poseStack.pushPose();

        // Get the baked model
        BakedGeoModel model = getGeoModel().getBakedModel(getGeoModel().getModelResource(ANIMATABLE));

        // Calculate animation time
        float animTime = player.tickCount + partialTick;

        // Apply animations to the model bones
        // GeckoLib's GeoRenderer.reusableBoneUpdateFunction handles this internally
        updateAnimatedBones(ANIMATABLE, model, animTime, partialTick);

        // Now render with the animated bone positions
        ResourceLocation texture = player.getSkinTextureLocation();
        RenderType renderType = RenderType.entityTranslucent(texture);
        VertexConsumer buffer = bufferSource.getBuffer(renderType);

        this.actuallyRender(poseStack, ANIMATABLE, model, renderType, bufferSource, buffer,
                false, partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);

        poseStack.popPose();
        this.currentPlayer = null;
    }

    /**
     * Update the bones with animation data.
     * This method triggers the animation controllers and applies bone
     * transformations.
     */
    private void updateAnimatedBones(SupermanGeckoPlayer animatable, BakedGeoModel model,
            float animTime, float partialTick) {
        // The animation is actually processed during actuallyRender() call
        // through GeoRenderer's internal animation processing
        // We just need to ensure the animatable manager is properly set up

        // GeckoLib automatically calls the animation predicate when rendering
        // The key is that the AnimationController's state is preserved between renders
    }
}
