package com.example.superheromod.client;

import com.example.superheromod.SupermanMod;
import com.example.superheromod.logic.FlightHandler;
import com.example.superheromod.util.FlightAnimator;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SupermanMod.MODID, value = Dist.CLIENT)
public class ClientTickHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // ทำงานตอนจบ Tick และเกมไม่ Pause
        if (event.phase == TickEvent.Phase.END && !Minecraft.getInstance().isPaused()) {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                // 1. อัปเดต State (Prev -> Current)
                boolean isFlying = FlightHandler.isFlying(player);
                FlightRenderState.tick(player, isFlying);

                // 2. อัปเดต Frame (Frame++)
                FlightAnimator.INSTANCE.tick();
            }
        }
    }
}