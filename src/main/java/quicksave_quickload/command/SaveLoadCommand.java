package quicksave_quickload.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import quicksave_quickload.QuickSaveQuickLoadMod;
import quicksave_quickload.handler.SaveLoadHandler;

public class SaveLoadCommand extends CommandBase {

	@Override
	public String getName() {
		return "q";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/q [save|load]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1)
			throw new WrongUsageException(this.getUsage(sender), new Object[0]);
		if (args[0].equalsIgnoreCase("save"))
			QuickSaveQuickLoadMod.eventHandler.save(sender.getCommandSenderEntity());
		else if (args[0].equalsIgnoreCase("load"))
			QuickSaveQuickLoadMod.eventHandler.scheduleLoadOnNextTick(sender.getCommandSenderEntity());
		else
			throw new WrongUsageException(this.getUsage(sender), new Object[0]);
	}
}
