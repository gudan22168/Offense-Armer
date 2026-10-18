package com.cmcy.offense_armer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 客户端按键处理：按 X 切换能力开关、按 R 爆冲。
 */
@EventBusSubscriber(modid = Offense_armer.MODID, value = Dist.CLIENT)
public class ClientEvents {
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
}
