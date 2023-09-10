package si.bismuth.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;

@PacketChannelName("register")
public class BisPacketRegister extends BisPacket {
	private static final int CURRENT_BISMUTH_PROTOCOL_ID = 1;

	public BisPacketRegister() {
		// noop
	}

	@Override
	public void writePacketData() {
		final PacketByteBuf buf = this.getPacketBuffer();
		buf.writeVarInt(CURRENT_BISMUTH_PROTOCOL_ID);
	}

	@Override
	public void readPacketData(PacketByteBuf buf) {
		// noop
	}

	@Override
	public void processPacket(ServerPlayerEntity player) {
		// noop
	}
}
