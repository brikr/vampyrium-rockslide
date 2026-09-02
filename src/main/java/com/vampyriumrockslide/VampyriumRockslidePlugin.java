package com.vampyriumrockslide;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.ui.overlay.infobox.Timer;

@Slf4j
@PluginDescriptor(
	name = "Vampyrium Rockslide"
)
public class VampyriumRockslidePlugin extends Plugin
{
	/**
	 * xp values gained for taking the shortcut. going early gives 27.5 xp, on time goes 550
	 */
	private static final Set<Integer> SHORTCUT_XPS = Set.of(27, 28, 550);
	/**
	 * location of the shortcut
	 */
	private static final WorldPoint SHORTCUT_WORLD_POINT = new WorldPoint(2559, 7818, 0);
	/**
	 * if you gain the correct xp amount within this many tiles of the shortcut, the plugin assumes you have taken the shortcut
	 */
	private static int SHORTCUT_DISTANCE_THRESHOLD = 5;
	/**
	 * cooldown for gaining xp from the shortcut in seconds
	 */
	private static final int SHORTCUT_COOLDOWN_SECONDS = 450; // 7.5 minutes
	/**
	 * cooldown for gaining xp from the shortcut in ticks
	 */
	private static final int SHORTCUT_COOLDOWN_TICKS = 750;


	@Inject
	private Client client;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private VampyriumRockslideConfig config;

	@Inject
	private Notifier notifier;

	private int lastAgilityXp;
	private int readyTick = -1;

	private Timer shortcutTimer;
	private InfoBox readyInfoBox;

	@Provides
	VampyriumRockslideConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VampyriumRockslideConfig.class);
	}

	@Override
	protected void shutDown() throws Exception
	{
		infoBoxManager.removeInfoBox(shortcutTimer);
		infoBoxManager.removeInfoBox(readyInfoBox);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getGroup().equals("vampyriumrockslide"))
		{
			if (!config.showReadyInfoBox())
			{
				infoBoxManager.removeInfoBox(readyInfoBox);
			}
			if (!config.showCooldownInfoBox())
			{
				infoBoxManager.removeInfoBox(shortcutTimer);
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			lastAgilityXp = client.getSkillExperience(Skill.AGILITY);
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		if (statChanged.getSkill() == Skill.AGILITY)
		{
			int xpGained = statChanged.getXp() - lastAgilityXp;
			lastAgilityXp = statChanged.getXp();

			WorldPoint pos = client.getLocalPlayer().getWorldLocation();

			// check whether we completed the shortcut by inspecting xp gained and world location
			if (SHORTCUT_XPS.contains(xpGained) && pos.distanceTo(SHORTCUT_WORLD_POINT) <= SHORTCUT_DISTANCE_THRESHOLD)
			{
				// start the cooldown info box
				startShortcutCooldownTimer();

				// schedule the tick when the shortcut is usable again
				readyTick = client.getTickCount() + SHORTCUT_COOLDOWN_TICKS;
				// clear the ready info box
				infoBoxManager.removeInfoBox(readyInfoBox);
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (readyTick != -1 && client.getTickCount() >= readyTick)
		{
			// shortcut is ready again
			readyTick = -1;
			showReadyInfoBox();

			notifier.notify(config.notifyOnReady(), "Vampyrium Rockslide is ready");
		}
	}

	private void startShortcutCooldownTimer()
	{
		if (!config.showCooldownInfoBox())
		{
			return;
		}

		if (shortcutTimer != null)
		{
			// shouldn't happen, but prevent duplicate info boxes
			infoBoxManager.removeInfoBox(shortcutTimer);
		}

		BufferedImage image = itemManager.getImage(ItemID.PAYDIRT);

		shortcutTimer = new Timer(SHORTCUT_COOLDOWN_SECONDS, ChronoUnit.SECONDS, image, this);
		shortcutTimer.setTooltip("Vampyrium Rockslide");

		infoBoxManager.addInfoBox(shortcutTimer);
	}

	private void showReadyInfoBox()
	{
		if (!config.showReadyInfoBox())
		{
			return;
		}

		BufferedImage image = itemManager.getImage(ItemID.PAYDIRT);

		readyInfoBox = new InfoBox(image, this)
		{
			@Override
			public String getText()
			{
				return "READY";
			}

			@Override
			public Color getTextColor()
			{
				return Color.GREEN;
			}
		};
		readyInfoBox.setTooltip("Vampyrium Rockslide");

		infoBoxManager.addInfoBox(readyInfoBox);
	}
}
