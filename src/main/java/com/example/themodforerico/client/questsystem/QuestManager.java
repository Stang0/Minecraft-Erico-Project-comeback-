package com.example.themodforerico.client.questsystem;

import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;

public class QuestManager {
    private static final List<QuestEntry> quests = new ArrayList<>();

    public static void addQuest(String title, String description, BlockPos pos, boolean shouldTrack) {
        QuestEntry newQuest = new QuestEntry(title, description, pos, shouldTrack);
        quests.add(newQuest);

        // *** แก้ตรงนี้: ถ้า shouldTrack เป็น true ให้สั่ง Track ทันที ***
        if (shouldTrack) {
            trackQuest(newQuest);
        }
    }

    public static void removeQuest(QuestEntry quest) {
        quests.remove(quest);
    }

    public static void removeQuestByName(String title) {
        quests.removeIf(q -> {
            if (q.getTitle().equals(title)) {
                if (q.isTracking()) trackQuest(null);
                return true;
            }
            return false;
        });
    }

    public static void trackQuest(QuestEntry questToTrack) {
        // ปิด Track ตัวเก่าทั้งหมดก่อน
        for (QuestEntry q : quests) q.setTracking(false);

        // ถ้าตัวใหม่มีค่า (ไม่เป็น null) ให้เปิด Track
        if (questToTrack != null) questToTrack.setTracking(true);
    }

    public static QuestEntry getTrackedQuest() {
        for (QuestEntry q : quests) {
            if (q.isTracking()) return q;
        }
        return null;
    }

    public static List<QuestEntry> getAllQuests() {
        return quests;
    }
}