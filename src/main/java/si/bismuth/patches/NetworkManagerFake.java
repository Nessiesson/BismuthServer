package si.bismuth.patches;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketFlow;

public class NetworkManagerFake extends Connection {
	NetworkManagerFake() {
		super(PacketFlow.CLIENTBOUND);
	}

	public void disableAutoRead() {
	}
}
