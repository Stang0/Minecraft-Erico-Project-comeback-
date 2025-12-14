package com.example.examplemod.superman.geckolib;

import com.example.examplemod.superman.SupermanFlightHandler;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GeckoLib animatable for Superman player animation.
 * Uses per-player animation state tracking to ensure smooth transitions work every time.
 */
public class SupermanGeckoPlayer implements GeoReplacedEntity {

    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    
    // IMPORTANT: Cache RawAnimation instances to avoid recreation every frame
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("animation.superman.fly");
    
    private AbstractClientPlayer currentPlayer;

    // Track animation state per player for proper transition handling
    private static final Map<UUID, PlayerAnimState> playerStates = new HashMap<>();

    private static class PlayerAnimState {
        boolean wasFlying = false;
        boolean animationStarted = false;
    }

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

    private PlayerAnimState getPlayerState(UUID playerId) {
        return playerStates.computeIfAbsent(playerId, k -> new PlayerAnimState());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Create animation controller with transition time for smooth blending
        // 10 ticks = 0.5 seconds transition between poses
        AnimationController<SupermanGeckoPlayer> controller = new AnimationController<>(
            this, 
            "superman_flight", 
            10,  // Transition length in ticks
            this::flightPredicate
        );
        controllers.add(controller);
    }

    private PlayState flightPredicate(AnimationState<SupermanGeckoPlayer> state) {
        AbstractClientPlayer player = getCurrentPlayer();
        if (player == null) {
            return PlayState.STOP;
        }

        UUID playerId = player.getUUID();
        PlayerAnimState playerState = getPlayerState(playerId);
        boolean isFlying = SupermanFlightHandler.isFlying(player);
        
        AnimationController<?> controller = state.getController();

        if (isFlying) {
            // Just started flying - trigger transition
            if (!playerState.wasFlying) {
                controller.forceAnimationReset();
                playerState.animationStarted = false;
            }
            
            // Set animation only once per flight session
            if (!playerState.animationStarted) {
                controller.setAnimation(FLY_ANIM);
                playerState.animationStarted = true;
            }
            
            playerState.wasFlying = true;
            return PlayState.CONTINUE;
        }

        // Stopped flying
        if (playerState.wasFlying) {
            playerState.wasFlying = false;
            playerState.animationStarted = false;
            controller.forceAnimationReset();
        }

        return PlayState.STOP;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    
    public static void clearPlayerState(UUID playerId) {
        playerStates.remove(playerId);
    }
}
