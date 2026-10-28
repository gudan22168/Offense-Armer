package com.cmcy.offense_armer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 制氮机 GUI：背景 + 电量/空气/氮气三条竖条。
 */
public class NitrogenGeneratorScreen extends AbstractContainerScreen<NitrogenGeneratorMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "textures/gui/nitrogen_generator.png");
    private static final ResourceLocation BAR_FRAME =
            ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "textures/gui/bar_frame.png");
    private static final ResourceLocation BAR_ENERGY =
            ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "textures/gui/bar_fill_energy.png");
    private static final ResourceLocation BAR_AIR =
            ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "textures/gui/bar_fill_air.png");
    private static final ResourceLocation BAR_NITROGEN =
            ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "textures/gui/bar_fill_nitrogen.png");

    private static final int BAR_W = 14;
    private static final int BAR_H = 40;
    private static final int MAX = NitrogenGeneratorBlockEntity.MAX_NITROGEN;

    public NitrogenGeneratorScreen(NitrogenGeneratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.blit(TEXTURE, leftPos, topPos, 0, 0f, 0f, imageWidth, imageHeight, imageWidth, imageHeight);
        renderBar(g, leftPos + 49, topPos + 22, menu.getEnergy(), BAR_ENERGY);
        renderBar(g, leftPos + 69, topPos + 22, menu.getAir(), BAR_AIR);
        renderBar(g, leftPos + 113, topPos + 22, menu.getNitrogen(), BAR_NITROGEN);
    }

    private void renderBar(GuiGraphics g, int x, int y, int value, ResourceLocation fill) {
        g.blit(BAR_FRAME, x, y, 0, 0f, 0f, BAR_W, BAR_H, BAR_W, BAR_H);
        int h = MAX > 0 ? (int) Math.round((double) BAR_H * value / MAX) : 0;
        if (h > 0) {
            h = Math.min(h, BAR_H);
            g.blit(fill, x, y + BAR_H - h, 0, 0f, (float) (BAR_H - h), BAR_W, h, BAR_W, BAR_H);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title, 8, 6, 0xFFCED5DD, false);
        g.drawString(this.font, this.playerInventoryTitle, 8, 82, 0xFF9AA3AD, false);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        // 物品提示（兜底，防止某些情况下不显示）
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            var stack = this.hoveredSlot.getItem();
            g.renderTooltip(this.font, this.getTooltipFromContainerItem(stack),
                    stack.getTooltipImage(), stack, mouseX, mouseY);
            return;
        }
        // 竖条数值提示
        if (isHovering(49, 22, BAR_W, BAR_H, mouseX, mouseY)) {
            g.renderTooltip(this.font, Component.translatable("tooltip.offense_armer.bar_energy",
                    menu.getEnergy(), MAX), mouseX, mouseY);
        } else if (isHovering(69, 22, BAR_W, BAR_H, mouseX, mouseY)) {
            g.renderTooltip(this.font, Component.translatable("tooltip.offense_armer.bar_air",
                    menu.getAir(), MAX), mouseX, mouseY);
        } else if (isHovering(113, 22, BAR_W, BAR_H, mouseX, mouseY)) {
            g.renderTooltip(this.font, Component.translatable("tooltip.offense_armer.bar_nitrogen",
                    menu.getNitrogen(), MAX), mouseX, mouseY);
        } else if (isHovering(89, 33, 18, 18, mouseX, mouseY)
                && menu.slots.get(NitrogenGeneratorMenu.SLOT_SIEVE).getItem().isEmpty()) {
            g.renderTooltip(this.font, Component.translatable("tooltip.offense_armer.slot_sieve"),
                    mouseX, mouseY);
        }
    }
}
