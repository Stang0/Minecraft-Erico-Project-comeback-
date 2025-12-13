package com.example.examplemod.superman.geckolib;

import com.example.examplemod.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for Superman player animations
 */
@SuppressWarnings("deprecation")
public class SupermanPlayerModel extends GeoModel<SupermanGeckoPlayer> {

    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID,
            "geo/superman_player.geo.json");

    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID,
            "animations/superman_player.animation.json");

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft",
            "textures/entity/steve.png");

    @Override
    public ResourceLocation getModelResource(SupermanGeckoPlayer animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SupermanGeckoPlayer animatable) {
        // Use player's actual skin - this is overridden in the renderer
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SupermanGeckoPlayer animatable) {
        return ANIMATION;
    }
}
