package com.cmcy.offense_armer;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * 调试命令：/nitrogen level|fill|set|toggle（需要权限等级 2）。
 */
@EventBusSubscriber(modid = Offense_armer.MODID)
public class NitrogenCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("nitrogen")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("level")
                        .then(Commands.argument("level", IntegerArgumentType.integer(0, NitrogenAbility.MAX_LEVEL))
                                .executes(ctx -> {
                                    int lvl = IntegerArgumentType.getInteger(ctx, "level");
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    NitrogenAbility a = player.getData(ModAttachments.NITROGEN_ABILITY.get());
                                    a.setLevel(lvl);
                                    ModNetwork.syncToClient(player, a);
                                    return 1;
                                })))
                .then(Commands.literal("fill")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            NitrogenAbility a = player.getData(ModAttachments.NITROGEN_ABILITY.get());
                            a.setNitrogen(a.getMaxNitrogen());
                            ModNetwork.syncToClient(player, a);
                            return 1;
                        }))
                .then(Commands.literal("set")
                        .then(Commands.argument("amount", FloatArgumentType.floatArg(0f))
                                .executes(ctx -> {
                                    float amt = FloatArgumentType.getFloat(ctx, "amount");
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    NitrogenAbility a = player.getData(ModAttachments.NITROGEN_ABILITY.get());
                                    a.setNitrogen(amt);
                                    ModNetwork.syncToClient(player, a);
                                    return 1;
                                })))
                .then(Commands.literal("toggle")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            NitrogenAbility a = player.getData(ModAttachments.NITROGEN_ABILITY.get());
                            a.setActive(!a.isActive());
                            ModNetwork.syncToClient(player, a);
                            return 1;
                        })));
    }
}
