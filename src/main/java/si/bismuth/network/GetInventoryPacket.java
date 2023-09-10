package si.bismuth.network;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.math.BlockPos;
import si.bismuth.MCServer;

@PacketChannelName("getinventory")
public class GetInventoryPacket extends BisPacket {
	private BlockPos pos;
	private DefaultedList<ItemStack> result;

	public GetInventoryPacket() {
		// noop
	}

	public GetInventoryPacket(DefaultedList<ItemStack> listIn) {
		this.result = listIn;
	}

	@Override
	public void writePacketData() {
		final PacketByteBuf buf = this.getPacketBuffer();
		buf.writeVarInt(this.result.size());
		for (ItemStack stack : this.result) {
			buf.writeItemStack(stack);
		}
	}

	@Override
	public void readPacketData(PacketByteBuf buf) {
		this.pos = buf.readBlockPos();
	}

	@Override
	public void processPacket(ServerPlayerEntity player) {
		final Inventory container = HopperBlockEntity.getInventoryAt(player.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
		// silence inspection since it falsely claims that container cannot be null. :(
		// noinspection ConstantConditions
		if (container == null) {
			return;
		}

		final DefaultedList<ItemStack> inventory = DefaultedList.of(container.getSize(), ItemStack.EMPTY);
		for (int i = 0; i < container.getSize(); i++) {
			inventory.set(i, container.getStack(i));
		}

		MCServer.pcm.sendPacketToPlayer(player, new GetInventoryPacket(inventory));
	}
}
