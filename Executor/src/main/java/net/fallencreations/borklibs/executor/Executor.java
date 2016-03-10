/*
 * Copyright (C) 2016 Goblom
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.fallencreations.borklibs.executor;

import com.google.common.collect.Lists;
import java.lang.reflect.Method;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Goblom
 */
@RequiredArgsConstructor
public abstract class Executor implements CommandExecutor {

    private final @NonNull String prefix;
    private final List<CommandData> commandMap = Lists.newArrayList();
    
    @Setter
    private boolean printErrors = false;
    
    public final void addListener(CommandListener listener) {
        Class<?> clazz = listener.getClass();
        
        while (clazz != null) {
            for (Method method : clazz.getMethods()) {
                Command subCommand = method.getAnnotation(Command.class);
                if (subCommand != null) {
                    commandMap.add(new CommandData(method.getName(), subCommand.alias(), method, listener));
                }
            }

            clazz = clazz.getSuperclass();
        }
    }
    
    private final CommandData getDataForName(String name) {
        for (CommandData data : commandMap) {
            if (data.getLongName().equalsIgnoreCase(name) || data.isAlias(name)) {
                return data;
            }
        }
        
        return null;
    }
    
    @Override
    public final boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length == 0 || (args.length >= 1 && args[0].equalsIgnoreCase("help"))) {
            try {
                if (args[0].equalsIgnoreCase("help") && !args[1].isEmpty()) {
                    CommandData data = getDataForName(args[1]);
                    Command cmd = data.getMethod().getAnnotation(Command.class);
                    
                    if (cmd.help().length != 0) {
                        sendMessage(sender, "Usage: /" + command.getName() + " " + args[1] + " " + cmd.usage());
                        for (String line : cmd.help()) {
                            sendMessage(sender, line);
                        }
                    } else {
                        sendMessage(sender, ChatColor.RED + "That command does not have any help.");
                    }
                }
                
                return true;
            } catch (Exception e) { if (printErrors) e.printStackTrace(); }
            List<CommandData> commands = Lists.newArrayList();
            
            sendMessage(sender, ChatColor.GOLD + "Available Commands:");
            commandMap.stream().filter((data) -> !(commands.contains(data))).forEach((data) -> {
                Command cmd = data.getMethod().getAnnotation(Command.class);
                
                if (hasPermission(sender, cmd.permissions())) {
                    sendMessage(sender, "/" + command.getName() + " " + data.getMethod().getName().toLowerCase() /*(cmd.alias().isEmpty() ? data.getMethod().getName().toLowerCase() : cmd.alias())*/ + " " + cmd.usage() + ChatColor.BLUE + " " + cmd.description());
                }
            });
        } else {
            CommandData data = getDataForName(args[0]);
            if (data == null) {
                sendMessage(sender, ChatColor.RED + "Error: That command does not exist");
                return true;
            }
            
            Method method = data.getMethod();
            CommandHandler handler = new CommandHandler(prefix, sender, args);
            Command cmd = method.getAnnotation(Command.class);
            
            if (cmd.allowConsole() || handler.isSenderPlayer()) {
                if (hasPermission(sender, cmd.permissions())) {
                    if (args.length > cmd.minArgs()) {
                        try {
                            method.invoke(data.getListener(), handler);
                        } catch (Throwable t) {
                            sendMessage(sender, ChatColor.RED + "Error: " + t.getMessage());
                            if (printErrors) t.printStackTrace();
                        }
                    } else {
                        sendMessage(sender, ChatColor.RED + "Error: Not enough arguments. Use /" + command.getName() + " for help");
                    }
                } else {
                    sendMessage(sender, ChatColor.RED + "Error: Invalid Permissions. Please contact your server admin if you feel this an error.");
                }
            } else {
                sendMessage(sender, ChatColor.RED + "Error: Only players can use this command.");
            }
            
        }
        
        return true;
    }
    
    public final void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(color(prefix + " " + message));
    }
    
    @AllArgsConstructor
    @Data
    private static final class CommandData {
        private final @NonNull String longName;
        private final @NonNull String[] alias;
        private final @NonNull Method method;
        private final @NonNull CommandListener listener;
        
        private boolean isAlias(String str) {
            if (alias.length == 1) return alias[0].equalsIgnoreCase(str);
            
            boolean is = false;
            for (String a : alias) {                
                if (is = a.equalsIgnoreCase(str)) break;
            }
            
            return is;
        }
        
        @Override
        public int hashCode() {
            return (method.getName() + listener.getClass().getCanonicalName()).hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CommandData) {
                return this.hashCode() == obj.hashCode();
            }
            
            return false;
        }
        
        @Override
        public String toString() {
            return method.getDeclaringClass().getCanonicalName() + ":" + method.getName();
        }
    }
    
    private String color(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
    
    public abstract boolean hasPermission(CommandSender sender, String[] permissions);
}
