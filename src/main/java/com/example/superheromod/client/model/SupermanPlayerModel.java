package com.example.superheromod.client.model;

import com.example.superheromod.client.FlightRenderState;
import com.example.superheromod.util.FiskMath;
import com.example.superheromod.util.FlightAnimator;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class SupermanPlayerModel<T extends Player> extends PlayerModel<T> {

    public SupermanPlayerModel(ModelPart root, boolean slim) {
        super(root, slim);
    }

    @Override
    public void setupAnim(T player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float partialTick = net.minecraft.client.Minecraft.getInstance().getPartialTick();

        // 1. คำนวณ smoothFly (0.0 = เดิน, 1.0 = บิน)
        float smoothFly = FiskMath.curve(FlightRenderState.getRenderFlyWeight(partialTick));

        // 2. คำนวณท่าเดิน Vanilla ไว้ก่อนเสมอ
        super.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (smoothFly > 0) {
            FlightAnimator anim = FlightAnimator.INSTANCE;

            // ส่ง Inputs
            anim.forward = FlightRenderState.smoothForward;
            anim.strafe = FlightRenderState.smoothStrafe;
            anim.motX = (float) player.getDeltaMovement().x;
            anim.motY = (float) player.getDeltaMovement().y;
            anim.motZ = (float) player.getDeltaMovement().z;

            // คำนวณท่า Mod
            anim.updateAnimations(partialTick);

            // ผสมท่า Mod ทับลงไปตามน้ำหนัก smoothFly
            applyBoneTransforms(anim, (float) Math.PI / 180F, smoothFly);

            syncLayer2();
        } else {
            // ถ้า smoothFly เป็น 0 สนิท ให้รีเซ็ตตำแหน่งกลับเป็น Vanilla
            resetBodyPositions();
        }
    }

    private void applyBoneTransforms(FlightAnimator anim, float RAD, float weight) {
        // ใช้ interpolate เพื่อผสมท่า Vanilla (ค่าปัจจุบัน) กับท่า Mod (anim.*)
        this.head.xRot = FiskMath.interpolate(this.head.xRot, anim.headRotX * RAD, weight);
        this.head.yRot = FiskMath.interpolate(this.head.yRot, anim.headRotY * RAD, weight);

        this.body.xRot = FiskMath.interpolate(this.body.xRot, anim.bodyRotX * RAD, weight);
        this.body.yRot = FiskMath.interpolate(this.body.yRot, anim.bodyRotY * RAD, weight);
        this.body.zRot = FiskMath.interpolate(this.body.zRot, anim.bodyRotZ * RAD, weight);

        this.rightArm.xRot = FiskMath.interpolate(this.rightArm.xRot, anim.rightArmRotX * RAD, weight);
        this.rightArm.yRot = FiskMath.interpolate(this.rightArm.yRot, anim.rightArmRotY * RAD, weight);
        this.rightArm.zRot = FiskMath.interpolate(this.rightArm.zRot, anim.rightArmRotZ * RAD, weight);
        this.rightArm.x = FiskMath.interpolate(-5.0F, -5.0F + anim.rightArmPosX, weight);
        this.rightArm.y = FiskMath.interpolate(2.0F, 2.0F + anim.rightArmPosY, weight);
        this.rightArm.z = FiskMath.interpolate(0.0F, anim.rightArmPosZ, weight);

        this.leftArm.xRot = FiskMath.interpolate(this.leftArm.xRot, anim.leftArmRotX * RAD, weight);
        this.leftArm.yRot = FiskMath.interpolate(this.leftArm.yRot, anim.leftArmRotY * RAD, weight);
        this.leftArm.zRot = FiskMath.interpolate(this.leftArm.zRot, anim.leftArmRotZ * RAD, weight);
        this.leftArm.x = FiskMath.interpolate(5.0F, 5.0F + anim.leftArmPosX, weight);
        this.leftArm.y = FiskMath.interpolate(2.0F, 2.0F + anim.leftArmPosY, weight);
        this.leftArm.z = FiskMath.interpolate(0.0F, anim.leftArmPosZ, weight);

        this.rightLeg.xRot = FiskMath.interpolate(this.rightLeg.xRot, anim.rightLegRotX * RAD, weight);
        this.rightLeg.yRot = FiskMath.interpolate(this.rightLeg.yRot, anim.rightLegRotY * RAD, weight);
        this.rightLeg.zRot = FiskMath.interpolate(this.rightLeg.zRot, anim.rightLegRotZ * RAD, weight);
        this.rightLeg.x = FiskMath.interpolate(-1.9F, -1.9F + anim.rightLegPosX, weight);
        this.rightLeg.y = FiskMath.interpolate(12.0F, 12.0F + anim.rightLegPosY, weight);
        this.rightLeg.z = FiskMath.interpolate(0.0F, anim.rightLegPosZ, weight);

        this.leftLeg.xRot = FiskMath.interpolate(this.leftLeg.xRot, anim.leftLegRotX * RAD, weight);
        this.leftLeg.yRot = FiskMath.interpolate(this.leftLeg.yRot, anim.leftLegRotY * RAD, weight);
        this.leftLeg.zRot = FiskMath.interpolate(this.leftLeg.zRot, anim.leftLegRotZ * RAD, weight);
        this.leftLeg.x = FiskMath.interpolate(1.9F, 1.9F + anim.leftLegPosX, weight);
        this.leftLeg.y = FiskMath.interpolate(12.0F, 12.0F + anim.leftLegPosY, weight);
        this.leftLeg.z = FiskMath.interpolate(0.0F, anim.leftLegPosZ, weight);

        // ท่าต่อย (ทับทุกอย่าง)
        if (this.attackTime > 0) {
            float swing = this.attackTime;
            float f = Mth.sin(Mth.sqrt(swing) * (float)Math.PI);
            float f1 = Mth.sin(Mth.sqrt(swing) * ((float)Math.PI * 2F));
            this.rightArm.xRot -= f * 1.2F;
            this.rightArm.zRot += f1 * 0.4F;
            this.rightArm.yRot += Mth.sin(Mth.sqrt(swing) * (float)Math.PI) * 0.5F;
        }
    }

    private void resetBodyPositions() {
        this.head.x = 0.0F; this.head.y = 0.0F; this.head.z = 0.0F;
        this.body.x = 0.0F; this.body.y = 0.0F; this.body.z = 0.0F;
        this.rightArm.x = -5.0F; this.rightArm.y = 2.0F; this.rightArm.z = 0.0F;
        this.leftArm.x = 5.0F; this.leftArm.y = 2.0F; this.leftArm.z = 0.0F;
        this.rightLeg.x = -1.9F; this.rightLeg.y = 12.0F; this.rightLeg.z = 0.0F;
        this.leftLeg.x = 1.9F; this.leftLeg.y = 12.0F; this.leftLeg.z = 0.0F;
    }

    private void syncLayer2() {
        this.rightPants.copyFrom(this.rightLeg);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightSleeve.copyFrom(this.rightArm);
        this.leftSleeve.copyFrom(this.leftArm);
        this.jacket.copyFrom(this.body);
    }
}