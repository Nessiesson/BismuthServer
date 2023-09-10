package si.bismuth.patches;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketFlow;

public class FakeConnection extends Connection {
	FakeConnection() {
		super(PacketFlow.CLIENTBOUND);
	}

	@Override
	public void disableAutoRead() {
	}
}
