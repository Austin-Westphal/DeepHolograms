/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.holographicdisplays.commands.main.subs;

import com.google.common.collect.Lists;
import me.filoghost.holographicdisplays.HolographicDisplays;
import me.filoghost.holographicdisplays.Colors;
import me.filoghost.holographicdisplays.commands.CommandValidator;
import me.filoghost.holographicdisplays.commands.Messages;
import me.filoghost.holographicdisplays.commands.main.HologramSubCommand;
import me.filoghost.holographicdisplays.Permissions;
import me.filoghost.holographicdisplays.disk.Configuration;
import me.filoghost.holographicdisplays.exception.CommandException;
import me.filoghost.holographicdisplays.object.NamedHologram;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditCommand extends HologramSubCommand {
    
    private static final List<QuickCommandInfo> QUICK_EDIT_COMMANDS = Lists.newArrayList(
        new QuickCommandInfo("Add", AddlineCommand.class),
        new QuickCommandInfo("Remove", RemovelineCommand.class),
        new QuickCommandInfo("Set", SetlineCommand.class),
        new QuickCommandInfo("Insert", InsertlineCommand.class),
        new QuickCommandInfo("View", InfoCommand.class)
    );
    
    public EditCommand() {
        super("edit");
        setPermission(Permissions.COMMAND_BASE + "edit");
    }

    @Override
    public String getPossibleArguments() {
        return "<hologramName>";
    }

    @Override
    public int getMinimumArguments() {
        return 1;
    }


    @Override
    public void execute(CommandSender sender, String label, String[] args) throws CommandException {
        NamedHologram hologram = CommandValidator.getNamedHologram(args[0]);
        
        sender.sendMessage("");
        Messages.sendTitle(sender, "How to edit the hologram '" + hologram.getName() + "'");
        for (HologramSubCommand subCommand : HolographicDisplays.getCommandHandler().getSubCommands()) {
            if (subCommand.getType() == SubCommandType.EDIT_LINES) {
                String usage = "/" + label + " " + subCommand.getName() + (subCommand.getPossibleArguments().length() > 0 ? " " + subCommand.getPossibleArguments().replace("<hologramName>", hologram.getName()).replace("<hologram>", hologram.getName()) : "");

                if (sender instanceof Player) {
                    
                    List<String> help = new ArrayList<>();
                    help.add(Colors.PRIMARY + usage);
                    for (String tutLine : subCommand.getTutorial()) {
                        help.add(Colors.SECONDARY_SHADOW + tutLine);
                    }
                    
                    ((Player) sender).spigot().sendMessage(new ComponentBuilder(usage)
                        .color(ChatColor.AQUA)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, usage))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(String.join("\n", help))))
                        .create());
                    
                } else {
                    sender.sendMessage(Colors.PRIMARY + usage);
                }
            }
        }

        if (sender instanceof Player) {
            HelpCommand.sendHoverTip((Player) sender);
        }
    }
    
    public static void sendQuickEditCommands(CommandSender sender, String label, String hologramName) {
        if (!Configuration.quickEditCommands) {
            return;
        }
        if (!(sender instanceof Player)) {
            return;
        }
        
        ComponentBuilder message = new ComponentBuilder("EDIT LINES:").color(ChatColor.GRAY).bold(true).append("  ", FormatRetention.NONE);
        
        for (QuickCommandInfo quickEditCommand : QUICK_EDIT_COMMANDS) {
            HologramSubCommand subCommand = HolographicDisplays.getCommandHandler().getSubCommand(quickEditCommand.commandClass);
                
                // Assume first argument is always "<hologram>" and remove it
                String arguments = subCommand.getPossibleArguments();
                if (arguments.contains(" ")) {
                    arguments = arguments.substring(arguments.indexOf(" ") + 1);
                } else {
                    arguments = "";
                }
                
                String usage = "/" + label + " " + subCommand.getName() + " " + hologramName + " ";
                message.append("[" + quickEditCommand.chatName + "]").color(ChatColor.DARK_AQUA)
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, usage))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                        ChatColor.GRAY + "Click to insert in chat the highlighted part of the command:\n" +
                        ChatColor.YELLOW + usage + ChatColor.DARK_GRAY + arguments)));
                message.append("  ", FormatRetention.NONE);
        }
        
        ((Player) sender).spigot().sendMessage(message.create());
    }

    @Override
    public List<String> getTutorial() {
        return Arrays.asList("Shows the commands to manipulate an existing hologram.");
    }
    
    @Override
    public SubCommandType getType() {
        return SubCommandType.GENERIC;
    }
    
    
    
    private static class QuickCommandInfo {
        
        private final String chatName;
        private final Class<? extends HologramSubCommand> commandClass;
        
        public QuickCommandInfo(String chatName, Class<? extends HologramSubCommand> command) {
            this.chatName = chatName;
            this.commandClass = command;
        }    
        
    }

}
