package com.cmcy.offense_armer;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 客户端按键处理：按 X 切换能力开关、按 R 爆冲；每 tick 更新残影追踪。
 */
@EventBusSubscriber(modid = Offense_armer.MODID, value = Dist.CLIENT)
public class ClientEvents {
    private static boolean burstKeyDown = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            AfterimageTracker.tick(mc.player);
        }
        while (ClientSetup.TOGGLE_ABILITY_KEY.consumeClick()) {
            PacketDistributor.sendToServer(new ModNetwork.ToggleAbilityPayload());
        }
        boolean down = ClientSetup.BURST_KEY.isDown();
        if (down || down != burstKeyDown) {
            burstKeyDown = down;
            PacketDistributor.sendToServer(new ModNetwork.SetBurstPayload(down));
        }
    }
}
