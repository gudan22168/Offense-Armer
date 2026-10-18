package com.cmcy.offense_armer;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 客户端追踪玩家最近的位置，用于渲染氮气残影（幽灵尾迹）。
 * 能力开启且 Lv3+ 移动时记录位置；停止/关闭时逐帧淡出。
 */
public class AfterimageTracker {
    private static final int MAX_FRAMES = 5;
    private static final Deque<Vec3> positions = new ArrayDeque<>();

    private AfterimageTracker() {
    }

    public static void tick(LocalPlayer player) {
        boolean trailing = ClientAbilityState.active && ClientAbilityState.level >= 3
                && player.getDeltaMovement().lengthSqr() > 0.02;
        if (trailing) {
            if (player.tickCount % 2 == 0) {
                positions.addLast(player.position());
                while (positions.size() > MAX_FRAMES) {
                    positions.removeFirst();
                }
            }
        } else if (!positions.isEmpty()) {
            positions.removeFirst();
        }
    }

    public static Deque<Vec3> getPositions() {
        return positions;
    }
}
