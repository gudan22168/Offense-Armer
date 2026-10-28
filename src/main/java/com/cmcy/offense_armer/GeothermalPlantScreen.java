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
 * 地热电站管理界面：电站状态（储能/送热棒/熔岩抽取）+ 连接设备列表 + 解除连接。
 */
public class GeothermalPlantScreen extends AbstractContainerScreen<GeothermalPlantMenu> {
    private static final int W = 250;
    private static final int H = 214;
    private static final int ROW_H = 22;
    private static final int MAX_ROWS = 4;
    private static final int ROW_Y = 98;

    private static final int C_BG = 0xFF0E141C;
    private static final int C_PANEL = 0xFF16202C;
    private static final int C_PANEL_HOVER = 0xFF1C2A36;
    private static final int C_BORDER = 0xFF24323F;
    private static final int C_ACCENT = 0xFF5CE0D4;
    private static final int C_TEXT = 0xFFDCE8F2;
    private static final int C_DIM = 0xFF6E8497;
    private static final int C_WARN = 0xFFC09060;

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
        g.fill(x + 1, y + 82, x + W - 1, y + 83, C_BORDER);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        int x = 10;
        g.drawString(font, "地热实验电站", x, 7, C_ACCENT, false);

        int energy = menu.getEnergy();
        int max = GeothermalPlantBlockEntity.MAX_ENERGY;
        g.drawString(font, "储能 " + energy + " / " + max, x, 30, C_TEXT, false);
        int barY = 41;
        int barW = W - 20;
        g.fill(x, barY, x + barW, barY + 6, 0xFF0A1016);
        int fill = (int) ((long) barW * Math.min(energy, max) / max);
        if (fill > 0) {
            g.fill(x, barY, x + fill, barY + 6, C_ACCENT);
        }

        int rod = menu.getRodDurability();
        if (rod < 0) {
            g.drawString(font, "送热棒：未安装", x, 52, C_WARN, false);
        } else {
            g.drawString(font, "送热棒耐久：" + rod, x, 52, C_TEXT, false);
        }
        g.drawString(font, "已抽取熔岩：" + menu.getLavaMined() + " 格   燃料：" + (menu.getLavaFuel() / 20) + " 秒",
                x, 62, C_TEXT, false);
        int scan = menu.getLastScanFound();
        String scanText = scan == -1 ? "储能已满，暂停抽取"
                : scan == -2 ? "燃料充足，暂不抽取"
                : scan > 0 ? scan + " 块熔岩" : "未发现熔岩源";
        g.drawString(font, "最近扫描：" + scanText, x, 72, scan == 0 ? C_WARN : C_TEXT, false);

        List<ModNetwork.PlantLink> links = links();
        g.drawString(font, "连接设备 (" + links.size() + ")", x, 86, C_DIM, false);

        for (int i = 0; i < Math.min(links.size(), MAX_ROWS); i++) {
            ModNetwork.PlantLink link = links.get(i);
            int ry = ROW_Y + i * ROW_H;
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

            int bx = x + 212;
            boolean bh = mx >= bx && mx < bx + 16 && my >= ry + 2 && my < ry + 16;
            g.fill(bx, ry + 2, bx + 16, ry + 16, bh ? 0xFF8A3030 : 0xFF3A2028);
            g.drawString(font, "X", bx + 5, ry + 6, 0xFFE0B0B0, false);
        }

        if (links.isEmpty()) {
            g.drawString(font, "暂无连接设备", x + 5, ROW_Y + 6, C_DIM, false);
            g.drawString(font, "用供电连接器 SHIFT+右键 设备", x + 5, ROW_Y + 18, C_DIM, false);
        }

        boolean clearHover = mx >= x && mx < x + 70 && my >= 188 && my < 204;
        g.fill(x, 188, x + 70, 204, clearHover ? 0xFF8A3030 : 0xFF3A2028);
        g.drawString(font, "全部断开", x + 8, 192, 0xFFE0B0B0, false);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0) {
            List<ModNetwork.PlantLink> links = links();
            for (int i = 0; i < Math.min(links.size(), MAX_ROWS); i++) {
                int by = ROW_Y + i * ROW_H + 2;
                double bx = leftPos + 222;
                if (mx >= bx && mx < bx + 16 && my >= topPos + by && my < topPos + by + 14) {
                    PacketDistributor.sendToServer(
                            new ModNetwork.RemoveLinkPayload(menu.getPlantPos(), links.get(i).pos()));
                    return true;
                }
            }
            if (mx >= leftPos + 10 && mx < leftPos + 80
                    && my >= topPos + 188 && my < topPos + 204) {
                PacketDistributor.sendToServer(new ModNetwork.ClearLinksPayload(menu.getPlantPos()));
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }
}
