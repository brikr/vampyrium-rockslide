package com.vampyriumrockslide;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class VampyriumRockslidePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(VampyriumRockslidePlugin.class);
		RuneLite.main(args);
	}
}