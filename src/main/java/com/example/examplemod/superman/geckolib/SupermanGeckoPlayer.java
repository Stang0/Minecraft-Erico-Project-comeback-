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
 * Implements a state machine for smooth animation transitions.
 */
public class SupermanGeckoPlayer implements GeoReplacedEntity {

    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    // Animation state enum
    public enum AnimState {
        STANDING, // Vanilla standing (no GeckoLib animation)
        BLEND_TO_FLY, // Transition: stand -> fly
        FLYING, // Flying animation loop
        BLEND_TO_DOGGY, // Transition: fly -> doggy
        DOGGY, // Doggy (sprint fly) animation loop
        BLEND_DOGGY_TO_FLY, // Transition: doggy -> fly
        BLEND_TO_STAND, // Transition: fly -> stand
        BLEND_DOGGY_TO_STAND // Transition: doggy -> stand
    }

    // Animation definitions
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("Fly");
    private static final RawAnimation DOGGY_ANIM = RawAnimation.begin().thenLoop("doggy");
    private static final RawAnimation BLEND_STAND_TO_FLY = RawAnimation.begin().thenPlay("blen_stand_to_fly");
    private static final RawAnimation BLEND_FLY_TO_STAND = RawAnimation.begin().thenPlay("blen_fly_to_stand");
    private static final RawAnimation BLEND_FLY_TO_DOGGY = RawAnimation.begin().thenPlay("blen_fly_to_doggy");
    private static final RawAnimation BLEND_DOGGY_TO_FLY = RawAnimation.begin().thenPlay("blen_doggy_to_fly");
    private static final RawAnimation BLEND_DOGGY_TO_STAND = RawAnimation.begin().thenPlay("blen_doggy_to_stand");

    private AbstractClientPlayer currentPlayer;

    // Per-player animation state tracking
    private static final Map<UUID, PlayerAnimState> playerStates = new HashMap<>();

    private static class PlayerAnimState {
        AnimState currentState = AnimState.STANDING;
        boolean wasFlying = false;
        boolean wasSprinting = false;
        boolean wasOnGround = true;
        long blendStartTime = 0;
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
        AnimationController<SupermanGeckoPlayer> controller = new AnimationController<>(
                this,
                "superman_flight",
                5, // Short transition for responsiveness
                this::flightPredicate);
        controllers.add(controller);
    }

    private PlayState flightPredicate(AnimationState<SupermanGeckoPlayer> state) {
        AbstractClientPlayer player = getCurrentPlayer();
        if (player == null) {
            return PlayState.STOP;
        }

        UUID playerId = player.getUUID();
        PlayerAnimState playerState = getPlayerState(playerId);
        AnimationController<?> controller = state.getController();

        // Current status
        boolean isFlying = SupermanFlightHandler.isFlying(player);
        boolean isSprinting = player.isSprinting();
        boolean isOnGround = player.onGround();

        // State machine logic
        switch (playerState.currentState) {
            case STANDING:
                if (isFlying && !isOnGround) {
                    // Start flying - play blend animation
                    playerState.currentState = AnimState.BLEND_TO_FLY;
                    controller.forceAnimationReset();
                    controller.setAnimation(BLEND_STAND_TO_FLY);
                    playerState.blendStartTime = System.currentTimeMillis();
                    return PlayState.CONTINUE;
                }
                // Stay in vanilla standing
                playerState.wasFlying = false;
                playerState.wasSprinting = false;
                return PlayState.STOP;

            case BLEND_TO_FLY:
                // Check if blend animation finished (approximately 1.3 seconds)
                if (System.currentTimeMillis() - playerState.blendStartTime > 1300) {
                    if (isSprinting && isFlying) {
                        playerState.currentState = AnimState.DOGGY;
                        controller.setAnimation(DOGGY_ANIM);
                    } else if (isFlying) {
                        playerState.currentState = AnimState.FLYING;
                        controller.setAnimation(FLY_ANIM);
                    } else {
                        // Cancelled flying during blend
                        playerState.currentState = AnimState.STANDING;
                        return PlayState.STOP;
                    }
                }
                return PlayState.CONTINUE;

            case FLYING:
                if (!isFlying || isOnGround) {
                    // Landing - play blend to stand
                    playerState.currentState = AnimState.BLEND_TO_STAND;
                    controller.forceAnimationReset();
                    controller.setAnimation(BLEND_FLY_TO_STAND);
                    playerState.blendStartTime = System.currentTimeMillis();
                    return PlayState.CONTINUE;
                }
                if (isSprinting) {
                    // Start sprinting - transition to doggy
                    playerState.currentState = AnimState.BLEND_TO_DOGGY;
                    controller.forceAnimationReset();
                    controller.setAnimation(BLEND_FLY_TO_DOGGY);
                    playerState.blendStartTime = System.currentTimeMillis();
                    return PlayState.CONTINUE;
                }
                // Continue flying
                return state.setAndContinue(FLY_ANIM);

            case BLEND_TO_DOGGY:
                // Check if blend animation finished
                if (System.currentTimeMillis() - playerState.blendStartTime > 1300) {
                    if (!isFlying || isOnGround) {
                        playerState.currentState = AnimState.BLEND_DOGGY_TO_STAND;
                        controller.forceAnimationReset();
                        controller.setAnimation(BLEND_DOGGY_TO_STAND);
                        playerState.blendStartTime = System.currentTimeMillis();
                    } else {
                        playerState.currentState = AnimState.DOGGY;
                        controller.setAnimation(DOGGY_ANIM);
                    }
                }
                return PlayState.CONTINUE;

            case DOGGY:
                if (!isFlying || isOnGround) {
                    // Landing from doggy - play blend to stand
                    playerState.currentState = AnimState.BLEND_DOGGY_TO_STAND;
                    controller.forceAnimationReset();
                    controller.setAnimation(BLEND_DOGGY_TO_STAND);
                    playerState.blendStartTime = System.currentTimeMillis();
                    return PlayState.CONTINUE;
                }
                if (!isSprinting) {
                    // Stop sprinting - transition back to flying
                    playerState.currentState = AnimState.BLEND_DOGGY_TO_FLY;
                    controller.forceAnimationReset();
                    controller.setAnimation(BLEND_DOGGY_TO_FLY);
                    playerState.blendStartTime = System.currentTimeMillis();
                    return PlayState.CONTINUE;
                }
                // Continue doggy
                return state.setAndContinue(DOGGY_ANIM);

            case BLEND_DOGGY_TO_FLY:
                // Check if blend animation finished
                if (System.currentTimeMillis() - playerState.blendStartTime > 1300) {
                    if (!isFlying || isOnGround) {
                        playerState.currentState = AnimState.BLEND_TO_STAND;
                        controller.forceAnimationReset();
                        controller.setAnimation(BLEND_FLY_TO_STAND);
                        playerState.blendStartTime = System.currentTimeMillis();
                    } else if (isSprinting) {
                        // Started sprinting again
                        playerState.currentState = AnimState.DOGGY;
                        controller.setAnimation(DOGGY_ANIM);
                    } else {
                        playerState.currentState = AnimState.FLYING;
                        controller.setAnimation(FLY_ANIM);
                    }
                }
                return PlayState.CONTINUE;

            case BLEND_TO_STAND:
            case BLEND_DOGGY_TO_STAND:
                // Check if blend animation finished
                if (System.currentTimeMillis() - playerState.blendStartTime > 1300) {
                    playerState.currentState = AnimState.STANDING;
                    return PlayState.STOP;
                }
                // If player starts flying again during blend
                if (isFlying && !isOnGround) {
                    playerState.currentState = AnimState.BLEND_TO_FLY;
                    controller.forceAnimationReset();
                    controller.setAnimation(BLEND_STAND_TO_FLY);
                    playerState.blendStartTime = System.currentTimeMillis();
                }
                return PlayState.CONTINUE;
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

    // Get current animation state for a player (for render layer to check)
    public static AnimState getPlayerAnimState(UUID playerId) {
        PlayerAnimState state = playerStates.get(playerId);
        return state != null ? state.currentState : AnimState.STANDING;
    }
}
