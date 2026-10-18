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
 * 能力开发药：喝下后把能力等级提升到指定阶段（需要前置阶段已达成）。
 */
public class AbilityPotionItem extends Item {
    private final int targetLevel;

    public AbilityPotionItem(Properties properties, int targetLevel) {
        super(properties);
        this.targetLevel = targetLevel;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer sp) {
            NitrogenAbility ability = sp.getData(ModAttachments.NITROGEN_ABILITY.get());
            if (ability.getLevel() == targetLevel - 1) {
                ability.setLevel(targetLevel);
                sp.displayClientMessage(Component.translatable("message.offense_armer.level_up", targetLevel), false);
                if (!sp.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                ModNetwork.syncToClient(sp, ability);
                return InteractionResultHolder.consume(stack);
            } else if (ability.getLevel() >= targetLevel) {
                sp.displayClientMessage(Component.translatable("message.offense_armer.already_developed"), true);
                return InteractionResultHolder.fail(stack);
            } else {
                sp.displayClientMessage(Component.translatable("message.offense_armer.too_early"), true);
                return InteractionResultHolder.fail(stack);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
