package quicksave_quickload.gui;

import static quicksave_quickload.QuickSaveQuickLoadMod.network;

import cubicchunks.world.ICubicWorld;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import quicksave_quickload.ClientNetworkHandler;

public class GuiEventHandler {

	private static final int SAVE_BUTTON_INDEX = 13;
	private static final int LOAD_BUTTON_INDEX = 14;

	@SubscribeEvent
	public void onGuiOpen(GuiScreenEvent.InitGuiEvent.Post event) {
		if (event.getGui() instanceof GuiIngameMenu) {
			ICubicWorld cworld = (ICubicWorld) Minecraft.getMinecraft().world;
			if(!cworld.isCubicWorld())
				return;
			GuiScreen gui = event.getGui();
			event.getButtonList().add(new GuiButton(SAVE_BUTTON_INDEX, gui.width / 2 - 100, gui.height / 4 + 144 + -16, 98, 20, I18n.format("quicksave_quickload.save")));
			event.getButtonList().add(new GuiButton(LOAD_BUTTON_INDEX, gui.width / 2 + 2, gui.height / 4 + 144 + -16, 98, 20, I18n.format("quicksave_quickload.load")));

		}
	}

	@SubscribeEvent
	public void onButtonPressed(GuiScreenEvent.ActionPerformedEvent.Post action) {
		if (action.getGui() instanceof GuiIngameMenu) {
			Minecraft mc = Minecraft.getMinecraft();
			switch (action.getButton().id) {
				case SAVE_BUTTON_INDEX :
					((ClientNetworkHandler)network).sendPacketSaveGame();
					mc.displayGuiScreen((GuiScreen) null);
					mc.setIngameFocus();
					break;
				case LOAD_BUTTON_INDEX :
					((ClientNetworkHandler)network).sendPacketLoadGame();
					mc.displayGuiScreen((GuiScreen) null);
					mc.setIngameFocus();
					break;
			}
		}
	}
}
