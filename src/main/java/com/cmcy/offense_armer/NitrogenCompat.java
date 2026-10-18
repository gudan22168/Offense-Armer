package com.cmcy.offense_armer;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import java.util.HashSet;
import java.util.Set;

/**
 * 跨模组氮气兼容：通过「流体 ID 配置列表 + 流体标签」识别其他模组注册的氮气。
 *
 * <p>用法：在配置 offense_armer-common.toml 的 nitrogenFluids 里填入目标模组的氮气流体 ID
 * （如 {@code c:...} 标签或 {@code modid:nitrogen}），或设置 nitrogenFluidTag 指向一个氮气标签。
 *
 * <p>「手动从其他模组储罐抽取氮气」与「Lv5 反向充能到其他模组设备/储罐」的具体接入，
 * 需依赖目标模组的流体/气体 API（Mekanism 的氮气是独立的气体系统，而非 Forge 流体），
 * 后续选定目标模组后在此类上扩展对应的抽取/注入逻辑即可。
 */
public class NitrogenCompat {
    private static Set<Fluid> nitrogenFluids = Set.of();
    private static TagKey<Fluid> nitrogenTag = null;

    private NitrogenCompat() {
    }

    public static boolean validateFluidName(Object obj) {
        return obj instanceof String && !((String) obj).isBlank();
    }

    /** 配置加载后调用，重建氮气流体识别表。 */
    public static void reload() {
        Set<Fluid> fluids = new HashSet<>();
        for (String id : Config.nitrogenFluidIds) {
            Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(id));
            if (fluid != null) {
                fluids.add(fluid);
            }
        }
        nitrogenFluids = Set.copyOf(fluids);

        nitrogenTag = null;
        if (Config.nitrogenFluidTag != null && !Config.nitrogenFluidTag.isBlank()) {
            nitrogenTag = TagKey.create(Registries.FLUID, ResourceLocation.parse(Config.nitrogenFluidTag));
        }
    }

    /** 判断一个流体是否被识别为「氮气」。 */
    public static boolean isNitrogenFluid(Fluid fluid) {
        if (fluid == null) {
            return false;
        }
        if (nitrogenTag != null && BuiltInRegistries.FLUID.wrapAsHolder(fluid).is(nitrogenTag)) {
            return true;
        }
        return nitrogenFluids.contains(fluid);
    }
}
