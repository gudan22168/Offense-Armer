package com.cmcy.offense_armer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * 供电连接器：右键电站选中，Shift+右键范围内设备建立连接。
 */
public class PowerLinkerItem extends Item {
    private static final String KEY_PLANT = "Plant";

    public PowerLinkerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockState state = level.getBlockState(pos);

        // 右键电站：选中
        if (state.is(ModBlocks.GEOTHERMAL_PLANT.get())) {
            if (!level.isClientSide()) {
                BlockPos master = GeothermalPlantBlock.getMasterPos(pos, state);
                CustomData.update(DataComponents.CUSTOM_DATA, stack,
                        tag -> tag.putLong(KEY_PLANT, master.asLong()));
                player.displayClientMessage(Component.translatable("message.offense_armer.linker_selected",
                        master.getX(), master.getY(), master.getZ()), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        // Shift+右键设备：连接
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                handleLink(player, stack, pos, level);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    private static void handleLink(Player player, ItemStack stack, BlockPos devicePos, Level level) {
        BlockPos plantPos = getSelectedPlant(stack);
        if (plantPos == null) {
            player.displayClientMessage(Component.translatable("message.offense_armer.linker_no_plant"), true);
            return;
        }
        if (!(level.getBlockEntity(plantPos) instanceof GeothermalPlantBlockEntity plant)) {
            player.displayClientMessage(Component.translatable("message.offense_armer.linker_plant_gone"), true);
            return;
        }
        double range = Config.geoWirelessRadius;
        if (plantPos.distSqr(devicePos) > range * range) {
            player.displayClientMessage(Component.translatable("message.offense_armer.linker_too_far"), true);
            return;
        }
        IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, devicePos, null);
        if (storage == null || !storage.canReceive()) {
            player.displayClientMessage(Component.translatable("message.offense_armer.linker_not_device"), true);
            return;
        }
        if (plant.addLink(devicePos)) {
            player.displayClientMessage(Component.translatable("message.offense_armer.linker_linked",
                    devicePos.getX(), devicePos.getY(), devicePos.getZ()), true);
            if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                ModNetwork.syncPlantLinks(sp, plant);
            }
        } else {
            player.displayClientMessage(Component.translatable("message.offense_armer.linker_already"), true);
        }
    }

    public static BlockPos getSelectedPlant(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        CompoundTag tag = data.copyTag();
        return tag.contains(KEY_PLANT) ? BlockPos.of(tag.getLong(KEY_PLANT)) : null;
    }
}
