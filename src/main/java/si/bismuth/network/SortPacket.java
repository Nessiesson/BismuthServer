package si.bismuth.network;

import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import si.bismuth.utils.InventoryHelper;

// Stolen from/based on code from https://github.com/kyrptonaught/Inventory-Sorter
@PacketChannelName("sort")
public class SortPacket extends BisPacket {
	private boolean isPlayerInv;

	public SortPacket() {
		// noop
	}

	@Override
	public void writePacketData() {
		// noop
	}

	@Override
	public void readPacketData(PacketByteBuf buf) {
		this.isPlayerInv = buf.readBoolean();
	}

	@Override
	public void processPacket(ServerPlayerEntity player) {
		if (player.isSpectator()) {
			return;
		}

		if (this.isPlayerInv) {
			InventoryHelper.sortInv(player.inventory, 9, 27);
		} else {
			final Inventory inv = player.menu.getSlot(0).inventory;
			InventoryHelper.sortInv(inv, 0, inv.getSize());
		}
	}
}
