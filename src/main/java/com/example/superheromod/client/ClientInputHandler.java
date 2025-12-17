package com.example.superheromod.client;

import com.example.superheromod.SupermanMod;
import com.example.superheromod.logic.FlightHandler;
import com.example.superheromod.network.NetworkHandler;
import com.example.superheromod.network.PacketToggleFlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SupermanMod.MODID, value = Dist.CLIENT)
public class ClientInputHandler {
    private static boolean lastSpace = false;
    private static int jumpTimer = 0;

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        boolean space = Minecraft.getInstance().options.keyJump.isDown();
        boolean shift = Minecraft.getInstance().options.keyShift.isDown();

        // Ctrl คือปุ่ม Sprint ใน Minecraft
        boolean ctrl = Minecraft.getInstance().options.keySprint.isDown();

        if (jumpTimer > 0) jumpTimer--;

        // --- 1. DOUBLE JUMP (เปิด/ปิดการบิน) ---
        if (space && !lastSpace) {
            if (jumpTimer > 0) {
                boolean newState = !FlightHandler.isFlying(player);

                // ส่งข้อมูลไป Server
                FlightHandler.setFlying(player, newState);
                NetworkHandler.INSTANCE.sendToServer(new PacketToggleFlight(newState));

                if (newState) {
                    // เริ่มบิน: ลอยขึ้นนิดเดียว (Hover Start)
                    Vec3 m = player.getDeltaMovement();
                    player.setDeltaMovement(m.x, 0.2, m.z);
                }
                jumpTimer = 0;
            } else {
                if (!player.onGround()) jumpTimer = 7;
            }
        }

        // --- 2. การควบคุมขณะบิน ---
        if (FlightHandler.isFlying(player)) {
            Vec3 m = player.getDeltaMovement();

            // ขึ้น / ลง
            double verticalSpeed = 0;
            if (space) verticalSpeed = 0.4;      // กด Space ขึ้น
            else if (shift) verticalSpeed = -0.4; // กด Shift ลง
            else verticalSpeed = 0;               // ไม่กด = ลอยนิ่งแกน Y

            // ถ้ากด Ctrl (Sprint) ให้พุ่งไปข้างหน้าแรงๆ
            if (ctrl) {
                player.setSprinting(true); // บังคับสถานะ Sprint เพื่อให้ Logic อื่นรู้ว่า Boost อยู่
            }

            // รวมความเร็วแกน Y เข้าไป (แกน X, Z จัดการใน FlightHandler)
            // เราใช้ m.y * 0.8 เพื่อให้มีความเฉื่อย (Smooth Stop)
            if (!space && !shift) {
                player.setDeltaMovement(m.x, m.y * 0.8, m.z);
            } else {
                player.setDeltaMovement(m.x, verticalSpeed, m.z);
            }
        }

        lastSpace = space;
    }
}