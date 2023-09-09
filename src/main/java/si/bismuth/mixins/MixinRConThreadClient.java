package si.bismuth.mixins;

import net.minecraft.network.rcon.IServer;
import net.minecraft.network.rcon.RConThreadBase;
import net.minecraft.network.rcon.RConThreadClient;
import net.minecraft.network.rcon.RConUtils;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import si.bismuth.MCServer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

@Mixin(RConThreadClient.class)
public abstract class MixinRConThreadClient extends RConThreadBase {
	// @formatter:off
	@Shadow private boolean loggedIn;
	@Shadow private Socket clientSocket;
	@Shadow @Final private byte[] buffer;
	@Shadow @Final private String rconPassword;
	@Shadow @Final private static Logger LOGGER;
	@Shadow protected abstract void closeSocket();
	@Shadow protected abstract void sendLoginFailedResponse();
	@Shadow protected abstract void sendResponse(int id, int type, String message);
	@Shadow protected abstract void sendMultipacketResponse(int id, String message);
	// @formatter:on

	protected MixinRConThreadClient(IServer server, String thread) {
		super(server, thread);
	}

	/**
	 * @author nessie
	 * @reason Fixes MC-72390
	 * @author
	 */
	@Overwrite
	public void run() {
		while (true) {
			try {
				if (!this.running) {
					break;
				}

				final BufferedInputStream stream = new BufferedInputStream(this.clientSocket.getInputStream());
				final int i = stream.read(this.buffer, 0, this.buffer.length);
				if (i == -1) {
					break;
				}

				if (i >= 10) {
					final int k = RConUtils.getBytesAsLEInt(this.buffer, 0, i);
					if (k != i - 4) {
						break;
					}

					final int id = RConUtils.getBytesAsLEInt(this.buffer, 4, i);
					final int type = RConUtils.getRemainingBytesAsLEInt(this.buffer, 8);
					switch (type) {
						case 2:
							if (this.loggedIn) {
								final String command = RConUtils.getBytesAsString(this.buffer, 12, i);

								MCServer.server.addScheduledTask(() -> {
									try {
										sendMultipacketResponse(id, server.handleRConCommand(command));
									} catch (Exception exception) {
										sendMultipacketResponse(id, String.format("Error executing: %s (%s)", command, exception.getMessage()));
									}
								});

								continue;
							}

							this.sendLoginFailedResponse();
							this.logInfo("Authorization failed on RCON connection from " + this.clientSocket.getInetAddress());
							break;
						case 3:
							final String s = RConUtils.getBytesAsString(this.buffer, 12, i);
							if (!s.isEmpty() && s.equals(this.rconPassword)) {
								this.loggedIn = true;
								this.sendResponse(id, 2, "");
								continue;
							}

							this.loggedIn = false;
							this.sendLoginFailedResponse();
							this.logInfo("Authorization failed on RCON connection from " + this.clientSocket.getInetAddress());
							break;
						default:
							this.sendMultipacketResponse(id, String.format("Unknown request %s", Integer.toHexString(type)));
							this.logInfo(String.format("Unknown RCON request %s from " + this.clientSocket.getInetAddress(), Integer.toHexString(type)));
							break;
					}
				}
			} catch (IOException exception) {
				break;
			} catch (Exception exception) {
				LOGGER.error("Exception whilst parsing RCON input", exception);
				break;
			}
		}
		this.closeSocket();
	}
}
