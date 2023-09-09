package si.bismuth.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.StringUtils;
import si.bismuth.MCServer;
import si.bismuth.scoreboard.IScoreboard;
import si.bismuth.scoreboard.IServerScoreboard;
import si.bismuth.scoreboard.LongScore;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class PluginChannelsManager {
	private static final String CHANNEL_SEPARATOR = "\u0000";
	private static final String REGISTER_CHANNELS = "REGISTER";
	private final Map<String, Class<? extends BisPacket>> allChannels = new HashMap<>();
	private final Map<UUID, List<String>> channelList = new HashMap<>();

	public PluginChannelsManager() {
		this.registerPacket(BisPacketGetInventory.class);
		this.registerPacket(BisPacketRegister.class);
		this.registerPacket(BisPacketSearchForItem.class);
		this.registerPacket(BisPacketSort.class);
		this.registerPacket(BisPacketUpdateScore.class);
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

	public void sendRegisterToPlayer(EntityPlayerMP player) {
		final String channels = StringUtils.join(this.allChannels.keySet(), CHANNEL_SEPARATOR);
		final SPacketCustomPayload packet = new SPacketCustomPayload(REGISTER_CHANNELS, new PacketBuffer(Unpooled.buffer().writeBytes(channels.getBytes())));
		player.connection.sendPacket(packet);
	}

	public void sendPacketToPlayer(EntityPlayerMP player, BisPacket packet) {
		//TODO: plz fix
		final String channel = this.getChannelFromPacket(packet);
		if (this.getChannelsForPlayer(player.getUniqueID()).contains(channel)) {
			packet.writePacketData();
			player.connection.sendPacket(new SPacketCustomPayload(channel, packet.getPacketBuffer()));
		}
	}

	public void processIncoming(EntityPlayerMP player, CPacketCustomPayload packetIn) {
		final UUID uuid = player.getUniqueID();
		final String channel = packetIn.getChannelName();
		final PacketBuffer data = packetIn.getBufferData();
		data.resetReaderIndex();

		if (channel.equals(REGISTER_CHANNELS)) {
			final List<String> incomingChannels = this.getChannelsFromBuffer(data);
			this.addChannelsForPlayer(uuid, incomingChannels);

			if (incomingChannels.contains(this.getChannelFromPacket(BisPacketUpdateScore.class))) {
				//TODO fix
				Set<ScoreObjective> set = Sets.newHashSet();
				WorldServer worldServer = player.getServerWorld();

				for (int i = 0; i < 19; ++i)
				{
					ScoreObjective scoreobjective = worldServer.getScoreboard().getObjectiveInDisplaySlot(i);

					if (scoreobjective != null && !set.contains(scoreobjective))
					{
						for (Packet<?> packet : ((ServerScoreboard)worldServer.getScoreboard()).getCreatePackets(scoreobjective))
						{
							player.connection.sendPacket(packet);
						}

						set.add(scoreobjective);
					}
				}
			}
		} else if (this.getChannelsForPlayer(uuid).contains(channel)) {
			try {
				final BisPacket packet = this.allChannels.get(channel).newInstance();
				packet.readPacketData(data);
				packet.processPacket(player);
			} catch (Exception ignored) {
				// meh, noop
			}
		}
	}

	private List<String> getChannelsFromBuffer(PacketBuffer data) {
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
