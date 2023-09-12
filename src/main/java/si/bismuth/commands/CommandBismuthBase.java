package si.bismuth.commands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.source.CommandSource;

abstract class CommandBismuthBase extends AbstractCommand {
	@Override
	public boolean canUse(MinecraftServer server, CommandSource sender) {
		return true;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
}
