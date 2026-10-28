package com.cmcy.offense_armer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * 手持已绑定的供电连接器时，在绑定的地热电站位置渲染高亮方框。
 */
@EventBusSubscriber(modid = Offense_armer.MODID, value = Dist.CLIENT)
public class LinkerHighlightRenderer {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        ItemStack linker = ItemStack.EMPTY;
        if (mc.player.getMainHandItem().getItem() instanceof PowerLinkerItem) {
            linker = mc.player.getMainHandItem();
        } else if (mc.player.getOffhandItem().getItem() instanceof PowerLinkerItem) {
            linker = mc.player.getOffhandItem();
        }
        if (linker.isEmpty()) {
            return;
        }
        BlockPos pos = PowerLinkerItem.getSelectedPlant(linker);
        if (pos == null) {
            return;
        }

        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);

        AABB box = new AABB(
                pos.getX() - 0.02, pos.getY() - 0.02, pos.getZ() - 0.02,
                pos.getX() + 2.02, pos.getY() + 2.02, pos.getZ() + 2.02);

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        LevelRenderer.renderLineBox(pose, buffers.getBuffer(RenderType.lines()), box,
                0.36f, 0.92f, 0.86f, 1.0f);
        buffers.endBatch(RenderType.lines());
        pose.popPose();
    }
}
