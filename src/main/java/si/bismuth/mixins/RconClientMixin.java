package si.bismuth.mixins;

import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import si.bismuth.MCServer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import net.minecraft.server.dedicated.IDedicatedServer;
import net.minecraft.server.rcon.BufferHelper;
import net.minecraft.server.rcon.RconBase;
import net.minecraft.server.rcon.RconClient;

@Mixin(RconClient.class)
public class RconClientMixin extends RconBase {
	// @formatter:off
	@Shadow private boolean authenticated;
	@Shadow private Socket socket;
	@Shadow @Final private byte[] packetBuffer;
	@Shadow @Final private String password;
	@Shadow @Final private static Logger LOGGER;
	@Shadow private void close() { }
	@Shadow private void executeUnknown() { }
	@Shadow private void execute(int id, int type, String message) { }
	@Shadow private void execute(int id, String message) { }
	// @formatter:on

	protected RconClientMixin(IDedicatedServer server, String thread) {
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

				final BufferedInputStream stream = new BufferedInputStream(this.socket.getInputStream());
				final int i = stream.read(this.packetBuffer, 0, this.packetBuffer.length);
				if (i == -1) {
					break;
				}

				if (i >= 10) {
					final int k = BufferHelper.getIntLE(this.packetBuffer, 0, i);
					if (k != i - 4) {
						break;
					}

					final int id = BufferHelper.getIntLE(this.packetBuffer, 4, i);
					final int type = BufferHelper.getIntLE(this.packetBuffer, 8);
					switch (type) {
						case 2:
							if (this.authenticated) {
								final String command = BufferHelper.getString(this.packetBuffer, 12, i);

								MCServer.server.submit(() -> {
									try {
										execute(id, server.runRconCommand(command));
									} catch (Exception exception) {
										execute(id, String.format("Error executing: %s (%s)", command, exception.getMessage()));
									}
								});

								continue;
							}

							this.executeUnknown();
							break;
						case 3:
							final String s = BufferHelper.getString(this.packetBuffer, 12, i);
							if (!s.isEmpty() && s.equals(this.password)) {
								this.authenticated = true;
								this.execute(id, 2, "");
								continue;
							}

							this.authenticated = false;
							this.executeUnknown();
							break;
						default:
							this.execute(id, String.format("Unknown request %s", Integer.toHexString(type)));
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
		this.close();
	}
}
