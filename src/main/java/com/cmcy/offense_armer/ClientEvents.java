package com.cmcy.offense_armer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 客户端按键处理：按 X 切换能力开关、按 R 爆冲；爆冲时身体俯冲（滑翔姿态）。
 */
@EventBusSubscriber(modid = Offense_armer.MODID, value = Dist.CLIENT)
public class ClientEvents {
    /** 爆冲时的俯冲倾角（度）：负值=头朝前俯冲。 */
    private static final float GLIDE_PITCH = -80f;

    private static boolean burstKeyDown = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (ClientSetup.TOGGLE_ABILITY_KEY.consumeClick()) {
            PacketDistributor.sendToServer(new ModNetwork.ToggleAbilityPayload());
        }
        boolean down = ClientSetup.BURST_KEY.isDown();
        if (down || down != burstKeyDown) {
            burstKeyDown = down;
            PacketDistributor.sendToServer(new ModNetwork.SetBurstPayload(down));
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        // 仅本地玩家，且能力开启 + Lv4 飞行 + 按住爆冲键 + 正在移动
        if (player != Minecraft.getInstance().player) {
            return;
        }
        if (!ClientAbilityState.active || ClientAbilityState.level < 4) {
            return;
        }
        if (!ClientSetup.BURST_KEY.isDown()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        float bodyYaw = Mth.rotLerp(event.getPartialTick(), player.yBodyRotO, player.yBodyRot);
        // 按住 Shift（下砸）时头朝下垂直俯冲，否则水平俯冲
        float pitch = player.isShiftKeyDown() ? -90f : GLIDE_PITCH;
        float yaw = 180f - bodyYaw;
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
    }
}
