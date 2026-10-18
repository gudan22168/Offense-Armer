package com.cmcy.offense_armer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Deque;

/**
 * 身体外层的氮气装甲渲染：
 * <ul>
 *   <li>氮气外壳：能力开启时渲染半透明白色发光壳（带呼吸流动效果）。</li>
 *   <li>氮气残影：Lv3+ 移动时在滞后位置渲染逐渐淡出的半透明白色幽灵模型，残影沿实际移动轨迹、不随视角转动。</li>
 * </ul>
 */
public class NitrogenShellLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation SHELL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "textures/entity/nitrogen_shell.png");

    public NitrogenShellLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!ClientAbilityState.active) {
            return;
        }
        PlayerModel<AbstractClientPlayer> model = this.getParentModel();

        // 氮气外壳（呼吸流动效果：透明度 + 缩放随正弦波动）
        float flow = (Mth.sin(ageInTicks * 0.1f) + 1f) * 0.5f;
        float shellScale = 1.10f + 0.025f * flow;
        int shellAlpha = 0x4E + (int) (0x30 * flow);
        poseStack.pushPose();
        poseStack.scale(shellScale, shellScale, shellScale);
        VertexConsumer shell = buffer.getBuffer(RenderType.entityTranslucentEmissive(SHELL_TEXTURE));
        model.renderToBuffer(poseStack, shell, packedLight, OverlayTexture.NO_OVERLAY, (shellAlpha << 24) | 0x00FFFFFF);
        poseStack.popPose();

        // 氮气残影（Lv3+）
        if (ClientAbilityState.level >= 3) {
            Deque<Vec3> ghosts = AfterimageTracker.getPositions();
            int size = ghosts.size();
            if (size > 0) {
                float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
                float undoYaw = (float) Math.toRadians(bodyYaw - 180f);
                int i = 0;
                for (Vec3 ghost : ghosts) {
                    float alpha = (i + 1.0f) / size * 0.25f;
                    int color = ((int) (alpha * 255f) << 24) | 0x00FFFFFF;
                    // 世界位移 → 撤销身体朝向（渲染层 poseStack 已叠加 bodyYaw 的 Y 轴旋转）
                    Vector3f delta = new Vector3f(
                            (float) (ghost.x - player.getX()),
                            (float) (ghost.y - player.getY()),
                            (float) (ghost.z - player.getZ()));
                    delta.rotateY(undoYaw);
                    poseStack.pushPose();
                    poseStack.translate(delta.x(), delta.y(), delta.z());
                    VertexConsumer ghostConsumer = buffer.getBuffer(RenderType.entityTranslucent(SHELL_TEXTURE));
                    model.renderToBuffer(poseStack, ghostConsumer, packedLight, OverlayTexture.NO_OVERLAY, color);
                    poseStack.popPose();
                    i++;
                }
            }
        }
    }
}
