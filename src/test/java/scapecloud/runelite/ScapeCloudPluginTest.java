package scapecloud.runelite;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ScapeCloudPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ScapeCloudPlugin.class);
		RuneLite.main(args);
	}
}