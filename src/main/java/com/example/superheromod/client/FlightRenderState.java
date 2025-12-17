package com.example.superheromod.client;

import com.example.superheromod.util.FiskMath;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public class FlightRenderState {

    public static float boost = 0f, prevBoost = 0f;
    public static float flyWeight = 0f, prevFlyWeight = 0f;
    public static float yawDelta = 0f, prevYawDelta = 0f;
    private static float lastBodyYaw = 0f;

    // --- ⭐ เพิ่มตัวแปรใหม่สำหรับแก้ปัญหา ⭐ ---
    // 1. ตัวเกลี่ยปุ่มกด (แก้ A/D วาร์ป)
    public static float smoothForward = 0f;
    public static float smoothStrafe = 0f;

    // 2. ตัวหน่วงเวลา (แก้ Hover -> Idle เร็วไป)
    public static int hoverTimer = 0;
    public static float hoverWeight = 0f, prevHoverWeight = 0f; // แยก Weight ของ Hover ออกมาต่างหาก

    public static void tick(Player player, boolean isFlying) {
        // Save Previous
        prevBoost = boost;
        prevFlyWeight = flyWeight;
        prevYawDelta = yawDelta;
        prevHoverWeight = hoverWeight;

        if (!isFlying) {
            flyWeight = moveTowards(flyWeight, 0f, 0.1f);
            boost = moveTowards(boost, 0f, 0.1f);
            hoverWeight = moveTowards(hoverWeight, 0f, 0.1f);
            smoothForward = 0;
            smoothStrafe = 0;
            return;
        }

        // --- 1. จัดการ Inputs (แก้ A/D วาร์ป) ---
        float rawForward = 0f;
        float rawStrafe = 0f;

        if (player instanceof LocalPlayer localPlayer) {
            rawForward = localPlayer.input.forwardImpulse;
            rawStrafe = localPlayer.input.leftImpulse;
        }

        // เกลี่ยค่าปุ่มกด (0 -> 1 จะค่อยๆ ไหล) **สำคัญมาก**
        smoothForward = FiskMath.interpolate(smoothForward, rawForward, 0.15f);
        smoothStrafe = FiskMath.interpolate(smoothStrafe, rawStrafe, 0.15f);

        // --- 2. จัดการ Delay (Hover > Idle) ---
        boolean isMoving = (Math.abs(rawForward) > 0 || Math.abs(rawStrafe) > 0);

        if (isMoving) {
            hoverTimer = 40; // ตั้งเวลาหน่วงไว้ 40 ticks (2 วินาที) ถ้ามีการขยับ
        } else if (hoverTimer > 0) {
            hoverTimer--; // นับถอยหลังเมื่อหยุดเดิน
        }

        // ถ้ามีการกดเดิน หรือ เวลายังไม่หมด ให้ถือว่าเป็น Hover Mode
        float targetHover = (isMoving || hoverTimer > 0) ? 1.0f : 0.0f;

        // ถ้ากด Boost ท่า Hover ต้องหายไป
        if (player.isSprinting()) targetHover = 0.0f;

        // ค่อยๆ เปลี่ยนค่า Hover
        hoverWeight = moveTowards(hoverWeight, targetHover, 0.1f);

        // --- 3. จัดการค่าอื่นๆ ---
        flyWeight = moveTowards(flyWeight, 1.0f, 0.1f);
        boost = moveTowards(boost, player.isSprinting() ? 1.0f : 0.0f, 0.1f);

        // Yaw Delta Calculation
        float currentYaw = player.yBodyRot;
        float diff = currentYaw - lastBodyYaw;
        while (diff < -180.0F) diff += 360.0F;
        while (diff >= 180.0F) diff -= 360.0F;
        if (Math.abs(diff) < 0.1f) diff = 0f;
        yawDelta = FiskMath.interpolate(yawDelta, diff, 0.3f);
        lastBodyYaw = currentYaw;
    }

    // Render Helpers
    public static float getRenderFlyWeight(float partialTicks) { return FiskMath.interpolate(prevFlyWeight, flyWeight, partialTicks); }
    public static float getRenderBoost(float partialTicks) { return FiskMath.interpolate(prevBoost, boost, partialTicks); }
    public static float getRenderHover(float partialTicks) { return FiskMath.interpolate(prevHoverWeight, hoverWeight, partialTicks); }
    public static float getRenderYawDelta(float partialTicks) { return FiskMath.interpolate(prevYawDelta, yawDelta, partialTicks); }

    private static float moveTowards(float current, float target, float step) {
        if (Math.abs(target - current) <= step) return target;
        return current + Math.signum(target - current) * step;
    }
}