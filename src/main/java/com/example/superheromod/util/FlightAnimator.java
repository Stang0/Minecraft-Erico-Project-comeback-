package com.example.superheromod.util;

import com.example.superheromod.client.FlightRenderState;

public class FlightAnimator {
    public static final FlightAnimator INSTANCE = new FlightAnimator();

    public float prevFrame = 0, frame = 0;

    // Weights
    public float data0 = 0; // Idle weight
    public float hover = 0; // Hover weight
    public float data1 = 0; // Boost weight

    // Rotations & Positions
    public float headRotX, headRotY, headRotZ;
    public float bodyRotX, bodyRotY, bodyRotZ;
    public float rightArmRotX, rightArmRotY, rightArmRotZ, leftArmRotX, leftArmRotY, leftArmRotZ;
    public float rightLegRotX, rightLegRotY, rightLegRotZ, leftLegRotX, leftLegRotY, leftLegRotZ;
    public float rightArmPosX, rightArmPosY, rightArmPosZ, leftArmPosX, leftArmPosY, leftArmPosZ;
    public float rightLegPosX, rightLegPosY, rightLegPosZ, leftLegPosX, leftLegPosY, leftLegPosZ;

    // Inputs
    public float motX, motY, motZ, forward, strafe, vel;

    public void tick() {
        prevFrame = frame;
        frame += 1.0f;
    }

    public void updateAnimations(float partialTicks) {
        updateVel();

        this.forward = FlightRenderState.smoothForward;
        this.strafe = FlightRenderState.smoothStrafe;

        // 2. รับค่า Weight ที่คำนวณ delay มาแล้ว
        float smoothFly = FiskMath.curve(FlightRenderState.getRenderFlyWeight(partialTicks));
        float smoothBoost = FiskMath.curve(FlightRenderState.getRenderBoost(partialTicks));
        float smoothHover = FiskMath.curve(FlightRenderState.getRenderHover(partialTicks));

        this.hover = smoothHover * (1.0f - smoothBoost) * smoothFly;

        this.data0 = (1.0f - smoothHover) * (1.0f - smoothBoost) * smoothFly;

        this.data1 = smoothBoost * smoothFly;

        clearRotations();

        if (this.data0 > 0.001f) applyIdle(this.data0);
        if (this.hover > 0.001f) applyHover(this.hover);
        if (this.data1 > 0.001f) applySuperman(this.data1);
    }


    public void applyIdle(float weight) {
        headRotX += -8.5f * weight;
        headRotY += -10f * weight;

        // --- Right Arm ---
        rightArmRotX += -10f * weight;
        rightArmRotY += 10f * weight;
        rightArmRotZ += 6f * weight;

        // --- Left Arm ---
        leftArmRotX += 5f * weight;
        leftArmRotY += -20f * weight;
        leftArmRotZ += -10f * weight;

        // --- Right Leg ---
        rightLegRotX += 20f * weight;
        rightLegRotY += 5f * weight;
        rightLegPosX += -0.3f * weight;
        rightLegPosY += -1.0f * weight;
        rightLegPosZ += -2.5f * weight;

        // --- Left Leg ---
        leftLegRotX += 5f * weight;
        leftLegRotY += -8f * weight;
        leftLegPosX += 0.1f * weight;
        leftLegPosY += -0.5f * weight;
        leftLegPosZ += -1.0f * weight;
    }

    public void applyHover(float weight) {

        // Head
        headRotX -= (15f * clamp(forward, -1, 1)) * weight;
        headRotY -= (15f * strafe) * weight;


        rightArmRotX += (10f * forward + 10f * strafe) * weight;
        rightArmRotZ += (20f * strafe) * weight;
        rightArmRotZ += 10f * weight;

        leftArmRotX += (10f * forward - 10f * strafe) * weight;
        leftArmRotZ -= (20f * strafe) * weight;
        leftArmRotZ -= 10f * weight;

        // Legs (ลู่ลมตามทิศทาง)
        rightLegRotX += (40f * forward) * weight;
        rightLegRotZ += (20f * strafe) * weight;

        leftLegRotX += (40f * forward) * weight;
        leftLegRotZ += (20f * strafe) * weight;
    }

    // --- 3. SUPERMAN ---
    public void applySuperman(float w) {
        headRotX += -20f * w;

        rightArmRotX += -175f * w;
        rightArmRotY += 0f * w;
        rightArmRotZ += 0f * w;

        leftArmRotX += 15f * w;
        leftArmRotZ += -5f * w;

        rightLegRotX += 5f * w;
        leftLegRotX += 10f * w;

        rightLegPosZ += 2f * w;
        leftLegPosZ += 3f * w;
    }

    public void clearRotations() {
        headRotX = headRotY = headRotZ = 0;
        bodyRotX = bodyRotY = bodyRotZ = 0;
        rightArmRotX = rightArmRotY = rightArmRotZ = 0;
        leftArmRotX = leftArmRotY = leftArmRotZ = 0;
        rightLegRotX = rightLegRotY = rightLegRotZ = 0;
        leftLegRotX = leftLegRotY = leftLegRotZ = 0;
        rightArmPosX = rightArmPosY = rightArmPosZ = 0;
        leftArmPosX = leftArmPosY = leftArmPosZ = 0;
        rightLegPosX = rightLegPosY = rightLegPosZ = 0;
        leftLegPosX = leftLegPosY = leftLegPosZ = 0;
    }

    private float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(val, max));
    }
    public void updateVel() {
        vel = (float) Math.sqrt(motX * motX + motZ * motZ + (float)Math.pow(0.3 * motY, 2));
    }
}
