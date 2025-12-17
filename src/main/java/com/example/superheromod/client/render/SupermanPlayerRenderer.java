package com.example.superheromod.client.render;

import com.example.superheromod.client.model.SupermanPlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;

public class SupermanPlayerRenderer extends PlayerRenderer {

    public SupermanPlayerRenderer(EntityRendererProvider.Context ctx, boolean slim) {
        super(ctx, slim);
        this.model = new SupermanPlayerModel<>(
                ctx.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER),
                slim
        );
    }
}

