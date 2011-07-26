// OtherBlocks - a Bukkit plugin
// Copyright (C) 2011 Robert Sargant
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.gmail.zariust.bukkit.otherblocks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.event.Event.Priority;
import org.bukkit.util.config.Configuration;
import org.bukkit.Material;
import org.bukkit.entity.*;

import com.gmail.zariust.bukkit.common.CommonMaterial;
import com.gmail.zariust.bukkit.common.CommonPlugin;

public class OtherBlocksConfig {

	public Boolean usePermissions;

	private OtherBlocks parent;

	static protected Integer verbosity;
	static protected Priority pri;
	protected boolean enableBlockTo;
	protected boolean disableEntityDrops;
	protected HashMap<String, OBContainer_DropGroups> blocksHash;

	private ArrayList<String> defaultWorlds = null;
	private ArrayList<String> defaultBiomes = null;
	private ArrayList<String> defaultWeather = null;
	private ArrayList<String> defaultPermissionGroups = null;
	private ArrayList<String> defaultPermissionGroupsExcept = null;
	private ArrayList<String> defaultPermissions = null;
	private ArrayList<String> defaultPermissionsExcept = null;
	private String defaultTime = null;
	

	public OtherBlocksConfig(OtherBlocks instance) {
		parent = instance;
		blocksHash = new HashMap<String, OBContainer_DropGroups>();

		verbosity = 2;
		pri = Priority.Lowest;
	}

	// load 
	public void load() {
		Boolean firstRun = true;
		loadConfig(firstRun);
		parent.setupPermissions();
	}

	public void reload()
	{
		Boolean firstRun = false;
		loadConfig(firstRun);
		parent.setupPermissions();
		//		parent.setupPermissions(this.usepermissions);

	}

	// Short functions
	//
	void logWarning(String msg) {
		parent.logWarning(msg);		
	}
	void logInfo(String msg) {
		parent.logInfo(msg);
	}

	public static boolean isCreature(String s) {
		return s.startsWith("CREATURE_");
	}

	public static boolean isPlayer(String s) {
		return s.startsWith("PLAYER");
	}

	public static boolean isPlayerGroup(String s) {
		return s.startsWith("PLAYERGROUP@");
	}

	public static boolean isDamage(String s) {
		return s.startsWith("DAMAGE_");
	}

	public static boolean isSynonymString(String s) {
		return s.startsWith("ANY_");
	}

	public static boolean isLeafDecay(String s) {
		return s.startsWith("SPECIAL_LEAFDECAY");
	}

	public static String creatureName(String s) {
		return (isCreature(s) ? s.substring(9) :s);
	}

	public static boolean hasDataEmbedded(String s) {
		return s.contains("@");
	}

	public static String getDataEmbeddedBlockString(String s) {
		if(!hasDataEmbedded(s)) return s;
		return s.substring(0, s.indexOf("@"));
	}

	public static String getDataEmbeddedDataString(String s) {
		if(!hasDataEmbedded(s)) return null;
		return s.substring(s.indexOf("@") + 1);
	}

	public static String getDropEmbeddedChance(String s)  {
		String divider = "/";
		if (s.contains(divider)) {
			for (String section : s.split("/")) {
				if (section.contains("%")) {
					return section.substring(0, section.indexOf("%"));
				}
			}
		} 
		return null;
	}

	public static String getDropEmbeddedQuantity(String s)  {
		String divider = "/";
		if (s.contains(divider)) {
			for (String section : s.split("/")) {
				if (section.matches("[0-9-~]+")) {
					return section;
				}
			}
		} 
		return null;
	}


	protected static void setAttackerDamage(OB_Drop obc, String dataString) {
		if(dataString == null) return;

		if(dataString.startsWith("RANGE-")) {
			String[] dataStringRangeParts = dataString.split("-");
			if(dataStringRangeParts.length != 3) throw new IllegalArgumentException("Invalid range specifier");
			obc.setAttackerDamage(Integer.parseInt(dataStringRangeParts[1]), Integer.parseInt(dataStringRangeParts[2]));
		} else {
			obc.setAttackerDamage(Integer.parseInt(dataString));
		}
	}

	protected static void setDataValues(OB_Drop obc, String dataString, String objectString) {

		if(dataString == null) return;

		if(dataString.startsWith("RANGE-")) {
			String[] dataStringRangeParts = dataString.split("-");
			if(dataStringRangeParts.length != 3) throw new IllegalArgumentException("Invalid range specifier");
			// TOFIX:: check for valid numbers - or is this checked earlier?
			obc.setData(Short.parseShort(dataStringRangeParts[1]), Short.parseShort(dataStringRangeParts[2]));
		} else {
			obc.setData(CommonMaterial.getAnyDataShort(objectString, dataString));
		}
	}

	protected static void setDropDataValues(OB_Drop obc, String dataString, String objectString) {

		if(dataString == null) return;

		if(dataString.startsWith("RANGE-")) {
			String[] dataStringRangeParts = dataString.split("-");
			if(dataStringRangeParts.length != 3) throw new IllegalArgumentException("Invalid range specifier");
			// TOFIX:: check for valid numbers - or is this checked earlier?
			obc.setDropData(Short.parseShort(dataStringRangeParts[1]), Short.parseShort(dataStringRangeParts[2]));
		} else {
			obc.setDropData(CommonMaterial.getAnyDataShort(objectString, dataString));
		}
	}

	public ArrayList<String> getArrayList(Object getVal, Boolean anyAll) throws Exception
	{
		ArrayList<String> arrayList = new ArrayList<String>();

		if(getVal == null) {
			arrayList.add((String) null);
		}
		else if(getVal instanceof String) {

			String getValString = (String) getVal;

			if (anyAll) {
				if(getValString.equalsIgnoreCase("ALL") || getValString.equalsIgnoreCase("ANY")) {
					arrayList.add((String) null);
				} else {
					arrayList.add(getValString);
				}
			} else {
				arrayList.add(getValString);
			}

		} else if (getVal instanceof List<?>) {

			for(Object listPart : (List<?>) getVal) {
				arrayList.add((String) listPart);
			}

		} else { // not a string or a list - throw exception
                  // TODO: what does this return if null value?  can we still return a val after throw exception?
                  // cannot throw in subfunction - catch null value and throw exception in main loadconfig function
			throw new Exception("Not a recognizable type");
		}
		return arrayList;
	}

	// LONGER FUNCTIONS
	public void loadConfig(boolean firstRun)
	{
		blocksHash.clear(); // clear here to avoid issues on /obr reloading
		
		String globalConfigName = ("otherblocks-globalconfig");
		File yml = new File(parent.getDataFolder(), globalConfigName+".yml");
		Configuration globalConfig = new Configuration(yml);
		// Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload) 

		if (!yml.exists())
		{
			try {
				yml.createNewFile();
				logInfo("Created an empty file " + parent.getDataFolder() +"/"+globalConfigName+", please edit it!");
				globalConfig.setProperty("otherblocks", null);
				globalConfig.save();
			} catch (IOException ex){
				logWarning(parent.getDescription().getName() + ": could not generate "+globalConfigName+". Are the file permissions OK?");
			}
		}

		// need to load the configuration for the reload command, otherwise config stays cached
		globalConfig.load();

		// Load in the values from the configuration file
		this.verbosity = CommonPlugin.getConfigVerbosity(globalConfig);
		this.pri = CommonPlugin.getConfigPriority(globalConfig);

		List <String> keys = CommonPlugin.getConfigRootKeys(globalConfig);

		// blockto/water damage is experimental, enable only if explicitly set
		if (keys.contains("enableblockto")) {
			if (globalConfig.getString("enableblockto").equalsIgnoreCase("true")) {
				enableBlockTo = true;
				logWarning("blockto/damage_water enabled - BE CAREFUL");
			} else {
				enableBlockTo = false;
			}
		}

		// blockto/water damage is experimental, enable only if explicitly set
		if (keys.contains("usepermissions")) {
			if (globalConfig.getString("usepermissions").equalsIgnoreCase("true")) {
				this.usePermissions = true;
				parent.usePermissions = true;
			} else {
				this.usePermissions = false;
				parent.usePermissions = false;
			}
		}

		// Read the config file version
		Integer configVersion = 1;
		if (keys.contains("configversion")) {
			if (globalConfig.getString("configversion").equalsIgnoreCase("1")) {
				configVersion = 1;
			} else if (globalConfig.getString("configversion").equalsIgnoreCase("2")) {
				configVersion = 2;
			} else {
				configVersion = 2; // assume latest version
			}
		}


		// load the globalconfig "OtherBlocks" section
		if (configVersion == 1) {
			System.out.println("loading version 1");
		} else {
			System.out.println("loading version 2");
		}
		loadSpecificFileVersion(globalConfigName, configVersion);


		// scan "include-files:" for additional files to load
		if(!keys.contains("include-files"))
		{
			//TODO: make this only show on verbosity 3
			if (parent.verbosity >= 3) {
			logInfo(parent.getDescription().getName() + ": no 'include-files' key found (optional)");
			}
			return;
		}

		keys.clear();
		keys = globalConfig.getKeys("include-files");

		if(null == keys)
		{
			// TODO: make this only show on verbosity 3
			if (parent.verbosity >= 3) {
			logInfo(parent.getDescription().getName() + ": no values found in include-files tag.");
			}
			return;
		}

		// keys found, clear existing (if any) transformlist
		// TODO: move clear to here rather than top? in case of config file failure?
		
		for(String s : keys) {
			//			List<Object> original_children = getConfiguration().getList("otherblocks."+s);

			//		if(original_children == null) {
			//	log.warning("Block \""+s+"\" has no children. Have you included the dash?");
			//	continue;
			
			// Reset default values
			defaultWorlds = null;
			defaultBiomes = null;
			defaultWeather = null;
			defaultPermissionGroups = null;
			defaultPermissionGroupsExcept = null;
			defaultTime = null;

			if (globalConfig.getString("include-files."+s, "true").equalsIgnoreCase("true")) {
				loadSpecificFileVersion(s, configVersion);
			}
		}


	}

	void loadSpecificFileVersion(String filename, Integer version) {

		// append .yml extension (cannot include this in config as fullstop is a special character, cleaner this way anyway)
		filename = filename+".yml";
		File yml = new File(parent.getDataFolder(), filename);
		Configuration configFile = new Configuration(yml);

		// Make sure config file exists (even for reloads - it's possible this did not create successfully or was deleted before reload) 
		if (!yml.exists())
		{
			logInfo("Trying to include: " + parent.getDataFolder() +"/"+filename+" but it does not exist!");
		}


		if (configFile == null) {
			return;
		}
		configFile.load(); // just in case

		List <String> keys = CommonPlugin.getConfigRootKeys(configFile);

		if(keys == null) {
			logWarning("No parent key not found.");
			return;
		}


		if(!keys.contains("otherblocks"))
		{
			parent.logWarning("No 'otherblocks' key found.", 2);
			return;
		}

		keys.clear();
		keys = configFile.getKeys("otherblocks");

		if(null == keys)
		{
			logInfo("No values found in config file!");
			return;
		}

		// BEGIN read default values

		List<Object> original_children = configFile.getList("defaults");

		if(original_children == null) {
			if (parent.verbosity >= 3) {
				logInfo("Defaults has no children (optional)");
			}
		} else {

			for(Object o : original_children) {
				if(o instanceof HashMap<?,?>) {

					try {
						HashMap<?, ?> m = (HashMap<?, ?>) o;

						defaultWorlds = getArrayList(m.get("worlds"), true);
						if (defaultWorlds == null) defaultWorlds = getArrayList(m.get("world"), true);
						defaultBiomes = getArrayList(m.get("biomes"), true);
						if (defaultBiomes == null) defaultBiomes = getArrayList(m.get("biome"), true);
						defaultWeather = getArrayList(m.get("weather"), true);
						defaultPermissionGroups = getArrayList(m.get("permissiongroup"), true);
						defaultPermissionGroupsExcept = getArrayList(m.get("permissiongroupexcept"), true);
						defaultPermissions = getArrayList(m.get("permissions"), true);
						defaultPermissionsExcept = getArrayList(m.get("permissionsexcept"), true);
						defaultTime = String.valueOf(m.get("time"));
					} catch(Throwable ex) {
					}
				}
			}
		}
		// END read default values


		parent.logInfo("CONFIG: loading keys for file: "+filename,3);


		for(Object currentKeyObj : keys) {
			// Each currentKeyObj is one block (SAND, GRASS, etc)

			// Trying to allow integer block values rather than needing eg. "4"
			// This bit doesn't work since the Bukkit getKeys function is a 
			// list of Strings and fails with Integers on the "for(Object currentKeyObj : keys)" line
			String currentKey = "";
			if (currentKeyObj instanceof String)
			{
				currentKey = (String) currentKeyObj;
			} else if (currentKeyObj instanceof Integer) {
				currentKey = currentKeyObj.toString();
			} else {
				logWarning("Block \""+currentKeyObj.toString()+"\" is not a string or an integer, skipping.");
				continue;
			}
			
			// Grab the children of the current block (generally lists)
			String currentPath = "otherblocks."+currentKey;
			original_children = configFile.getList(currentPath);
			
			if(original_children == null) {
				logWarning("(loadSpecificFileVersion) Block \""+currentKey+"\" has no children. Have you included the dash?");
				continue;
			}

			currentKey = currentKey.toUpperCase();
			OBContainer_DropGroups dropGroups = new OBContainer_DropGroups();
			if (version == 1) {
				for(Object o : original_children) {
					if(o instanceof HashMap<?,?>) {


						OB_Drop drop = readTool(currentKey, o, configFile);

						if (!(drop == null)) {
							OBContainer_Drops drops = new OBContainer_Drops();
							drops.list.add(drop);
							dropGroups.list.add(drops);
						}
					}
				}
			} else if (version == 2) {
				dropGroups = readBlock(currentPath, configFile, currentKey);
			}

			// new hash map for more efficient comparisons

			String blockId = null;
			// Source block
			String blockString = getDataEmbeddedBlockString(currentKey);

			try {
				Integer blockInt = Integer.valueOf(blockString);
				blockId = blockInt.toString();
			} catch(NumberFormatException x) {
				if(isCreature(blockString)) {
					blockId = "CREATURE_" + CreatureType.valueOf(creatureName(blockString)).toString();
				} else if(isPlayer(currentKey)) {
					blockId = "PLAYER";
				} else if(isPlayerGroup(currentKey)) {
					blockId = "PLAYER";
				} else if(isLeafDecay(blockString)) {
					blockId = "SPECIAL_LEAFDECAY";
				} else if(isSynonymString(blockString)) {
					if(!CommonMaterial.isValidSynonym(blockString)) {
						throw new IllegalArgumentException(blockString + " is not a valid synonym");
					} else {
						// add to each hash for id's here
						List<Material> listMats = CommonMaterial.getSynonymValues(blockString);
						for (Material mat : listMats) {
							Integer blockInt = mat.getId();
							blockId = blockInt.toString();
							addToDropHash(blockId, dropGroups);
						}
						blockId = null;
					}
				} else {
					try {
						Integer blockInt = Material.getMaterial(blockString).getId();
						blockId = blockInt.toString();
					} catch(Throwable ex) {
						logWarning("Configread: error getting matId for "+blockString);
					}
				}
			}
			if (blockId != null) addToDropHash(blockId, dropGroups);

		}
		parent.logInfo("CONFIG: "+filename+" loaded.",2);
	}

	private void addToDropHash(String blockId, OBContainer_DropGroups dropGroups) {			
		if (blockId != null) {
			// check for existing container at this ID and add to it if there is
			OBContainer_DropGroups thisDropGroups = blocksHash.get(blockId);
			if (thisDropGroups != null) {
				for (OBContainer_Drops dropGroup : dropGroups.list) {
					thisDropGroups.list.add(dropGroup);
				}
				parent.logInfo("CONFIG: adding to existing blocksHash for: ("+blockId+")",3);
				blocksHash.put(blockId, thisDropGroups);
			} else {
				blocksHash.put(blockId, dropGroups);
				parent.logInfo("CONFIG: creating new blocksHash for: ("+blockId+")",3);
			}
		}
}
		private OBContainer_DropGroups readBlock(String currentPath, Configuration configFile, String blockName) {
			OBContainer_DropGroups dropGroups = new OBContainer_DropGroups();

			List<Object> blockChildren = configFile.getList(currentPath);

			if(blockChildren == null) {
				logWarning("(readblock) Block \""+currentPath+"\" has no children. Have you included the dash?");
				return null;
			}
			//for(String blockChild : blockChildren) {

			for(Object blockChild : blockChildren) {
				//logWarning("inside readblock loop");
				if(blockChild instanceof HashMap<?,?>) {
					try {
						HashMap<?, ?> m = (HashMap<?, ?>) blockChild;
	
						if (m.get("dropgroup") != null) {
							parent.logInfo("readBlock: adding dropgroup: " + String.valueOf(m.get("dropgroup")), 3);
							dropGroups.list.add(readDropGroup(m, configFile, blockName));
						} else {
							OB_Drop drop = readTool(blockName, blockChild, configFile);
							if (!(drop == null)) {
								parent.logInfo("readBlock: adding single drop",3);
								OBContainer_Drops dropGroup = new OBContainer_Drops();
								dropGroup.list.add(drop);
								dropGroups.list.add(dropGroup);
							}
						}
					} catch(Throwable ex) {
						if(verbosity > 1) {
							logWarning("Error while processing dropgroup inside block '" + blockName + "' (" + ex.getMessage() + ")");
						}
		 
						if (verbosity > 2) ex.printStackTrace();
						return null;
					}

				}
			}
			return dropGroups;
		}

		private OBContainer_Drops readDropGroup(HashMap<?, ?> m, Configuration configFile, String blockName) throws Exception
		{

			OBContainer_Drops dropGroup = new OBContainer_Drops();
	//		List<Object> blockChildren = configFile.getList(currentPath);

//			if(blockChildren == null) {
	//			logWarning("Block \""+currentPath+"\" has no children. Have you included the dash?");
		//		return null;
			//}
			//for(String blockChild : blockChildren) {

		//	for(Object blockChild : blockChildren) {
			//	if(blockChild instanceof HashMap<?, ?>) {
				//	try{

					//	HashMap<?, ?> m = (HashMap<?, ?>) blockChild;
						parent.logInfo("CONFIG: IN DROPGROUP....",3);

						//if (m.get("dropgroup") == null) {
					//		dropGroup.list.add(readTool(blockName, blockChild, configFile));
					//	} else {
							String name = (String) m.get("dropgroup");
							parent.logInfo("Dropgroup found ("+name+")", 2);
							dropGroup.name = name;

							Double dropChance;
							try {
								dropChance = Double.valueOf(String.valueOf(m.get("chance")));
								dropGroup.chance = (dropChance < 0 || dropChance > 100) ? 100 : dropChance;
							} catch(NumberFormatException ex) {
								dropGroup.chance = 100.0;
							}

							// Applicable worlds
							String getString;

							getString = "world";
							if (m.get(getString) == null) getString = "worlds";															
							dropGroup.worlds = getArrayList(m.get(getString), true);
							if (dropGroup.worlds == null) {
								if (defaultWorlds == null) {
									throw new Exception("Not a recognizable type");
								} else {
									dropGroup.worlds = defaultWorlds;
								}
							}

							// Get applicable weather conditions
							dropGroup.weather = getArrayList(m.get("weather"), true);
							if (dropGroup.weather == null) {
								if (defaultWeather == null) {
									throw new Exception("Not a recognizable type");
								} else {
									dropGroup.weather = defaultWeather;
								}
							}

							// Get applicable biome conditions
							getString = "biome";
							if (m.get(getString) == null) getString = "biomes";															
							dropGroup.biome = getArrayList(m.get(getString), true);
							if (dropGroup.biome == null) throw new Exception("Not a recognizable type");
							if (dropGroup.biome == null) {
								if (defaultBiomes == null) {
									throw new Exception("Not a recognizable type");
								} else {
									dropGroup.biome = defaultBiomes;
								}
							}

							// Get event conditions
							dropGroup.event = getArrayList(m.get("event"), true);
							if (dropGroup.event == null) throw new Exception("Not a recognizable type");

							// Message
							// Applicable messages
							getString = "message";
							if (m.get(getString) == null) getString = "messages";															
							dropGroup.messages = getArrayList(m.get(getString), false);
							if (dropGroup.messages == null) throw new Exception("Not a recognizable type");

							// Get the time string
							String timeString = String.valueOf(m.get("time"));
							if(m.get("time") == null) {
								dropGroup.time = defaultTime;
							} else {
								dropGroup.time = timeString;
							}

							// Get the exclusive string
							String exlusiveString = String.valueOf(m.get("exclusive"));
							if(m.get("exclusive") == null) {
								dropGroup.exclusive = null;
							} else {
								dropGroup.exclusive = exlusiveString;
							}

							// Get permission groups
							dropGroup.permissionGroups = getArrayList(m.get("permissiongroup"), true);
							if (dropGroup.permissionGroups == null) throw new Exception("Not a recognizable type");
							if (dropGroup.permissionGroups == null) {
								if (defaultPermissionGroups == null) {
									throw new Exception("Not a recognizable type");
								} else {
									logWarning("permissionsgroup is obselete - please use 'permissions' and assign 'otherblocks.custom.<permission>' to groups or users as neccessary.");
									dropGroup.permissionGroups = defaultPermissionGroups;
								}
							}

							// Get permission groups
							dropGroup.permissionGroupsExcept = getArrayList(m.get("permissiongroupexcept"), true);
							if (dropGroup.permissionGroupsExcept == null) throw new Exception("Not a recognizable type");
							if (dropGroup.permissionGroupsExcept == null) {
								if (defaultPermissionGroupsExcept == null) {
									throw new Exception("Not a recognizable type");
								} else {
									logWarning("permissionsgroupexcept is obselete - please use 'permissionsExcept' and assign 'otherblocks.custom.<permission>' to groups or users as neccessary.");
									dropGroup.permissionGroupsExcept = defaultPermissionGroupsExcept;
								}
							}

							// Get permissions
							dropGroup.permissions = getArrayList(m.get("permissions"), true);
							if (dropGroup.permissions == null) throw new Exception("Not a recognizable type");
							if (dropGroup.permissions == null) {
								if (defaultPermissions == null) {
									throw new Exception("Not a recognizable type");
								} else {
									dropGroup.permissions = defaultPermissions;
								}
							}

							// Get permission exceptions
							dropGroup.permissionsExcept = getArrayList(m.get("permissionsExcept"), true);
							if (dropGroup.permissionsExcept == null) throw new Exception("Not a recognizable type");
							if (dropGroup.permissionsExcept == null) {
								if (defaultPermissionsExcept == null) {
									throw new Exception("Not a recognizable type");
								} else {
									dropGroup.permissionsExcept = defaultPermissionsExcept;
								}
							}

							String heightString = String.valueOf(m.get("height"));
							if(m.get("height") == null) {
								dropGroup.height = null;
							} else {
								dropGroup.height = heightString;
							}
							
							if (m.get("drops") != null) {
								List<Object> dropGroupDrops = (List<Object>) m.get("drops");

							if(dropGroupDrops == null) {
								logWarning("Dropgroup drops for \""+blockName+"."+name+"\" has no children. Have you included the dash?");
								return null;
							}
							//for(String blockChild : blockChildren) {

							for(Object dropGroupChild : dropGroupDrops) {
								if(dropGroupChild instanceof HashMap<?, ?>) {
									try{
										
										OB_Drop toolContainer = readTool(blockName, dropGroupChild, configFile);
										dropGroup.list.add(toolContainer);
									} catch(Throwable ex) {
										if(verbosity > 1) {
											logWarning("DROPGROUP: Error while processing dropgroup drops " + blockName + ": " + ex.getMessage());
										}

										ex.printStackTrace();
										return null;
									}
								}
							}
							if (dropGroup.name != null) parent.logInfo("dropgroup with name completed", 2);
							}
						



			return dropGroup; 
		}
		
		private OB_Drop readTool(String s, Object o, Configuration configFile) {    
			OB_Drop bt = new OB_Drop();

			try {
				HashMap<?, ?> m = (HashMap<?, ?>) o;

				// Source block
				s = s.toUpperCase();
				String blockString = getDataEmbeddedBlockString(s);
				String dataString = getDataEmbeddedDataString(s);

				bt.original = null;
				bt.setData(null);
				try {
					Integer block = Integer.valueOf(blockString);
					bt.original = blockString;
				} catch(NumberFormatException x) {
					if(isCreature(blockString)) {
						// Sheep can be coloured - check here later if need to add data vals to other mobs
						bt.original = "CREATURE_" + CreatureType.valueOf(creatureName(blockString)).toString();
						if(blockString.contains("SHEEP")) {
							setDataValues(bt, dataString, "WOOL");
						} else {
							setDataValues(bt, dataString, blockString);
						}
					} else if(isPlayer(s)) {
						bt.original = s;
					} else if(isPlayerGroup(s)) {
						bt.original = s;
					} else if(isLeafDecay(blockString)) {
						bt.original = blockString;
						setDataValues(bt, dataString, "LEAVES");
					} else if(isSynonymString(blockString)) {
						if(!CommonMaterial.isValidSynonym(blockString)) {
							throw new IllegalArgumentException(blockString + " is not a valid synonym");
						} else {
							bt.original = blockString;
						}
					} else {
						bt.original = Material.valueOf(blockString).toString();
						setDataValues(bt, dataString, blockString);
					}
				}

				// Tool used
				bt.tool = new ArrayList<String>();

					if (m.get("tool") == null) {
						bt.tool.add(null); // set the default to ALL if not specified
					} else if(isLeafDecay(bt.original)) {
						bt.tool.add(null);
					} else if(m.get("tool") instanceof Integer) {
						Integer tool = (Integer) m.get("tool");
						bt.tool.add(tool.toString());
					} else if(m.get("tool") instanceof String) {
						String toolString = (String) m.get("tool");
						if(toolString.equalsIgnoreCase("DYE")) toolString = "INK_SACK";

						if(toolString.equalsIgnoreCase("ALL") || toolString.equalsIgnoreCase("ANY")) {
							bt.tool.add(null);
						} else if(CommonMaterial.isValidSynonym(toolString)) {
							bt.tool.add(toolString);
						} else if(isDamage(toolString) || isCreature(toolString)) {
						    bt.tool.add(toolString);
						} else if (toolString.contains("@")) {
							String[] toolSplit = toolString.split("@");
							bt.tool.add(Material.valueOf(toolSplit[0].toUpperCase()).toString()+"@"+toolSplit[1]);
						} else {
							bt.tool.add(Material.valueOf(toolString.toUpperCase()).toString());
						}
					} else if (m.get("tool") instanceof List<?>) {

						for(Object listTool : (List<?>) m.get("tool")) {
							String t = (String) listTool;
							if(CommonMaterial.isValidSynonym(t)) {
								bt.tool.add(t);
							} else if(isDamage(t)) {
							    bt.tool.add(t);
							//} else if(isCreature(t)) {
                            //    bt.tool.add(t);
                            } else {
								bt.tool.add(Material.valueOf(t.toUpperCase()).toString());
							}
						}

					} else {
						throw new Exception("Tool: Not a recognizable type");
					}

				// Tool EXCEPTIONS

				if (m.get("toolexcept") == null) {
					bt.toolExceptions = null;
				} else {
					bt.toolExceptions = new ArrayList<String>();
					if(isLeafDecay(bt.original)) {
						bt.toolExceptions.add(null);
					} else if(m.get("toolexcept") instanceof String) {

						String toolString = (String) m.get("toolexcept");
						toolString = toolString.toUpperCase();

						if(toolString.equalsIgnoreCase("DYE")) toolString = "INK_SACK";

						if(toolString.equalsIgnoreCase("ALL") || toolString.equalsIgnoreCase("ANY")) {
							bt.toolExceptions.add(null);
						} else if(CommonMaterial.isValidSynonym(toolString)) {
							bt.toolExceptions.add(toolString);
						} else if(isDamage(toolString) || isCreature(toolString)) {
							bt.toolExceptions.add(toolString);
						} else {
							bt.toolExceptions.add(Material.valueOf(toolString).toString());
						}

					} else if (m.get("toolexcept") instanceof List<?>) {

						for(Object listTool : (List<?>) m.get("toolexcept")) {
							String t = (String) listTool;
							t = t.toUpperCase();
							if(CommonMaterial.isValidSynonym(t)) {
								bt.toolExceptions.add(t);
							} else if(isDamage(t)) {
								bt.toolExceptions.add(t);
								//} else if(isCreature(t)) {
								//    bt.tool.add(t);
							} else {
								bt.toolExceptions.add(Material.valueOf(t).toString());
							}
						}

					} else {
						throw new Exception("Toolexcept: Not a recognizable type");
					}
				}

				// Dropped item
				String fullDropString = String.valueOf(m.get("drop")).toUpperCase();
				String dropString = getDataEmbeddedBlockString(fullDropString);
				String dropDataString = getDataEmbeddedDataString(fullDropString);

				try {
					Integer block = Integer.valueOf(fullDropString);
					bt.dropped = fullDropString;
				} catch(NumberFormatException x) {
					if(dropString.equalsIgnoreCase("DYE")) dropString = "INK_SACK";
					if(dropString.equalsIgnoreCase("NOTHING")) dropString = "AIR";

					if (m.get("drop") == null) {
						bt.dropped = "DEFAULT";
					} else if (dropString.startsWith("MONEY")) {
						bt.dropped = dropString;
					} else if(isCreature(dropString)) {
						bt.dropped = "CREATURE_" + CreatureType.valueOf(creatureName(dropString)).toString();
						setDropDataValues(bt, dropDataString, dropString);
					} else if(dropString.equalsIgnoreCase("CONTENTS")) {
					    bt.dropped = "CONTENTS";
					} else if(dropString.equalsIgnoreCase("DEFAULT")) {
					    bt.dropped = "DEFAULT";
					} else if(dropString.equalsIgnoreCase("DENY")) {
						bt.dropped = "DENY";
					} else if(dropString.equalsIgnoreCase("NODROP")) {
						bt.dropped = "NODROP";
					} else {
						bt.dropped = Material.valueOf(dropString.toUpperCase()).toString();
						setDropDataValues(bt, dropDataString, dropString);
					}
				}

				bt.setAttackerDamage(0);
				if (m.get("damageattacker") != null) {
					try {
						Integer dropQuantity = Integer.valueOf(m.get("damageattacker").toString());
						bt.setAttackerDamage(dropQuantity.intValue());
					} catch(NumberFormatException x) {
						String dropQuantity = String.class.cast(m.get("damageattacker"));
						String[] split;
						if (dropQuantity.contains("~")) {
							split = dropQuantity.split("~");
						} else {
							split = dropQuantity.split("-");
						}
						if (split.length == 2) {
							bt.setAttackerDamage(Integer.valueOf(split[0]), Integer.valueOf(split[1]));									
						} else {
							parent.logWarning("[BLOCK: "+bt.original+"] Invalid damageAttacker - set to 0.",3);
						}
					}
				}
					

				// Dropped color
				String dropColor = String.valueOf(m.get("color"));

				if (m.get("color") != null) {
					bt.setDropData(CommonMaterial.getAnyDataShort(bt.dropped, dropColor));
				}

				// Dropped quantity
				bt.setQuantity(Float.valueOf(1));
				if (m.get("quantity") != null) {
					try {
						Double dropQuantity = Double.valueOf(m.get("quantity").toString());
						//log.info(dropQuantity.toString());
						bt.setQuantity(dropQuantity.floatValue());
					} catch(NumberFormatException x) {
						String dropQuantity = String.class.cast(m.get("quantity"));
						String[] split;
						if (dropQuantity.contains("~")) {
							split = dropQuantity.split("~");
						} else {
							split = dropQuantity.split("-");
						}
						if (split.length == 2) {
							bt.setQuantity(Float.valueOf(split[0]), Float.valueOf(split[1]));									
						} else {
							logWarning("[BLOCK: "+bt.original+"] Invalid quantity - set to 1.");
						}
					}
				}

				// Tool damage
				Integer toolDamage = Integer.class.cast(m.get("damagetool"));
				if (toolDamage == null) {
					toolDamage = Integer.class.cast(m.get("damage"));
					if (toolDamage != null) logWarning("'damage' is obselete, use 'damagetool'");
				}
				bt.damage = (toolDamage == null || toolDamage < 0) ? 1 : toolDamage;


				// Delay
				bt.setDelay(0);
				if (m.get("delay") != null) {
					try {
						Integer dropQuantity = Integer.valueOf(m.get("delay").toString());
						bt.setDelay(dropQuantity.intValue());
					} catch(NumberFormatException x) {
						String dropQuantity = String.class.cast(m.get("delay"));
						String[] split;
						if (dropQuantity.contains("~")) {
							split = dropQuantity.split("~");
						} else {
							split = dropQuantity.split("-");
						}
						if (split.length == 2) {
							bt.setDelay(Integer.valueOf(split[0]), Integer.valueOf(split[1]));									
						} else {
							parent.logWarning("[BLOCK: "+bt.original+"] Invalid delay - set to 0.",3);
						}
					}
				}

				
				// Drop probability
				Double dropChance;
				try {
					dropChance = Double.valueOf(String.valueOf(m.get("chance")));
					bt.chance = (dropChance < 0 || dropChance > 100) ? 100 : dropChance;
				} catch(NumberFormatException ex) {
					bt.chance = 100.0;
				}
				
				// Applicable worlds
				String getString;
				
				getString = "world";
				if (m.get(getString) == null) getString = "worlds";															
				bt.worlds = getArrayList(m.get(getString), true);
				if (bt.worlds == null) {
					if (defaultWorlds == null) {
						throw new Exception("Not a recognizable type");
					} else {
						bt.worlds = defaultWorlds;
					}
				}

				bt.regions = getArrayList(m.get("regions"), true);
				if (bt.regions == null) {
						throw new Exception("Not a recognizable type");
				}
				
				// Get applicable weather conditions
				bt.weather = getArrayList(m.get("weather"), true);
				if (bt.weather == null) {
					if (defaultWeather == null) {
						throw new Exception("Not a recognizable type");
					} else {
						bt.weather = defaultWeather;
					}
				}
				
				// Get applicable biome conditions
				getString = "biome";
				if (m.get(getString) == null) getString = "biomes";															
				bt.biome = getArrayList(m.get(getString), true);
				if (bt.biome == null) throw new Exception("Not a recognizable type");
				if (bt.biome == null) {
					if (defaultBiomes == null) {
						throw new Exception("Not a recognizable type");
					} else {
						bt.biome = defaultBiomes;
					}
				}

				// Get event conditions
				bt.event = getArrayList(m.get("event"), true);
				if (bt.event == null) throw new Exception("Not a recognizable type");

				// Message
				// Applicable messages
				getString = "message";
				if (m.get(getString) == null) getString = "messages";															
				bt.messages = getArrayList(m.get(getString), false);
				if (bt.messages == null) throw new Exception("Not a recognizable type");

				// Get the time string
				String timeString = String.valueOf(m.get("time"));
				if(m.get("time") == null) {
					bt.time = defaultTime;
				} else {
					bt.time = timeString;
				}

				// Get the exclusive string
				String exlusiveString = String.valueOf(m.get("exclusive"));
				if(m.get("exclusive") == null) {
					bt.exclusive = null;
				} else {
					bt.exclusive = exlusiveString;
				}

				// Get permission groups
				bt.permissionGroups = getArrayList(m.get("permissiongroup"), true);
				if (bt.permissionGroups == null) throw new Exception("Not a recognizable type");
				if (bt.permissionGroups == null) {
					if (defaultPermissionGroups == null) {
						throw new Exception("Not a recognizable type");
					} else {
                                          logWarning("permissionsgroup is obselete - please use 'permissions' and assign 'otherblocks.custom.<permission>' to groups or users as neccessary.");
                                          bt.permissionGroups = defaultPermissionGroups;
					}
				}
				
				// Get permission groups
				bt.permissionGroupsExcept = getArrayList(m.get("permissiongroupexcept"), true);
				if (bt.permissionGroupsExcept == null) throw new Exception("Not a recognizable type");
				if (bt.permissionGroupsExcept == null) {
					if (defaultPermissionGroupsExcept == null) {
						throw new Exception("Not a recognizable type");
					} else {
                                            logWarning("permissionsgroupexcept is obselete - please use 'permissionsExcept' and assign 'otherblocks.custom.<permission>' to groups or users as neccessary.");
                                            bt.permissionGroupsExcept = defaultPermissionGroupsExcept;
					}
				}

                                // Get permissions
                                bt.permissions = getArrayList(m.get("permissions"), true);
                                if (bt.permissions == null) throw new Exception("Not a recognizable type");
                                if (bt.permissions == null) {
                                        if (defaultPermissions == null) {
                                                throw new Exception("Not a recognizable type");
                                        } else {
                                                bt.permissions = defaultPermissions;
                                        }
                                }
                                
                                // Get permission exceptions
                                bt.permissionsExcept = getArrayList(m.get("permissionsExcept"), true);
                                if (bt.permissionsExcept == null) throw new Exception("Not a recognizable type");
                                if (bt.permissionsExcept == null) {
                                        if (defaultPermissionsExcept == null) {
                                                throw new Exception("Not a recognizable type");
                                        } else {
                                                bt.permissionsExcept = defaultPermissionsExcept;
                                        }
                                }

                                bt.height = mGetString(m, "height");
                                bt.attackRange = mGetString(m, "attackrange");
 
			} catch(Throwable ex) {
				if(verbosity > 1) {
					logWarning("Error while processing block '" + s + "' (" + ex.getMessage() + ")");
				}
 
				if (verbosity > 2) ex.printStackTrace();
				return null;
			}

			if(verbosity > 1) {
				logInfo("BLOCK: " +
						(bt.tool.contains(null) ? "ALL TOOLS" : (bt.tool.size() == 1 ? bt.tool.get(0).toString() : bt.tool.toString())) + " + " +
						creatureName(bt.original) + bt.getData() + " now drops " +
						(bt.getQuantityRange() + "x ") + 
						creatureName(bt.dropped) + "@" + bt.getDropDataRange() +
						(bt.chance < 100 ? " with " + bt.chance.toString() + "% chance" : "") +
						(!bt.regions.contains(null) ? " in regions " + bt.regions.toString() + " only ": ""));
			}

			return bt;

		}
		
        String mGetString (HashMap<?, ?> m, String param) {
			String heightString = String.valueOf(m.get(param));
			if(m.get(param) == null) {
				return null;
			} else {
				return heightString;
			}
                        }
	}