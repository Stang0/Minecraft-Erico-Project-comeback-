package com.example.examplemod.superman.geckolib;

import com.example.examplemod.ExampleMod;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.GeckoLib;

/**
 * Handles GeckoLib initialization, layer registration, and hiding vanilla model
 */
public class SupermanGeckoLibInit {

    private static boolean initialized = false;

    public static void init() {
        if (!initialized) {
            GeckoLib.initialize();
            initialized = true;
            System.out.println("[Superman Mod] GeckoLib initialized for 1.20.1!");
        }
    }

    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
            // Add layer to default player renderer
            PlayerRenderer defaultRenderer = event.getSkin("default");
            if (defaultRenderer != null) {
                defaultRenderer.addLayer(new SupermanAnimationLayer(defaultRenderer));
            }

            // Add layer to slim player renderer
            PlayerRenderer slimRenderer = event.getSkin("slim");
            if (slimRenderer != null) {
                slimRenderer.addLayer(new SupermanAnimationLayer(slimRenderer));
            }
        }
    }

    /**
     * Handle rendering events to hide vanilla model when flying
     */
    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
    public static class ForgeEvents {

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void onRenderPlayerPre(RenderLivingEvent.Pre<?, ?> event) {
            if (event.getEntity() instanceof AbstractClientPlayer player) {
                if (SupermanAnimationLayer.isVisualFlying((AbstractClientPlayer) player)) {
                    // Hide vanilla model parts so only GeckoLib model shows
                    if (event.getRenderer().getModel() instanceof PlayerModel<?> playerModel) {
                        playerModel.setAllVisible(false);
                        // Make sure we don't hide the layer we just added though?
                        // Layers render independently, but they use the model for positioning
                        // sometimes.
                        // GeckoLib model is standalone so it's fine.
                    }
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.LOW)
        public static void onRenderPlayerPost(RenderLivingEvent.Post<?, ?> event) {
            if (event.getEntity() instanceof AbstractClientPlayer player) {
                // Restore visibility for next frame/other render passes
                if (event.getRenderer().getModel() instanceof PlayerModel<?> playerModel) {
                    playerModel.setAllVisible(true);
                    // Restore specific parts defaults if needed (jacket, sleeves etc)
                    playerModel.hat.visible = true;
                    playerModel.jacket.visible = true;
                    playerModel.leftPants.visible = true;
                    playerModel.rightPants.visible = true;
                    playerModel.leftSleeve.visible = true;
                    playerModel.rightSleeve.visible = true;
                }
            }
        }
    }
}
