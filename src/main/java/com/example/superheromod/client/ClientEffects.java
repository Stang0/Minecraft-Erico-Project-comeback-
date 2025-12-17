package com.example.superheromod.client;

import com.example.superheromod.SupermanMod;
import com.example.superheromod.logic.FlightHandler;
// ต้องมี Enum FlightState ใน Logic หรือใช้ boolean เช็คเอาก็ได้
// สมมติว่า FlightHandler มี method check state
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SupermanMod.MODID, value = Dist.CLIENT)
public class ClientEffects {

    // ตัวแปรจำสถานะเก่า (เพื่อเช็คจังหวะเปลี่ยน)
    private static boolean wasBoosting = false;
    private static boolean wasBraking = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p == null) return;

        // เช็คว่าบินอยู่ไหม
        boolean isFlying = FlightHandler.isFlying(p);
        if (!isFlying) {
            wasBoosting = false;
            wasBraking = false;
            return;
        }

        // ดึงสถานะปัจจุบัน (สมมติว่าเช็คจากความเร็วหรือปุ่มกด)
        Vec3 motion = p.getDeltaMovement();
        double speed = motion.horizontalDistance();

        // Logic จำลอง State (ถ้าคุณมี Enum FlightState ให้ใช้ Enum แทนนะครับ)
        boolean isBoosting = speed > 0.5 && p.isSprinting();
        boolean isBraking = mc.options.keyShift.isDown() && speed > 0.1;

        // -----------------------
        // 💥 SONIC BOOM (เมื่อเริ่ม Boost)
        // -----------------------
        if (isBoosting && !wasBoosting) {
            // 1. เสียงระเบิดทุ้มๆ (Pitch ต่ำๆ = เสียงใหญ่)
            p.level().playSound(p, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0f, 0.5f);

            // 2. Particle วงแหวน (Sonic Ring)
            spawnSonicRing(p);

            // 3. ระเบิดควันใหญ่ตรงกลาง
            p.level().addParticle(ParticleTypes.EXPLOSION_EMITTER,
                    p.getX(), p.getY() + 1, p.getZ(), 0, 0, 0);
        }

        // -----------------------
        // 💨 AIR BRAKE (เมื่อเริ่มเบรก)
        // -----------------------
        if (isBraking && !wasBraking) {
            // 1. เสียงลมกระแทก (ใช้เสียง Elytra หรือ Fire Extinguish)
            p.level().playSound(p, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0f, 0.8f);

            // 2. Particle ควันฟุ้งไปข้างหน้า (ตามแรงเฉื่อย)
            for (int i = 0; i < 20; i++) {
                double rX = (Math.random() - 0.5) * 2;
                double rY = (Math.random() - 0.5) * 2;
                double rZ = (Math.random() - 0.5) * 2;
                p.level().addParticle(ParticleTypes.CLOUD,
                        p.getX() + rX, p.getY() + rY, p.getZ() + rZ,
                        motion.x, motion.y, motion.z); // ควันพุ่งไปตามทิศที่เคยบิน
            }
        }

        // อัปเดตสถานะเก่า
        wasBoosting = isBoosting;
        wasBraking = isBraking;
    }

    // ฟังก์ชันสร้างวงแหวนควัน (Sonic Ring Math)
    private static void spawnSonicRing(Player p) {
        Vec3 look = p.getLookAngle();
        // หาทิศทางขวางลำตัว (Right Vector)
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
        // หาทิศทางขึ้น (Up Vector) โดย Cross Product
        Vec3 up = look.cross(right).normalize();

        int particleCount = 40; // จำนวนเม็ดฝุ่นในวงแหวน
        double radius = 2.5;    // รัศมีวงแหวน

        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount;

            // คำนวณตำแหน่งจุดในวงกลม โดยอิงตามแกน Right และ Up (เพื่อให้วงแหวนตั้งฉากกับตัวเรา)
            double offsetX = Math.cos(angle) * radius;
            double offsetY = Math.sin(angle) * radius;

            Vec3 pos = p.position().add(0, 1, 0) // เริ่มที่กลางตัว
                    .add(right.scale(offsetX))   // ขยับซ้ายขวา
                    .add(up.scale(offsetY))      // ขยับขึ้นลง
                    .add(look.scale(-1.0));      // ถอยหลังไปนิดนึง (ให้อยู่หลังตัว)

            // สร้าง Particle
            p.level().addParticle(ParticleTypes.CLOUD,
                    pos.x, pos.y, pos.z,
                    look.x * -0.5, look.y * -0.5, look.z * -0.5); // ให้ควันพุ่งสวนทางกลับไป
        }
    }
}