package si.bismuth.network;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;

public abstract class BisPacket {
	private PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());

	public abstract void writePacketData();

	public abstract void readPacketData(PacketByteBuf buf) throws IOException;

	public abstract void processPacket(ServerPlayerEntity player);

	public PacketByteBuf getPacketBuffer() {
		return this.data;
	}
}
