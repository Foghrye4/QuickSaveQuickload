package quicksave_quickload;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import quicksave_quickload.gui.GuiEventHandler;

public class ClientProxy extends ServerProxy {
	
	@Override
	public void load() {
		MinecraftForge.EVENT_BUS.register(new GuiEventHandler());
	}

	@Override
	public File getMinecraftDir() {
		return Minecraft.getMinecraft().mcDataDir;
	}
	
	@Override
	public void preInit() {
	}
}
