package com.example.examplemod.superman.geckolib;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.superman.SupermanFlightHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * GeckoLib-powered player animation handler
 * Uses animation.superman.fly from the animation.json file
 */
@SuppressWarnings("deprecation")
public class SupermanGeckoPlayer implements GeoReplacedEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Animation name - must match exactly what's in superman_player.animation.json
    public static final String ANIM_FLY = "animation.superman.fly";

    // Store reference to current player being animated
    private AbstractClientPlayer currentPlayer;

    public void setCurrentPlayer(AbstractClientPlayer player) {
        this.currentPlayer = player;
    }

    public AbstractClientPlayer getCurrentPlayer() {
        return this.currentPlayer;
    }

    @Override
    public EntityType<?> getReplacingEntityType() {
        return EntityType.PLAYER;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Main flight controller - transition time of 20 ticks (1 second) for smooth
        // blend
        controllers.add(new AnimationController<>(this, "superman_flight", 20, this::flightPredicate));
    }

    /**
     * Determines which animation to play based on player state
     */
    private PlayState flightPredicate(AnimationState<SupermanGeckoPlayer> state) {
        AbstractClientPlayer player = getCurrentPlayer();

        if (player == null) {
            return PlayState.STOP;
        }

        boolean isFlying = SupermanFlightHandler.isFlying(player);

        if (!isFlying) {
            return PlayState.STOP;
        }

        // Play the fly animation (loops as defined in the JSON)
        state.getController().setAnimation(RawAnimation.begin().thenLoop(ANIM_FLY));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
