package si.bismuth.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.BlockPos;
import si.bismuth.utils.InventoryHelper;

import java.io.IOException;

@PacketChannelName("searchforitem")
public class SearchForItemPacket extends BisPacket {
	private ItemStack stack;
	private DefaultedList<BlockPos> result;

	public SearchForItemPacket() {
		// noop
	}

	public SearchForItemPacket(DefaultedList<BlockPos> listIn) {
		this.result = listIn;
	}

	@Override
	public void writePacketData() {
		final PacketByteBuf buf = this.getPacketBuffer();
		buf.writeVarInt(this.result.size());
		for (BlockPos pos : this.result) {
			buf.writeBlockPos(pos);
		}
	}

	@Override
	public void readPacketData(PacketByteBuf buf) throws IOException {
		this.stack = buf.readItemStack();
	}

	@Override
	public void processPacket(ServerPlayerEntity player) {
		InventoryHelper.processFindItem(player, this.stack);
	}
}
