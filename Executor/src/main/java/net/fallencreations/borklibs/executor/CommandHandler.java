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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Goblom
 */
@AllArgsConstructor
public class CommandHandler {
    @Setter
    private String prefix;
        
    @Getter
    private final CommandSender commandSender;
    
    @Getter
    private final String[] args;
    
    public String getArg(int index) {
        try {
            return args[index + 1];
        } catch (Exception e) { }
        return "";
    }
    
    public boolean getBoolean(int index, boolean def) {
        try {
            return Boolean.valueOf(getArg(index));
        } catch (Exception e) { }
        
        return def;
    }
    
    public int getArgsLength() {
        return getArgs().length - 1;
    }
    
    public String combine(int start, char spacer) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = start; i < getArgsLength(); i++) {
            sb.append(getArg(i)).append(spacer);
        }
        
        return sb.toString();
    }
    
    public boolean isSenderPlayer() {
        return commandSender instanceof Player;
    }
    
    public Player getPlayer() {
        return (Player) commandSender;
    }
    
    public void reply(String message, Object... info) {
        String p = prefix == null ? "" : prefix;
        message = p + " " + message;
        
        getCommandSender().sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(message, info)));
    } 
}
