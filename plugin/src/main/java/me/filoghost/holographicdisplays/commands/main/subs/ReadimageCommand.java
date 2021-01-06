/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.holographicdisplays.commands.main.subs;

import me.filoghost.holographicdisplays.Colors;
import me.filoghost.holographicdisplays.commands.CommandValidator;
import me.filoghost.holographicdisplays.commands.Messages;
import me.filoghost.holographicdisplays.commands.main.HologramSubCommand;
import me.filoghost.holographicdisplays.Permissions;
import me.filoghost.holographicdisplays.disk.HologramDatabase;
import me.filoghost.holographicdisplays.event.NamedHologramEditedEvent;
import me.filoghost.holographicdisplays.exception.CommandException;
import me.filoghost.holographicdisplays.exception.TooWideException;
import me.filoghost.holographicdisplays.exception.UnreadableImageException;
import me.filoghost.holographicdisplays.image.ImageMessage;
import me.filoghost.holographicdisplays.object.NamedHologram;
import me.filoghost.holographicdisplays.object.line.CraftTextLine;
import me.filoghost.holographicdisplays.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReadimageCommand extends HologramSubCommand {


    public ReadimageCommand() {
        super("readimage", "image");
        setPermission(Permissions.COMMAND_BASE + "readimage");
    }

    @Override
    public String getPossibleArguments() {
        return "<hologram> <imageWithExtension> <width>";
    }

    @Override
    public int getMinimumArguments() {
        return 3;
    }


    @Override
    public void execute(CommandSender sender, String label, String[] args) throws CommandException {
        
        boolean append = false;
        
        List<String> newArgs = new ArrayList<>();

        for (String arg : args) {
            if (arg.equalsIgnoreCase("-a") || arg.equalsIgnoreCase("-append")) {
                append = true;
            } else {
                newArgs.add(arg);
            }
        }
        
        args = newArgs.toArray(new String[0]);
        
        NamedHologram hologram = CommandValidator.getNamedHologram(args[0]);
        
        int width = CommandValidator.getInteger(args[2]);
        
        CommandValidator.isTrue(width >= 2, "The width of the image must be 2 or greater.");

        boolean isUrl = false;
        
        try {
            String fileName = args[1];
            BufferedImage image;
            
            if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
                isUrl = true;
                image = FileUtils.readImage(new URL(fileName));
            } else {
                
                if (fileName.matches(".*[a-zA-Z0-9\\-]+\\.[a-zA-Z0-9\\-]{1,4}\\/.+")) {
                    Messages.sendWarning(sender, "The image path seems to be an URL. If so, please use http:// or https:// in the path.");
                }

                File targetImage = CommandValidator.getUserReadableFile(fileName);
                image = FileUtils.readImage(targetImage);
            }
            
            if (!append) {
                hologram.clearLines();
            }
            
            ImageMessage imageMessage = new ImageMessage(image, width);
            String[] newLines = imageMessage.getLines();
            for (String newLine : newLines) {
                CraftTextLine line = new CraftTextLine(hologram, newLine);
                line.setSerializedConfigValue(newLine);
                hologram.getLinesUnsafe().add(line);
            }
            
            hologram.refreshAll();
            
            if (newLines.length < 5) {
                Messages.sendTip(sender, "The image has a very low height. You can increase it by increasing the width, it will scale automatically.");
            }
            
            HologramDatabase.saveHologram(hologram);
            HologramDatabase.trySaveToDisk();
            
            if (append) {
                sender.sendMessage(Colors.PRIMARY + "The image was appended int the end of the hologram!");
            } else {
                sender.sendMessage(Colors.PRIMARY + "The image was drawn in the hologram!");
            }
            Bukkit.getPluginManager().callEvent(new NamedHologramEditedEvent(hologram));
            
        } catch (MalformedURLException e) {
            throw new CommandException("The provided URL was not valid.");
        } catch (TooWideException e) {
            throw new CommandException("The image is too large. Max width allowed is " + ImageMessage.MAX_WIDTH + " pixels.");
        } catch (UnreadableImageException e) {
            throw new CommandException("The plugin was unable to read the image. Be sure that the format is supported.");
        } catch (FileNotFoundException e) {
            throw new CommandException("The image \"" + args[1] + "\" doesn't exist in the plugin's folder.");
        } catch (IOException e) {
            e.printStackTrace();
            throw new CommandException("I/O exception while reading the image. " + (isUrl ? "Is the URL valid?" : "Is it in use?"));
        }
    }
    
    @Override
    public List<String> getTutorial() {
        return Arrays.asList("Reads an image from a file. Tutorial:",
                "1) Move the image in the plugin's folder",
                "2) Do not use spaces in the name",
                "3) Do /holograms read <hologram> <image> <width>",
                "4) Choose <width> to automatically resize the image",
                "5) (Optional) Use the flag '-a' if you only want to append",
                "   the image to the hologram without clearing the lines",
                "",
                "Example: you have an image named 'logo.png', you want to append",
                "it to the lines of the hologram named 'test', with a width of",
                "50 pixels. In this case you would execute the following command:",
                ChatColor.YELLOW + "/holograms readimage test logo.png 50 -a",
                "",
                "The symbols used to create the image are taken from the config.yml.");
    }
    
    @Override
    public SubCommandType getType() {
        return SubCommandType.EDIT_LINES;
    }

}
