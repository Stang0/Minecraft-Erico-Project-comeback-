package com.example.examplemod.superman.geckolib;

import com.example.examplemod.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * GeckoLib model for Superman player animation
 */
public class SupermanPlayerModel extends GeoModel<SupermanGeckoPlayer> {

    private static final ResourceLocation MODEL = new ResourceLocation(ExampleMod.MODID,
            "geo/superman_player.geo.json");
    private static final ResourceLocation ANIMATION = new ResourceLocation(ExampleMod.MODID,
            "animations/superman_player.animation.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/entity/steve.png");

    @Override
    public ResourceLocation getModelResource(SupermanGeckoPlayer animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(SupermanGeckoPlayer animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(SupermanGeckoPlayer animatable) {
        return ANIMATION;
    }
}
