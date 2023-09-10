package si.bismuth.patches;

import net.minecraft.network.Connection;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.text.Text;

public class FakeServerPlayNetworkHandler extends ServerPlayNetworkHandler {
	public FakeServerPlayNetworkHandler(MinecraftServer server, Connection connection, ServerPlayerEntity player) {
		super(server, connection, player);
	}

	public void sendPacket(final Packet<?> packet) {
	}

	public void sendDisconnect(final Text reason) {
		this.player.discard();
	}
}



