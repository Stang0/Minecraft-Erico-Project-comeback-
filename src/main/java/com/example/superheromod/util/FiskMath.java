package com.example.superheromod.util;

public class FiskMath {

    // Linear Interpolation (พื้นฐาน)
    public static float interpolate(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    // Rotation Interpolation (สำคัญมาก! แก้บั๊กหมุนควงสว่าน 360 องศา)
    public static float interpolateRot(float start, float end, float progress) {
        float f;
        for (f = end - start; f < -180.0F; f += 360.0F) { ; }
        while (f >= 180.0F) { f -= 360.0F; }
        return start + progress * f;
    }

    // Sine Wave สำหรับ Curve
    public static float curveCrests(float d) {
        return (float) Math.sin(d * Math.PI / 2.0F);
    }

    public static float curve(float value) {
        double d = (double) value;
        return (float) ((Math.sin(d * Math.PI - Math.PI / 2) + 1.0) / 2.0);
    }

    // Clamp (จำกัดค่า)
    public static float clamp(float value, float min, float max) {
        return value < min ? min : (value > max ? max : value);
    }

    // แปลงความเร็วเป็น Factor 0-1 (สูตร Fisk)
    public static float getSpeedFactor(double speedSq) {
        return clamp((float) (Math.sqrt(speedSq) * 2.5), 0.0F, 1.0F);
    }
}