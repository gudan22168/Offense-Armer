package com.cmcy.offense_armer;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 地热电站管理界面：储能、连接设备列表、解除连接。
 */
public class GeothermalPlantScreen extends AbstractContainerScreen<GeothermalPlantMenu> {
    private static final int W = 250;
    private static final int H = 196;
    private static final int ROW_H = 22;
    private static final int MAX_ROWS = 5;

    private static final int C_BG = 0xFF0E141C;
    private static final int C_PANEL = 0xFF16202C;
    private static final int C_PANEL_HOVER = 0xFF1C2A36;
    private static final int C_BORDER = 0xFF24323F;
    private static final int C_ACCENT = 0xFF5CE0D4;
    private static final int C_TEXT = 0xFFDCE8F2;
    private static final int C_DIM = 0xFF6E8497;

    public GeothermalPlantScreen(GeothermalPlantMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = W;
        this.imageHeight = H;
    }

    @Override
    protected void init() {
        super.init();
        PacketDistributor.sendToServer(new ModNetwork.RequestLinksPayload(menu.getPlantPos()));
    }

    private List<ModNetwork.PlantLink> links() {
        if (menu.getPlantPos().equals(ClientPlantLinks.plant)) {
            return ClientPlantLinks.links;
        }
        return List.of();
    }

    private String deviceName(BlockPos pos) {
        if (minecraft == null || minecraft.level == null) {
            return "?";
        }
        BlockState state = minecraft.level.getBlockState(pos);
        if (state.isAir()) {
            return "已失效";
        }
        return state.getBlock().getName().getString();
    }

    @Override
    protected void renderBg(GuiGraphics g, float partial, int mx, int my) {
        int x = leftPos;
        int y = topPos;
        g.fill(x, y, x + W, y + H, C_BG);
        g.fill(x, y, x + W, y + 1, C_ACCENT);
        g.fill(x, y + H - 1, x + W, y + H, C_BORDER);
        g.fill(x, y, x + 1, y + H, C_BORDER);
        g.fill(x + W - 1, y, x + W, y + H, C_BORDER);
        g.fill(x + 1, y + 24, x + W - 1, y + 25, C_BORDER);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        int x = 10;
        g.drawString(font, "地热实验电站", x, 7, C_ACCENT, false);

        int energy = menu.getEnergy();
        int max = GeothermalPlantBlockEntity.MAX_ENERGY;
        g.drawString(font, "储能 " + energy + " / " + max, x, 28, C_TEXT, false);

        int barX = x;
        int barY = 39;
        int barW = W - 20;
        g.fill(barX, barY, barX + barW, barY + 6, 0xFF0A1016);
        int fill = (int) ((long) barW * Math.min(energy, max) / max);
        if (fill > 0) {
            g.fill(barX, barY, barX + fill, barY + 6, C_ACCENT);
        }

        List<ModNetwork.PlantLink> links = links();
        g.drawString(font, "连接设备 (" + links.size() + ")", x, 52, C_DIM, false);

        for (int i = 0; i < Math.min(links.size(), MAX_ROWS); i++) {
            ModNetwork.PlantLink link = links.get(i);
            int ry = 66 + i * ROW_H;
            boolean hover = mx >= x + 212 && mx < x + 228 && my >= ry + 2 && my < ry + 16;
            g.fill(x, ry, W - 10, ry + ROW_H - 3, hover ? C_PANEL_HOVER : C_PANEL);

            g.drawString(font, deviceName(link.pos()), x + 5, ry + 3, C_TEXT, false);
            g.drawString(font, link.pos().getX() + ", " + link.pos().getY() + ", " + link.pos().getZ(),
                    x + 5, ry + 12, C_DIM, false);

            if (link.energy() >= 0) {
                int ebX = x + 152;
                int ebW = 52;
                g.fill(ebX, ry + 6, ebX + ebW, ry + 12, 0xFF0A1016);
                int ef = (int) ((long) ebW * Math.min(link.energy(), link.max()) / Math.max(1, link.max()));
                if (ef > 0) {
                    g.fill(ebX, ry + 6, ebX + ef, ry + 12, C_ACCENT);
                }
            } else {
                g.drawString(font, "离线", x + 152, ry + 5, 0xFFB05050, false);
            }

            // 解除按钮
            int bx = x + 212;
            boolean bh = mx >= bx && mx < bx + 16 && my >= ry + 2 && my < ry + 16;
            g.fill(bx, ry + 2, bx + 16, ry + 16, bh ? 0xFF8A3030 : 0xFF3A2028);
            g.drawString(font, "X", bx + 5, ry + 6, 0xFFE0B0B0, false);
        }

        if (links.isEmpty()) {
            g.drawString(font, "暂无连接设备", x + 5, 72, C_DIM, false);
            g.drawString(font, "用供电连接器 SHIFT+右键 设备", x + 5, 84, C_DIM, false);
        }

        // 底部按钮
        boolean clearHover = mx >= x && mx < x + 70 && my >= 172 && my < 188;
        g.fill(x, 172, x + 70, 188, clearHover ? 0xFF8A3030 : 0xFF3A2028);
        g.drawString(font, "全部断开", x + 8, 176, 0xFFE0B0B0, false);
        g.drawString(font, "X 解除单台", x + 80, 176, C_DIM, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            List<ModNetwork.PlantLink> links = links();
            for (int i = 0; i < Math.min(links.size(), MAX_ROWS); i++) {
                int by = 66 + i * ROW_H + 2;
                if (mx >= leftPos + 10 + 202 && mx < leftPos + 10 + 218
                        && my >= topPos + by && my < topPos + by + 14) {
                    PacketDistributor.sendToServer(
                            new ModNetwork.RemoveLinkPayload(menu.getPlantPos(), links.get(i).pos()));
                    return true;
                }
            }
            if (mx >= leftPos + 10 && mx < leftPos + 80
                    && my >= topPos + 172 && my < topPos + 188) {
                PacketDistributor.sendToServer(new ModNetwork.ClearLinksPayload(menu.getPlantPos()));
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }
}
