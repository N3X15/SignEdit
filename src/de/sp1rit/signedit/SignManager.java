package de.sp1rit.signedit;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.io.*;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.block.*;

public class SignManager {
	private static final Logger logger = Logger.getLogger("Minecraft.SignEdit");
    private final SignEdit plugin;
	private Properties signToId = new Properties();
    private Properties idToSign = new Properties();
    
        public SignManager(SignEdit instance) {
        	plugin = instance;
        	convertOldSigns();
            try {
            	File signEditFolder = new File(plugin.getDataFolder().getPath());
            	if (!signEditFolder.isDirectory())
            		signEditFolder.mkdirs();
        		File registeredSigns = new File(plugin.getDataFolder().getPath(), "signs.dat");
        		if (!registeredSigns.exists())
        			registeredSigns.createNewFile();
        			
                BufferedReader in = new BufferedReader(new FileReader(registeredSigns));

                String line;
                while( (line=in.readLine() ) != null ) {

                    String lineTrim = line.trim();

                    if( lineTrim.startsWith( "#" ) ) {
                        continue;
                    }

                    String[] split = lineTrim.split("=");

                    signToId.put(split[0], split[1]);
                    idToSign.put(split[1], split[0]);
                }

                in.close();
            } catch(IOException e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
        
        public void save() {
        	try {
            	File signEditFolder = new File(plugin.getDataFolder().getPath());
            	if (!signEditFolder.isDirectory())
            		signEditFolder.mkdirs();
        		File registeredSigns = new File(plugin.getDataFolder().getPath(), "signs.dat");
        		if (!registeredSigns.exists())
        			registeredSigns.createNewFile();
        		
        		BufferedWriter out = new BufferedWriter(new FileWriter(registeredSigns));
        	
        		signToId.store(out, "DO NOT EDIT THIS FILE!");
        		
        		out.close();
        	} catch(IOException e) {
        		logger.log(Level.SEVERE, null, e);
        	}
        }
        
        private void convertOldSigns() {
            try {
            	File signEditFolder = new File(plugin.getDataFolder().getPath());
            	if (signEditFolder.isDirectory()) {
	        		File oldSignsFile = new File(plugin.getDataFolder().getPath(), "signs.txt");
	        		if (oldSignsFile.exists()) {		
		                BufferedReader in = new BufferedReader(new FileReader(oldSignsFile));
		
		                String line;
		                while( (line=in.readLine() ) != null ) {
		
		                    String lineTrim = line.trim();
		
		                    if( lineTrim.startsWith( "#" ) ) {
		                        continue;
		                    }
		
		                    String[] split = lineTrim.split("=");
		                    
		                    split[0] = split[0] + ";" + plugin.getServer().getWorlds().get(0).getName();
		
		                    signToId.put(split[0], split[1]);
		                    idToSign.put(split[1], split[0]);
		                }
		                in.close();
		                if (signToId.size() > 0 && idToSign.size() > 0)
		                	save();
		                oldSignsFile.deleteOnExit();
		                
		                signToId.clear();
		                idToSign.clear();
	        		}
            	}
            } catch(IOException e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
        
	    /**
	     * Gibt das Schild im Sichtfeld des Spielers zur�ck.
	     * @param player
	     * @return
	     */
	    public Sign getTargetSign(Player player) {
	    		Block block = player.getTargetBlock(null, 300);
	            if (block != null) {
	                if (block.getState() instanceof Sign) {
	                    return(Sign)block.getState();
	                }
	            }
	            return null;
	    }

        /**
         *
         * Schild registrieren.
         *
         */

        public Boolean addSign(String signId, Block sign) {
            return addSign(signId, sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
        }

        public Boolean addSign(String signId, Sign sign) {
            return addSign(signId, sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
        }

        public Boolean addSign(String signId, World signWorld, int signX, int signY, int signZ) {
            if (!signExists(signId) && !signExists(signWorld, signX, signY, signZ)) {
                String signLocation = String.valueOf(signX) + ";" + String.valueOf(signY) + ";" + String.valueOf(signZ) + ";" + signWorld.getName();

                signToId.setProperty(signLocation, signId);
                idToSign.setProperty(signId, signLocation);
                save();

                return true;
            }
            return false;
        }

        /**
         *
         * Schild entfernen.
         *
         */

        public Boolean removeSign(Block sign) {
            return removeSign(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
        }

        public Boolean removeSign(Sign sign) {
            return removeSign(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
        }

        public Boolean removeSign(World signWorld, int signX, int signY, int signZ) {
            if (signExists(signWorld, signX, signY, signZ)) {
                String signLocation = String.valueOf(signX) + ";" + String.valueOf(signY) + ";" + String.valueOf(signZ) + ";" + signWorld.getName();
                String signId = signToId.getProperty(signLocation);

                signToId.remove(signLocation);
                idToSign.remove(signId);
                save();

                return true;
            }
            return false;
        }

        public Boolean removeSign(String signId) {
            if (signExists(signId)) {
                String signLocation = idToSign.getProperty(signId);

                signToId.remove(signLocation);
                idToSign.remove(signId);
                save();

                return true;
            }
            return false;
        }

        /**
         *
         * Schild-ID bearbeiten.
         *
         */

        public Boolean editSign(Block sign, String newId) {
            return editSign(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ(), newId);
        }

        public Boolean editSign(Sign sign, String newId) {
            return editSign(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ(), newId);
        }

        public Boolean editSign(World signWorld, int signX, int signY, int signZ, String newId) {
            if (signExists(signWorld, signX, signY, signZ) && !signExists(newId)) {
                String signLocation = String.valueOf(signX) + ";" + String.valueOf(signY) + ";" + String.valueOf(signZ) + ";" + signWorld.getName();
                String signId = signToId.getProperty(signLocation);

                signToId.setProperty(signLocation, newId);
                idToSign.remove(signId);
                idToSign.setProperty(newId, signLocation);
                save();

                return true;
            }
            return false;
        }

        public Boolean editSign(String signId, String newId) {
            if (signExists(signId) && !signExists(newId)) {
                String signLocation = idToSign.getProperty(signId);

                signToId.setProperty(signLocation, newId);
                idToSign.remove(signId);
                idToSign.setProperty(newId, signLocation);
                save();
                
                return true;
            }
            return false;
        }

        /**
         *
         * Pr�fen ob Schild registriert ist.
         *
         */

        public Boolean signExists(Block sign) {
            return signExists(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
        }

        public Boolean signExists(Sign sign) {
            return signExists(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
        }

        public Boolean signExists(World signWorld, int signX, int signY, int signZ) {
            String signLocation = String.valueOf(signX) + ";" + String.valueOf(signY) + ";" + String.valueOf(signZ) + ";" + signWorld.getName();
            if (signToId.containsKey(signLocation)) {
                return true;
            }
            return false;
        }

        public Boolean signExists(String signId) {
            if (idToSign.containsKey(signId)) {
                return true;
            }
            return false;
        }

        /**
         *
         * Schild-ID zur�ckgeben.
         *
         */

        public String getId(Block sign) {
            return getId(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
        }
        
        public String getId(Sign sign) {
            return getId(sign.getWorld(), sign.getX(), sign.getY(), sign.getZ());
        }

        public String getId(World signWorld, int signX, int signY, int signZ) {
            if (signExists(signWorld, signX, signY, signZ)) {
                String signLocation = String.valueOf(signX) + ";" + String.valueOf(signY) + ";" + String.valueOf(signZ) + ";" + signWorld.getName();
                
                return signToId.getProperty(signLocation);
            }
            return null;
        }
        
        /**
         * Get a sign by it�s registered ID.
         * @param signId
         * @return
         */
        public Sign getSign(String signId, Player p) {
        	if(signId.startsWith("{")) {
        		// {x,y,z}
        		String[] vec = signId.substring(1, signId.length()-2).split(",");
        		if(p==null) return null;
        		World pw = p.getWorld();
        		Vector signloc = new Vector(
    				Integer.valueOf(vec[0]), 
    				Integer.valueOf(vec[1]), 
    				Integer.valueOf(vec[2]));
        		Location ploc = p.getLocation();
        		if(signloc.distanceSquared(ploc.toVector())>36) {
        			return null;
        		}
        		Block block = pw.getBlockAt(signloc.toLocation(pw));
        		if(block==null)
        			return null;
        		if( block.getState() instanceof Sign)
        			return (Sign)block.getState();
        		return null;
        	}
            if (signExists(signId)) {
                String[] signLocation = idToSign.getProperty(signId).split(";");
                if (signLocation.length == 4) {
                	World world = plugin.getServer().getWorld(signLocation[3]);
                	if (world != null) {
	                	Block block = plugin.getServer().getWorld(world.getName()).getBlockAt(Integer.valueOf(signLocation[0]), Integer.valueOf(signLocation[1]), Integer.valueOf(signLocation[2]));
	                    if (block.getState() instanceof Sign) {
	                        return (Sign)block.getState();
	                    } else {
	                        removeSign(world, Integer.valueOf(signLocation[0]), Integer.valueOf(signLocation[1]), Integer.valueOf(signLocation[2]));
	                    }
                	}
                }
            }
            return null;
        }
}
