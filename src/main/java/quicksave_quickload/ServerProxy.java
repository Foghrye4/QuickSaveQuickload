package quicksave_quickload;

import java.io.File;

import net.minecraft.server.MinecraftServer;

public class ServerProxy {

	private MinecraftServer server;

	public void load() {
	}

	public File getMinecraftDir() {
		return new File(".");
	}

	public void preInit() {
	}

	public void setServer(MinecraftServer serverIn) {
		this.server = serverIn;
	}

	public MinecraftServer getServer() {
		return server;
	}
}
