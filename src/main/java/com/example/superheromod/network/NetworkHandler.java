package com.example.superheromod.network;

import com.example.superheromod.SupermanMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel INSTANCE;

    public static void register() {
        int id = 0;
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(SupermanMod.MODID, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        // ลงทะเบียน PacketToggleFlight ที่นี่
        INSTANCE.registerMessage(id++,
                PacketToggleFlight.class,
                PacketToggleFlight::encode,
                PacketToggleFlight::decode,
                PacketToggleFlight::handle
        );
    }
}