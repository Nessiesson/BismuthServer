package si.bismuth.network;

import si.bismuth.MCServer;

import java.util.Arrays;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;

@PacketChannelName(value = "carpet:client", isCustom = true)
public class FakeCarpetClientSupport extends BisPacket {
	public FakeCarpetClientSupport() {
		// noop
	}

	@Override
	public void writePacketData() {
		final PacketByteBuf buf = this.getPacketBuffer();
		final NbtCompound nbt = new NbtCompound();
		nbt.putString("carpetVersion", "bismuth");
		nbt.putFloat("tickrate", 20F);
		final NbtCompound ctrlQCrafting = new NbtCompound();
		ctrlQCrafting.putString("rule", "ctrlQCrafting");
		ctrlQCrafting.putString("current", "true");
		ctrlQCrafting.putString("default", "true");
		ctrlQCrafting.putBoolean("isfloat", false);
		final NbtList carpetRules = new NbtList();
		carpetRules.add(ctrlQCrafting);
		nbt.put("ruleList", carpetRules);
		buf.writeInt(1);
		buf.writeNbtCompound(nbt);
	}

	@Override
	public void readPacketData(PacketByteBuf buf) {
		System.out.println(Arrays.toString(buf.readByteArray()));
		// noop
	}

	@Override
	public void processPacket(ServerPlayerEntity player) {
		MCServer.pcm.sendPacketToPlayer(player, new FakeCarpetClientSupport());
	}
}
