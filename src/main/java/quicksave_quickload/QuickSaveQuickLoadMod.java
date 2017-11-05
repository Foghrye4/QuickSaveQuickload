package quicksave_quickload;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import quicksave_quickload.command.SaveLoadCommand;
import quicksave_quickload.event.QuickSaveQuickLoadEventHandler;

@Mod(modid = QuickSaveQuickLoadMod.MODID, name = QuickSaveQuickLoadMod.NAME, version = QuickSaveQuickLoadMod.VERSION, dependencies = "required-after:cubicchunks")
public class QuickSaveQuickLoadMod {
	public static final String MODID = "quicksave_quickload";
	public static final String NAME = "Quicksave-quickload";
	public static final String VERSION = "0.1.2";

	public static Logger log;
	public static QuickSaveQuickLoadEventHandler eventHandler = new QuickSaveQuickLoadEventHandler();
	@SidedProxy(clientSide = "quicksave_quickload.ClientProxy", serverSide = "quicksave_quickload.ServerProxy")
	public static ServerProxy proxy;
	@SidedProxy(clientSide = "quicksave_quickload.ClientNetworkHandler", serverSide = "quicksave_quickload.ServerNetworkHandler")
	public static ServerNetworkHandler network;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) throws IOException, IllegalAccessException {
		log = event.getModLog();
		MinecraftForge.EVENT_BUS.register(eventHandler);
		MinecraftForge.EVENT_BUS.register(network);
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.load();
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event) {
		network.setServer(event.getServer());
		proxy.setServer(event.getServer());
		event.registerServerCommand(new SaveLoadCommand());
	}
}
