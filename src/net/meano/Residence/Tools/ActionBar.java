package net.meano.Residence.Tools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;

public class ActionBar {
		private static boolean enable = true;
		private static Method getHandle;
		private static Class<?> nmsChatSerializer;
		private static Class<?> nmsIChatBaseComponent;
		private static Class<?> packetType;
		private static Field playerConnection;
		private static Method sendPacket;
		private static String version = "";

		public static void broadCast(String message) {
				for (Player player : Bukkit.getOnlinePlayers())
						send(player, message);
		}

		public static void broadCast(final String message, int times, final Plugin plugin) {
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
						public void run() {
								int time = times;
								do {
										for (Player player : Bukkit.getOnlinePlayers())
												ActionBar.send(player, message);
										try {
												Thread.sleep(1000L);
										} catch (InterruptedException e) {
												plugin.getLogger().warning("ActionBar发包错误!");
										}
										time--;
								} while (time > 0);
						}
				});
		}

		public static void broadCast(final World world, final String message, int times, final Plugin plugin) {
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
						public void run() {
								int time = times;
								do {
										for (Player player : Bukkit.getOnlinePlayers()) {
												if (player.getWorld().getName().equalsIgnoreCase(world.getName()))
														ActionBar.send(player, message);
										}
										try {
												Thread.sleep(1000L);
										} catch (InterruptedException e) {
												plugin.getLogger().warning("ActionBar发包错误!");
										}
										time--;
								} while (time > 0);
						}
				});
		}

		public static boolean init() {
				return enable;
		}

		public static void send(Player receivingPacket, String msg) {
				Object packet = null;
				try {
						Object serialized = nmsChatSerializer.getMethod("a", new Class[] { String.class }).invoke(null, new Object[] { "{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', JSONObject.escape(msg)) + "\"}" });
						if (!version.contains("1_7"))
								packet = packetType.getConstructor(new Class[] { nmsIChatBaseComponent, Byte.TYPE }).newInstance(new Object[] { serialized, Byte.valueOf((byte) 2) });
						else {
								packet = packetType.getConstructor(new Class[] { nmsIChatBaseComponent, Integer.TYPE }).newInstance(new Object[] { serialized, Integer.valueOf(2) });
						}
						Object player = getHandle.invoke(receivingPacket, new Object[0]);
						Object connection = playerConnection.get(player);
						sendPacket.invoke(connection, new Object[] { packet });
				} catch (Exception ex) {
						Bukkit.getLogger().log(Level.SEVERE, "ActionBar发包错误 %s " + version, ex);
				}
				try {
						Object player = getHandle.invoke(receivingPacket, new Object[0]);
						Object connection = playerConnection.get(player);
						sendPacket.invoke(connection, new Object[] { packet });
				} catch (Exception ex) {
						Bukkit.getLogger().log(Level.SEVERE, "ActionBar发包错误 %s", ex);
				}
		}

		public static void send(final Player receivingPacket, final String msg, int times, final Plugin plugin) {
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
						public void run() {
								int time = times;
								do {
										ActionBar.send(receivingPacket, msg);
										try {
												Thread.sleep(1000L);
										} catch (InterruptedException e) {
												plugin.getLogger().warning("ActionBar发包错误!");
										}
										time--;
								} while (time > 0);
						}
				});
		}

		private static String getChatSerializerClasspath() {
				if ((version.equals("v1_8_R1")) || (version.contains("1_7"))) {
						return "net.minecraft.server." + version + ".ChatSerializer";
				}
				return "net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer";
		}

		private static String getCraftPlayerClasspath() {
				return "org.bukkit.craftbukkit." + version + ".entity.CraftPlayer";
		}

		private static String getIChatBaseComponentClasspath() {
				return "net.minecraft.server." + version + ".IChatBaseComponent";
		}

		private static String getNMSPlayerClasspath() {
				return "net.minecraft.server." + version + ".EntityPlayer";
		}

		private static String getPacketClasspath() {
				return "net.minecraft.server." + version + ".Packet";
		}

		private static String getPacketPlayOutChat() {
				return "net.minecraft.server." + version + ".PacketPlayOutChat";
		}

		private static String getPlayerConnectionClasspath() {
				return "net.minecraft.server." + version + ".PlayerConnection";
		}

		static {
				try {
						version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
						packetType = Class.forName(getPacketPlayOutChat());
						Class<?> typeCraftPlayer = Class.forName(getCraftPlayerClasspath());
						Class<?> typeNMSPlayer = Class.forName(getNMSPlayerClasspath());
						Class<?> typePlayerConnection = Class.forName(getPlayerConnectionClasspath());
						nmsChatSerializer = Class.forName(getChatSerializerClasspath());
						nmsIChatBaseComponent = Class.forName(getIChatBaseComponentClasspath());
						getHandle = typeCraftPlayer.getMethod("getHandle", new Class[0]);
						playerConnection = typeNMSPlayer.getField("playerConnection");
						sendPacket = typePlayerConnection.getMethod("sendPacket", new Class[] { Class.forName(getPacketClasspath()) });
				} catch (Exception ex) {
						enable = false;
				}
		}
}
