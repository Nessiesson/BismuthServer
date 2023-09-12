package si.bismuth.network;

import com.google.common.collect.Lists;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.StringUtils;
import si.bismuth.MCServer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;

public class PluginChannelsManager {
	private static final String CHANNEL_SEPARATOR = "\u0000";
	private static final String REGISTER_CHANNELS = "REGISTER";
	private final Map<String, Class<? extends BisPacket>> allChannels = new HashMap<>();
	private final Map<UUID, List<String>> channelList = new HashMap<>();

	public PluginChannelsManager() {
		this.registerPacket(GetInventoryPacket.class);
		this.registerPacket(RegisterPacket.class);
		this.registerPacket(SearchForItemPacket.class);
		this.registerPacket(SortPacket.class);
		this.registerPacket(FakeCarpetClientSupport.class);
	}

	private void registerPacket(Class<? extends BisPacket> clazz) {
		if (!clazz.isAnnotationPresent(PacketChannelName.class)) {
			MCServer.log.error("Packet {} lacks plugin channel annotation.", clazz.getSimpleName());
			return;
		}

		final String channel = this.getChannelFromPacket(clazz);
		if (this.allChannels.containsKey(channel)) {
			MCServer.log.error("Packet {} attempted to register packet on channel '{}' but it already exists!", clazz.getSimpleName(), channel);
		} else {
			this.allChannels.put(channel, clazz);
		}
	}

	public void sendRegisterToPlayer(ServerPlayerEntity player) {
		final String channels = StringUtils.join(this.allChannels.keySet(), CHANNEL_SEPARATOR);
		final CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(REGISTER_CHANNELS, new PacketByteBuf(Unpooled.buffer().writeBytes(channels.getBytes())));
		player.networkHandler.sendPacket(packet);
	}

	public void sendPacketToPlayer(ServerPlayerEntity player, BisPacket packet) {
		//TODO: plz fix
		final String channel = this.getChannelFromPacket(packet);
		if (this.getChannelsForPlayer(player.getUuid()).contains(channel)) {
			packet.writePacketData();
			player.networkHandler.sendPacket(new CustomPayloadS2CPacket(channel, packet.getPacketBuffer()));
		}
	}

	public boolean processIncoming(ServerPlayerEntity player, CustomPayloadC2SPacket packetIn) {
		final UUID uuid = player.getUuid();
		final String channel = packetIn.getChannel();
		final PacketByteBuf data = packetIn.getData();
		data.resetReaderIndex();

		if (channel.equals(REGISTER_CHANNELS)) {
			final List<String> incomingChannels = this.getChannelsFromBuffer(data);
			this.addChannelsForPlayer(uuid, incomingChannels);

			return true;
		} else if (this.getChannelsForPlayer(uuid).contains(channel)) {
			try {
				final BisPacket packet = this.allChannels.get(channel).newInstance();
				packet.readPacketData(data);
				packet.processPacket(player);

				return true;
			} catch (Exception ignored) {
				// meh, noop
			}
		}

		return false;
	}

	private List<String> getChannelsFromBuffer(PacketByteBuf data) {
		final byte[] bytes = new byte[data.readableBytes()];
		data.readBytes(bytes);
		final String channels = new String(bytes, StandardCharsets.UTF_8);
		return Lists.newArrayList(channels.split(CHANNEL_SEPARATOR));
	}

	private void addChannelsForPlayer(UUID player, List<String> channels) {
		this.getChannelsForPlayer(player).addAll(channels);
	}

	private List<String> getChannelsForPlayer(UUID player) {
		final Map<UUID, List<String>> channels = this.channelList;
		if (!channels.containsKey(player)) {
			channels.put(player, new ArrayList<>());
		}

		return channels.get(player);
	}

	private String getChannelFromPacket(BisPacket packet) {
		return this.getChannelFromPacket(packet.getClass());
	}

	private String getChannelFromPacket(Class<? extends BisPacket> clazz) {
		PacketChannelName annotation = clazz.getDeclaredAnnotation(PacketChannelName.class);
		return (annotation.isCustom() ? "" : "Bis|") + annotation.value();
	}
}
