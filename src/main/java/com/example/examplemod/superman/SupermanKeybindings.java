package com.example.examplemod.superman;

import com.example.examplemod.ExampleMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;


@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SupermanKeybindings {

    public static final KeyMapping TOGGLE_FLIGHT_KEY = new KeyMapping(
            "key.examplemod.toggle_flight",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G, 
            "key.categories.examplemod");

    public static final KeyMapping SPEED_BOOST_KEY = new KeyMapping(
            "key.examplemod.speed_boost",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R, 
            "key.categories.examplemod");


    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_FLIGHT_KEY);
        event.register(SPEED_BOOST_KEY);
    }


    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
    public static class KeyInputHandler {

        private static boolean wasTogglePressed = false;
        private static boolean wasBoostPressed = false;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END)
                return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null)
                return;

            // Handle toggle flight key
            if (TOGGLE_FLIGHT_KEY.isDown()) {
                if (!wasTogglePressed) {
                    wasTogglePressed = true;
                    SupermanFlightHandler.toggleFlight(mc.player);
                }
            } else {
                wasTogglePressed = false;
            }

            // Handle speed boost key
            if (SPEED_BOOST_KEY.isDown() && SupermanFlightHandler.isFlying(mc.player)) {
                // Apply speed boost
                if (!wasBoostPressed) {
                    wasBoostPressed = true;
                    // Give initial boost
                    var look = mc.player.getLookAngle();
                    mc.player.setDeltaMovement(
                            mc.player.getDeltaMovement().add(
                                    look.x * 0.5,
                                    look.y * 0.5,
                                    look.z * 0.5));
                }
            } else {
                wasBoostPressed = false;
            }

            // Check and display flight status
            SupermanAbilityHandler.ClientEvents.checkFlightStatus();
        }
    }
}
