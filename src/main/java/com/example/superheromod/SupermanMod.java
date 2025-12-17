package com.example.superheromod;

import com.example.superheromod.client.ClientCameraHandler;
import com.example.superheromod.client.ClientInputHandler;
import com.example.superheromod.logic.FlightHandler;
import com.example.superheromod.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SupermanMod.MODID)
public class SupermanMod {
    public static final String MODID = "supermanmod";

    public static final Logger LOGGER = LogManager.getLogger();
    public SupermanMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        // ลงทะเบียนระบบต่างๆ
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(FlightHandler.class);      // ฟิสิกส์ (Server/Common)
        MinecraftForge.EVENT_BUS.register(ClientInputHandler.class); // ปุ่มกด (Client)
        MinecraftForge.EVENT_BUS.register(ClientCameraHandler.class);// กล้องเอียง (Client)
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.register(); // ลงทะเบียน Network Packet
    }
}