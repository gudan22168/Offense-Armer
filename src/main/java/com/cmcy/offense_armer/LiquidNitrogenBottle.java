package com.cmcy.offense_armer;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 液氮瓶：手动补充氮气的基础来源（初始阶段的主要获取方式）。
 */
public class LiquidNitrogenBottle extends Item {
    public static final float FILL_AMOUNT = 30f;

    public LiquidNitrogenBottle(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer sp) {
            NitrogenAbility ability = sp.getData(ModAttachments.NITROGEN_ABILITY.get());
            if (ability.getNitrogen() >= ability.getMaxNitrogen()) {
                sp.displayClientMessage(Component.translatable("message.offense_armer.full"), true);
                return InteractionResultHolder.fail(stack);
            }
            ability.addNitrogen(FILL_AMOUNT);
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
            sp.displayClientMessage(Component.translatable("message.offense_armer.refilled"), true);
            ModNetwork.syncToClient(sp, ability);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
