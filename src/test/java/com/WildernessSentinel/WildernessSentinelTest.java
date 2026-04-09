package com.WildernessSentinel;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class WildernessSentinelTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(WildernessSentinelPlugin.class);
		RuneLite.main(args);
	}
}