package com.cmcy.offense_armer;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetwork {

    @EventBusSubscriber(modid = Offense_armer.MODID)
    public static class Registrar {
        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");
            registrar.playToClient(SyncAbilityPayload.TYPE, SyncAbilityPayload.STREAM_CODEC, SyncAbilityPayload::handle);
            registrar.playToServer(ToggleAbilityPayload.TYPE, ToggleAbilityPayload.STREAM_CODEC, ToggleAbilityPayload::handle);
            registrar.playToServer(SetBurstPayload.TYPE, SetBurstPayload.STREAM_CODEC, SetBurstPayload::handle);
        }
    }

    public static void syncToClient(ServerPlayer player, NitrogenAbility ability) {
        PacketDistributor.sendToPlayer(player, new SyncAbilityPayload(
                ability.getLevel(), ability.getNitrogen(), ability.getMaxNitrogen(), ability.isActive()));
    }

    public record SyncAbilityPayload(int level, float nitrogen, float maxNitrogen, boolean active)
            implements CustomPacketPayload {
        public static final Type<SyncAbilityPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "sync_ability"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SyncAbilityPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, SyncAbilityPayload::level,
                ByteBufCodecs.FLOAT, SyncAbilityPayload::nitrogen,
                ByteBufCodecs.FLOAT, SyncAbilityPayload::maxNitrogen,
                ByteBufCodecs.BOOL, SyncAbilityPayload::active,
                SyncAbilityPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(SyncAbilityPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                ClientAbilityState.level = payload.level();
                ClientAbilityState.nitrogen = payload.nitrogen();
                ClientAbilityState.maxNitrogen = payload.maxNitrogen();
                ClientAbilityState.active = payload.active();
            });
        }
    }

    public record SetBurstPayload(boolean bursting) implements CustomPacketPayload {
        public static final Type<SetBurstPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "set_burst"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SetBurstPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, SetBurstPayload::bursting,
                SetBurstPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(SetBurstPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    NitrogenAbility ability = player.getData(ModAttachments.NITROGEN_ABILITY.get());
                    ability.setBursting(payload.bursting());
                }
            });
        }
    }

    public record ToggleAbilityPayload() implements CustomPacketPayload {
        public static final Type<ToggleAbilityPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "toggle_ability"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ToggleAbilityPayload> STREAM_CODEC =
                StreamCodec.unit(new ToggleAbilityPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(ToggleAbilityPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    NitrogenAbility ability = player.getData(ModAttachments.NITROGEN_ABILITY.get());
                    if (ability.isActive()) {
                        ability.setActive(false);
                    } else {
                        if (ability.getLevel() < 1) {
                            player.displayClientMessage(Component.translatable("message.offense_armer.not_developed"), true);
                            return;
                        }
                        if (ability.getNitrogen() <= 0f) {
                            player.displayClientMessage(Component.translatable("message.offense_armer.no_nitrogen"), true);
                            return;
                        }
                        ability.setActive(true);
                    }
                    syncToClient(player, ability);
                }
            });
        }
    }
}
