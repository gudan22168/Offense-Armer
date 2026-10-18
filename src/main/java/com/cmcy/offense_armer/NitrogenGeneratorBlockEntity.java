package com.cmcy.offense_armer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * 制氮机方块实体：存储电量与氮气（默认满），站在机器附近自动给玩家回氮。
 */
public class NitrogenGeneratorBlockEntity extends BlockEntity {
    public static final int MAX_ENERGY = 10000;
    public static final int MAX_NITROGEN = 10000;

    private int energy = MAX_ENERGY;
    private int nitrogen = MAX_NITROGEN;

    public NitrogenGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.NITROGEN_GENERATOR_BE.get(), pos, state);
    }

    public int getEnergy() {
        return energy;
    }

    public int getNitrogen() {
        return nitrogen;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NitrogenGeneratorBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }
        if (level.getGameTime() % 10 != 0) {
            return;
        }
        // 站在机器附近（右侧一格 / 前方）自动回氮
        AABB area = new AABB(pos).inflate(2.0);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, area);
        for (ServerPlayer player : players) {
            NitrogenAbility ability = player.getData(ModAttachments.NITROGEN_ABILITY.get());
            float missing = ability.getMaxNitrogen() - ability.getNitrogen();
            float add = Math.min(5.0f, missing);
            if (add > 0 && be.nitrogen >= (int) (add * 20)) {
                ability.addNitrogen(add);
                be.nitrogen -= (int) (add * 20);
                ModNetwork.syncToClient(player, ability);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("Energy", energy);
        tag.putInt("Nitrogen", nitrogen);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("Energy")) {
            energy = tag.getInt("Energy");
        }
        if (tag.contains("Nitrogen")) {
            nitrogen = tag.getInt("Nitrogen");
        }
    }
}
