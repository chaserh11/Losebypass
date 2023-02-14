/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 */
package net.minecraft.command.server;

import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.util.BlockPos;

public class CommandBanPlayer
extends CommandBase {
    @Override
    public String getCommandName() {
        return "ban";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.ban.usage";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        if (!MinecraftServer.getServer().getConfigurationManager().getBannedPlayers().isLanServer()) return false;
        if (!super.canCommandSenderUseCommand(sender)) return false;
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) throw new WrongUsageException("commands.ban.usage", new Object[0]);
        if (args[0].length() <= 0) throw new WrongUsageException("commands.ban.usage", new Object[0]);
        MinecraftServer minecraftserver = MinecraftServer.getServer();
        GameProfile gameprofile = minecraftserver.getPlayerProfileCache().getGameProfileForUsername(args[0]);
        if (gameprofile == null) {
            throw new CommandException("commands.ban.failed", args[0]);
        }
        String s = null;
        if (args.length >= 2) {
            s = CommandBanPlayer.getChatComponentFromNthArg(sender, args, 1).getUnformattedText();
        }
        UserListBansEntry userlistbansentry = new UserListBansEntry(gameprofile, (Date)null, sender.getName(), (Date)null, s);
        minecraftserver.getConfigurationManager().getBannedPlayers().addEntry(userlistbansentry);
        EntityPlayerMP entityplayermp = minecraftserver.getConfigurationManager().getPlayerByUsername(args[0]);
        if (entityplayermp != null) {
            entityplayermp.playerNetServerHandler.kickPlayerFromServer("You are banned from this server.");
        }
        CommandBanPlayer.notifyOperators(sender, (ICommand)this, "commands.ban.success", args[0]);
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length < 1) return null;
        List<String> list = CommandBanPlayer.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        return list;
    }
}

