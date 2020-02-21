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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import net.cubespace.yamler.YamlerConfigurationException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.autobroadcast.config.FormatConfig;
import com.gmail.filoghost.autobroadcast.config.MessagesConfig;
import com.gmail.filoghost.autobroadcast.config.SettingsConfig;
import com.google.common.collect.Lists;

public class AutoBroadcast extends JavaPlugin {
	
	@Getter private List<String> messages;
	private int delay;
	private String prefix;
	private String suffix;
	
	private BukkitRunnable announceTask;
	private LinkedList<String> broadcastDeque;

	@Override
	public void onEnable() {

		if (!Bukkit.getPluginManager().isPluginEnabled("WildCommons")) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + this.getName() + "] Richiesto WildCommons!");
			setEnabled(false);
			return;
		}
		
		new CommandHandler(this, "autobroadcast");
		
		try {
			load();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			getLogger().severe("Impossibile caricare la configurazione!");
		}
	}
	
	
	public void load() throws YamlerConfigurationException {
		if (announceTask != null) {
			announceTask.cancel();
		}
		
		SettingsConfig settingsConfig = new SettingsConfig(this);
		MessagesConfig messagesConfig = new MessagesConfig(this);
		FormatConfig formatConfig = new FormatConfig(this);
		
		settingsConfig.init();
		formatConfig.init();
		messagesConfig.init();
		
		delay = settingsConfig.delay;
		if (delay < 5) {
			delay = 5;
		}
		
		prefix = format(formatConfig.prefix);
		suffix = format(formatConfig.suffix);
		
		messages = Lists.newArrayList();
		for (String message : messagesConfig.messages) {
			messages.add(format(message));
		}
		
		announceTask = new BukkitRunnable() {
			
			@Override
			public void run() {
				if (Bukkit.getOnlinePlayers().isEmpty() || messages == null || messages.isEmpty()) {
					return;
				}
				
				// In questo modo non ci dovrebbero quasi mai essere due messaggi uguali di seguito
				if (broadcastDeque == null || broadcastDeque.isEmpty()) {
					broadcastDeque = new LinkedList<>(messages);
					Collections.shuffle(broadcastDeque);
				}
				
				broadcastFormatted(broadcastDeque.poll());
			}
		};
		announceTask.runTaskTimer(this, delay * 20, delay * 20);
	}
	
	
	public String format(String input) {
		return ChatColor.translateAlternateColorCodes('&', input.replace("\\n", "\n"));
	}
	
	
	public void broadcastFormatted(String message) {
		StringBuilder sb = new StringBuilder();
		if (prefix != null) {
			sb.append(prefix);
		}
		
		sb.append(format(message));
		
		if (suffix != null) {
			sb.append(suffix);
		}
		
		for (String splitNewlines : sb.toString().split("\n")) {
			Bukkit.broadcastMessage(splitNewlines);
		}
	}
	

	
}
