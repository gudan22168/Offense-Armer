package com.cmcy.offense_armer;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

/**
 * 《魔法禁书目录》绢旗最爱的「氮气装甲」能力还原 mod。
 * NeoForge 1.21.1。
 */
@Mod(Offense_armer.MODID)
public class Offense_armer {
    public static final String MODID = "offense_armer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Offense_armer(IEventBus modEventBus, ModContainer modContainer) {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, screen) -> ModList.get().isLoaded("cloth_config")
                        ? ModConfigScreen.create(screen)
                        : null);

        LOGGER.info("Offense Armer (氮气装甲) 加载完成。");
    }
}
