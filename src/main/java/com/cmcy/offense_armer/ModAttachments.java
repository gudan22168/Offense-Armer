package com.cmcy.offense_armer;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * 能力数据挂载点注册。使用 NeoForge 1.21.1 的 DataAttachment 体系。
 */
public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Offense_armer.MODID);

    public static final Supplier<AttachmentType<NitrogenAbility>> NITROGEN_ABILITY = ATTACHMENT_TYPES.register(
            "nitrogen_ability",
            () -> AttachmentType.builder(NitrogenAbility::new)
                    .serialize(NitrogenAbility.CODEC)
                    .copyOnDeath()
                    .build()
    );
}
