package com.vampyriumrockslide;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Notification;

@ConfigGroup("vampyriumrockslide")
public interface VampyriumRockslideConfig extends Config
{
	@ConfigItem(
		keyName = "showCooldownInfoBox",
		name = "Cooldown info box",
		description = "Show an info box counting down until the shortcut is ready again",
		position = 0
	)
	default boolean showCooldownInfoBox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showReadyInfoBox",
		name = "Ready info box",
		description = "Show an info box when the shortcut is ready",
		position = 1
	)
	default boolean showReadyInfoBox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "notifyOnReady",
		name = "Notify when ready",
		description = "Send a notification when the shortcut is ready",
		position = 2
	)
	default Notification notifyOnReady()
	{
		return Notification.OFF;
	}
}
