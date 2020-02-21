/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.autobroadcast;

import java.util.Arrays;

import net.cubespace.yamler.YamlerConfig;
import net.cubespace.yamler.YamlerConfigurationException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.gmail.filoghost.autobroadcast.config.MessagesConfig;
import com.google.common.base.Joiner;

import wild.api.command.CommandFramework;
import wild.api.command.CommandFramework.Permission;

@Permission("autobroadcast.admin")
public class CommandHandler extends CommandFramework {
	
	private AutoBroadcast plugin;

	public CommandHandler(AutoBroadcast plugin, String label) {
		super(plugin, label);
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		
		if (args.length == 0) {
			sender.sendMessage(ChatColor.GREEN + "Comandi AutoBroadcast:");
			sender.sendMessage(ChatColor.GRAY + "/" + label + " list");
			sender.sendMessage(ChatColor.GRAY + "/" + label + " send <numero>");
			sender.sendMessage(ChatColor.GRAY + "/" + label + " add <messaggio>");
			sender.sendMessage(ChatColor.GRAY + "/" + label + " remove <numero>");
			sender.sendMessage(ChatColor.GRAY + "/" + label + " reload");
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("list")) {
			CommandValidate.isTrue(!plugin.getMessages().isEmpty(), "Non ci sono ancora messaggi automatici.");
			
			sender.sendMessage(ChatColor.GREEN + "Lista messaggi automatici:");
			int index = 1;
			for (String message : plugin.getMessages()) {
				sender.sendMessage(ChatColor.GRAY + "" + (index++) + ". " + ChatColor.RESET + message);
			}
			
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("send")) {
			CommandValidate.minLength(args, 2, "Utilizzo comando: /" + label + " send <numero>");
			int index0 = CommandValidate.getPositiveIntegerNotZero(args[1]) - 1;
			CommandValidate.isTrue(index0 < plugin.getMessages().size(), "Inserisci un numero minore o uguale a " + plugin.getMessages().size());
			
			plugin.broadcastFormatted(plugin.getMessages().get(index0));
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("add")) {
			CommandValidate.minLength(args, 2, "Utilizzo comando: /" + label + " add <messaggio>");
			String newMessage = Joiner.on(" ").join(Arrays.copyOfRange(args, 1, args.length));
			
			MessagesConfig messagesConfig = new MessagesConfig(plugin);
			tryLoad(messagesConfig);
			messagesConfig.messages.add(newMessage);
			trySave(messagesConfig);
			
			tryReload();
			sender.sendMessage(ChatColor.GREEN + "Aggiunto messaggio: " + ChatColor.RESET + plugin.format(newMessage));
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("remove")) {
			CommandValidate.minLength(args, 2, "Utilizzo comando: /" + label + " remove <numero>");
			int index0 = CommandValidate.getPositiveIntegerNotZero(args[1]) - 1;
			
			MessagesConfig messagesConfig = new MessagesConfig(plugin);
			tryLoad(messagesConfig);
			
			CommandValidate.isTrue(!messagesConfig.messages.isEmpty(), "Non ci sono messaggi da rimuovere!");
			CommandValidate.isTrue(index0 < messagesConfig.messages.size(), "Inserisci un numero minore o uguale a " + messagesConfig.messages.size());
			String removed = messagesConfig.messages.remove(index0);
			trySave(messagesConfig);
			
			tryReload();
			sender.sendMessage(ChatColor.GREEN + "Rimosso messaggio: " + ChatColor.RESET + plugin.format(removed));
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("reload")) {
			tryReload();
			sender.sendMessage(ChatColor.GREEN + "Configurazione ricaricata da disco!");
			return;
		}
		
		
		sender.sendMessage(ChatColor.RED + "Sottocomando sconosciuto. Scrivi /" + label + " per la lista comandi.");
	}
	
	
	
	private void tryLoad(YamlerConfig config) throws ExecuteException {
		try {
			config.init();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			throw new ExecuteException("Impossibile leggere il file da disco, controlla la console!");
		}
	}
	

	private void trySave(YamlerConfig config) throws ExecuteException {
		try {
			config.save();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			throw new ExecuteException("Impossibile salvare il file su disco, controlla la console!");
		}
	}
	
	
	private void tryReload() throws ExecuteException {
		try {
			plugin.load();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			throw new ExecuteException("Impossibile ricaricare la configurazione, controlla la console!");
		}
	}

}
