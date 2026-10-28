package com.cmcy.offense_armer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
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
            registrar.playToClient(PlantLinksPayload.TYPE, PlantLinksPayload.STREAM_CODEC, PlantLinksPayload::handle);
            registrar.playToServer(RemoveLinkPayload.TYPE, RemoveLinkPayload.STREAM_CODEC, RemoveLinkPayload::handle);
            registrar.playToServer(ClearLinksPayload.TYPE, ClearLinksPayload.STREAM_CODEC, ClearLinksPayload::handle);
            registrar.playToServer(RequestLinksPayload.TYPE, RequestLinksPayload.STREAM_CODEC, RequestLinksPayload::handle);
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
                        float threshold = (float) (ability.getMaxNitrogen() * Config.reenableThreshold);
                        if (ability.getNitrogen() < threshold) {
                            player.displayClientMessage(Component.translatable("message.offense_armer.overloaded"), true);
                            return;
                        }
                        ability.setActive(true);
                    }
                    syncToClient(player, ability);
                }
            });
        }
    }

    // ---------------- 电站连接 ----------------

    public record PlantLink(BlockPos pos, int energy, int max) {
    }

    /** 把电站的连接列表与实时电量发给客户端界面。 */
    public static void syncPlantLinks(ServerPlayer player, GeothermalPlantBlockEntity be) {
        List<PlantLink> list = new ArrayList<>();
        for (BlockPos p : be.getLinks()) {
            var storage = be.getLevel() == null ? null : EnergyHelper.findAny(be.getLevel(), p);
            list.add(new PlantLink(p,
                    storage == null ? -1 : storage.getEnergyStored(),
                    storage == null ? -1 : storage.getMaxEnergyStored()));
        }
        PacketDistributor.sendToPlayer(player, new PlantLinksPayload(be.getBlockPos(), list));
    }

    public record PlantLinksPayload(BlockPos plant, List<PlantLink> links) implements CustomPacketPayload {
        public static final Type<PlantLinksPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "plant_links"));
        public static final StreamCodec<RegistryFriendlyByteBuf, PlantLinksPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeBlockPos(payload.plant());
                    buf.writeVarInt(payload.links().size());
                    for (PlantLink l : payload.links()) {
                        buf.writeBlockPos(l.pos());
                        buf.writeVarInt(Math.max(-1, l.energy()));
                        buf.writeVarInt(Math.max(-1, l.max()));
                    }
                },
                buf -> {
                    BlockPos plant = buf.readBlockPos();
                    int n = buf.readVarInt();
                    List<PlantLink> list = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) {
                        list.add(new PlantLink(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt()));
                    }
                    return new PlantLinksPayload(plant, list);
                }
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(PlantLinksPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> ClientPlantLinks.set(payload.plant(), payload.links()));
        }
    }

    public record RemoveLinkPayload(BlockPos plant, BlockPos device) implements CustomPacketPayload {
        public static final Type<RemoveLinkPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "remove_link"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RemoveLinkPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeBlockPos(payload.plant());
                    buf.writeBlockPos(payload.device());
                },
                buf -> new RemoveLinkPayload(buf.readBlockPos(), buf.readBlockPos())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(RemoveLinkPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer sp
                        && sp.level().getBlockEntity(payload.plant()) instanceof GeothermalPlantBlockEntity be) {
                    be.removeLink(payload.device());
                    syncPlantLinks(sp, be);
                }
            });
        }
    }

    public record RequestLinksPayload(BlockPos plant) implements CustomPacketPayload {
        public static final Type<RequestLinksPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "request_links"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestLinksPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> buf.writeBlockPos(payload.plant()),
                buf -> new RequestLinksPayload(buf.readBlockPos())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(RequestLinksPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer sp
                        && sp.level().getBlockEntity(payload.plant()) instanceof GeothermalPlantBlockEntity be) {
                    syncPlantLinks(sp, be);
                }
            });
        }
    }

    public record ClearLinksPayload(BlockPos plant) implements CustomPacketPayload {
        public static final Type<ClearLinksPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Offense_armer.MODID, "clear_links"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ClearLinksPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> buf.writeBlockPos(payload.plant()),
                buf -> new ClearLinksPayload(buf.readBlockPos())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(ClearLinksPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer sp
                        && sp.level().getBlockEntity(payload.plant()) instanceof GeothermalPlantBlockEntity be) {
                    be.clearLinks();
                    syncPlantLinks(sp, be);
                }
            });
        }
    }
}
