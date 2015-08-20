/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.meano.Residence.chat;

import java.util.HashMap;
import java.util.Map;
import net.meano.Residence.Residence;
import org.bukkit.Server;

/**
 *
 * @author Administrator
 */
public class ChatManager {

	protected Map<String, ChatChannel> channelmap;
	protected Server server;

	public ChatManager() {
		server = Residence.getServ();
		channelmap = new HashMap<String, ChatChannel>();
	}

	public void setChannel(String player, String channel) {
		this.removeFromChannel(player);
		if (!channelmap.containsKey(channel))
			channelmap.put(channel, new ChatChannel(channel));
		channelmap.get(channel).join(player);
	}

	public void removeFromChannel(String player) {
		for (ChatChannel chan : channelmap.values()) {
			if (chan.hasMember(player))
				chan.leave(player);
		}
	}

	public ChatChannel getChannel(String channel) {
		return channelmap.get(channel);
	}

	public ChatChannel getPlayerChannel(String player) {
		for (ChatChannel chan : channelmap.values()) {
			if (chan.hasMember(player))
				return chan;
		}
		return null;
	}

}
