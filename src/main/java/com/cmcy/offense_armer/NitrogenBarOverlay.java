package com.cmcy.offense_armer;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/**
 * 左上角的氮气条 HUD（图片版）。仅在能力开发（Lv1 及以上）后显示。
 *
 * <p>布局：左侧 16×16 图标（N₂ 晶体板） + 中间 100×10 氮气条 + 右侧 Lv 等级文字。
 * 氮气条：空条底（bg）+ 按比例裁切的充能层（fill）；满能量时切换为合成成品（full）。
 */
public class NitrogenBarOverlay {
    private static final ResourceLocation BG_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "textures/gui/nitrogen_bar_bg.png");
    private static final ResourceLocation FILL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "textures/gui/nitrogen_bar_fill.png");
    private static final ResourceLocation FULL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "textures/gui/nitrogen_bar_full.png");
    private static final ResourceLocation ICON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "textures/gui/nitrogen_icon.png");

    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 10;
    private static final int ICON_SIZE = 16;
    /** 氮气低于该比例（20%）时填充层变红提醒。 */
    private static final float LOW_THRESHOLD = 0.2f;

    public static void render(GuiGraphics g, DeltaTracker deltaTracker) {
        if (!ClientAbilityState.active) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.options.hideGui) {
            return;
        }
        Font font = mc.font;
        int x = 8;
        int y = 8;

        // 平滑过渡
        float target = ClientAbilityState.nitrogen;
        ClientAbilityState.displayNitrogen += (target - ClientAbilityState.displayNitrogen) * 0.25f;
        float frac = ClientAbilityState.maxNitrogen <= 0f
                ? 0f
                : ClientAbilityState.displayNitrogen / ClientAbilityState.maxNitrogen;
        frac = Math.max(0f, Math.min(1f, frac));

        // 图标
        g.blit(ICON_TEXTURE, x, y, 0, 0f, 0f, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);

        // 氮气条（垂直居中于图标）
        int bx = x + ICON_SIZE + 4;
        int by = y + (ICON_SIZE - BAR_HEIGHT) / 2;
        if (frac >= 1f) {
            g.blit(FULL_TEXTURE, bx, by, 0, 0f, 0f, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        } else {
            g.blit(BG_TEXTURE, bx, by, 0, 0f, 0f, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
            int fillW = (int) (BAR_WIDTH * frac);
            if (fillW > 0) {
                // 氮气过低（<20%）时填充层变红提醒
                if (frac < LOW_THRESHOLD) {
                    g.setColor(1.0f, 0.25f, 0.25f, 1.0f);
                }
                g.blit(FILL_TEXTURE, bx, by, 0, 0f, 0f, fillW, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
                g.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }

        // 等级文字
        g.drawString(font, "Lv " + ClientAbilityState.level, bx + BAR_WIDTH + 5, by + 1, 0xFFa8d8ff);
    }
}
