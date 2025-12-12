package com.example.themodforerico;

import com.example.themodforerico.handler.PacketHandler;
import com.example.themodforerico.client.ClientEvents;
import com.example.themodforerico.server.ServerEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TheModForErico.MODID)
public class TheModForErico {
    public static final String MODID = "themodforerico";

    public TheModForErico() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // ลงทะเบียน Setup (Networking)
        modEventBus.addListener(this::commonSetup);

        // ลงทะเบียน Event Bus หลัก
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Init Networking
        PacketHandler.register();
    }
}