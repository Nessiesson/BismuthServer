package si.bismuth.patches;

import net.minecraft.network.Connection;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.text.Text;

public class NetHandlerPlayServerFake extends ServerPlayNetworkHandler {
	public NetHandlerPlayServerFake(MinecraftServer server, Connection nm, ServerPlayerEntity playerIn) {
		super(server, nm, playerIn);
	}

	public void sendPacket(final Packet<?> packetIn) {
	}

	public void sendDisconnect(final Text textComponent) {
		this.player.discard();
	}
}



