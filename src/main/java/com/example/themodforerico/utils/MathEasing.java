package com.example.themodforerico.utils;

public class MathEasing {
    private static float easeOut(float t) {
        return (float)((double)1.0F - Math.pow((double)(1.0F - t), (double)8.0F));
    }

    private static float easeIn(float t) {
        return (float)Math.pow((double)t, (double)8.0F);
    }

    private static float easeInOut(float t) {
        return (float)(Math.pow((double)t, (double)2.0F) * (double)(3.0F - 2.0F * t));
    }

    private static float elasticEaseOut(float t) {
        return (float)((double)1.0F - Math.pow((double)2.0F, (double)(-10.0F * t)) * Math.cos((double)t * Math.PI * (double)4.0F));
    }

    public static float easeOutLerp(float start, float end, float t) {
        return start + (end - start) * easeOut(t);
    }

    public static int easeOutLerp(int start, int end, float t) {
        return (int)((float)start + (float)(end - start) * easeOut(t));
    }

    public static float easeInLerp(float start, float end, float t) {
        return start + (end - start) * easeIn(t);
    }

    public static int easeInLerp(int start, int end, float t) {
        return (int)((float)start + (float)(end - start) * easeIn(t));
    }

    public static float easeInOutLerp(float start, float end, float t) {
        return start + (end - start) * easeInOut(t);
    }

    public static int easeInOutLerp(int start, int end, float t) {
        return (int)((float)start + (float)(end - start) * easeInOut(t));
    }

    public static float elasticEaseOutLerp(float start, float end, float t) {
        return start + (end - start) * elasticEaseOut(t);
    }

    public static int elasticEaseOutLerp(int start, int end, float t) {
        return (int)((float)start + (float)(end - start) * elasticEaseOut(t));
    }
}