/*
 */

package booknaviger.profiles;

import booknaviger.MainInterface;
import booknaviger.properties.PropertiesManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Class which contains all the profiles of the current instance
 * @author Inervo
 */
public final class Profiles {
    
    private List<String[]> profiles = new ArrayList<>();
    private int currentProfile = 0;

    /**
     * Constructor to Create a default profile, and then load the profiles from the properties
     * @see Profiles#loadProfilesProperties() 
     */
    public Profiles() {
        Logger.getLogger(Profiles.class.getName()).entering(Profiles.class.getName(), "Profiles");
        profiles.add(new String[]{"Default", ""});
        loadProfilesProperties();
        Logger.getLogger(Profiles.class.getName()).exiting(Profiles.class.getName(), "Profiles");
    }

    /**
     * Get the current profile name
     * @return The name of the current profile
     */
    public String getCurrentProfileName() {
        Logger.getLogger(Profiles.class.getName()).entering(Profiles.class.getName(), "getCurrentProfileName");
        Logger.getLogger(Profiles.class.getName()).exiting(Profiles.class.getName(), "getCurrentProfileName", profiles.get(currentProfile)[0]);
        return profiles.get(currentProfile)[0];
    }
    
    /**
     * Get the current profile folder
     * @return The base folder of the series for the current profile
     */
    public String getCurrentProfileFolder() {
        Logger.getLogger(Profiles.class.getName()).entering(Profiles.class.getName(), "getCurrentProfileFolder");
        Logger.getLogger(Profiles.class.getName()).exiting(Profiles.class.getName(), "getCurrentProfileFolder", profiles.get(currentProfile)[1]);
        return profiles.get(currentProfile)[1];
    }
    
    /**
     * Set the base folder for the current profile
     * @param profileFolder The folder to set for the current profile
     */
    public void setCurrentProfileFolder(String profileFolder) {
        Logger.getLogger(Profiles.class.getName()).entering(Profiles.class.getName(), "setCurrentProfileFolder", profileFolder);
        setProfileFolder(profileFolder, getCurrentProfileName());
        Logger.getLogger(Profiles.class.getName()).exiting(Profiles.class.getName(), "setCurrentProfileFolder");
    }
    
    /**
     * Set the profile folder for a specific profile
     * @param profileFolder The profile folder to set
     * @param profileName The profile name to which the profileFolder must be set
     */
    public void setProfileFolder(String profileFolder, String profileName) {
        Logger.getLogger(Profiles.class.getName()).entering(Profiles.class.getName(), "setProfileFolder", new Object[] {profileFolder, profileName});
        profiles.set(getIndexFromProfileName(profileName), new String[] {profileName, profileFolder});
        if (profileName.equals(getCurrentProfileName())) {
            MainInterface.getInstance().refreshProfilesList();
        }
        Logger.getLogger(Profiles.class.getName()).exiting(Profiles.class.getName(), "setProfileFolder");
    }
    
    /**
     * Set a profile as current profile
     * @param profileName The profile name to set as current
     * @return The profile folder of the new current profile
     */
    public String setNewCurrentProfile(String profileName) {
        Logger.getLogger(Profiles.class.getName()).entering(Profiles.class.getName(), "setNewCurrentProfile", profileName);
        currentProfile = getIndexFromProfileName(profileName);
        if (currentProfile == -1) {
            currentProfile = 0;
        }
        Logger.getLogger(Profiles.class.getName()).exiting(Profiles.class.getName(), "setNewCurrentProfile", getCurrentProfileFolder());
        return getCurrentProfileFolder();
    }
    
    /**
     * Set a {@link java.util.Vector} of {@link java.util.Vector} of {@link String} as profiles.
     * The profiles which will be set will first erase every previous profile
     * @param profiles The profiles to set as the new and only profiles
     */
    public void setProfiles(Vector<Vector<String>> profiles) {
        Logger.getLogger(Profiles.class.getName()).entering(Profiles.class.getName(), "setProfiles", profiles);
        String currentProfileName = getCurrentProfileName();
        this.profiles.clear();
        currentProfile = 0;
        for (Vector<String> profile : profiles) {
            this.profiles.add(new String[] {profile.get(0), profile.get(1)});
            if (profile.get(0).equals(currentProfileName)) {
                setNewCurrentProfile(currentProfileName);
            }
        }
        MainInterface.getInstance().refreshProfilesList();
        Logger.getLogger(Profiles.class.getName()).exiting(Profiles.class.getName(), "setProfiles");
    }
    
    /**
     * Get all the profiles names
     * @return the profiles names
     */
    public String[] getProfilesNames() {
        Logger.getLogger(Profiles.class.getName()).entering(Profiles.class.getName(), "getProfilesNames");
        String[] profilesNames = new String[profiles.size()];
        for (int i = 0; i < profiles.size(); i++) {
            profilesNames[i] = profiles.get(i)[0];
        }
        Logger.getLogger(Profiles.class.getName()).exiting(Profiles.class.getName(), "getProfilesNames", profilesNames);
        return profilesNames;
    }
    
    /**
     * Get all the profiles folders
     * @return The profiles folders
     */
    public String[] getProfilesFolders() {
        Logger.getLogger(Profiles.class.getName()).entering(Profiles.class.getName(), "getProfilesFolders");
        String[] profilesFolder = new String[profiles.size()];
        for (int i = 0; i < profiles.size(); i++) {
            profilesFolder[i] = profiles.get(i)[1];
        }
        Logger.getLogger(Profiles.class.getName()).exiting(Profiles.class.getName(), "getProfilesFolders", profilesFolder);
        return profilesFolder;
    }
    
    /**
     * Get the number of profiles
     * @return The number of profiles
     */
    public int getProfilesCount() {
        Logger.getLogger(Profiles.class.getName()).entering(Profiles.class.getName(), "getProfilesCount");
        Logger.getLogger(Profiles.class.getName()).exiting(Profiles.class.getName(), "getProfilesCount", profiles.size());
        return profiles.size();
    }

    /**
     * Get the index number of the profile from its name
     * @param profileName The profile name to search its index
     * @return the index of the searched profile name
     */
    private int getIndexFromProfileName(String profileName) {
        Logger.getLogger(Profiles.class.getName()).entering(Profiles.class.getName(), "getIndexFromProfileName", profileName);
        int profileIndex = -1;
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i)[0].equals(profileName)) {
                profileIndex = i;
            }
        }
        Logger.getLogger(Profiles.class.getName()).exiting(Profiles.class.getName(), "getIndexFromProfileName", profileIndex);
        return profileIndex;
    }
    
    /**
     * Load the profiles from the properties
     */
    public void loadProfilesProperties() {
        Logger.getLogger(Profiles.class.getName()).entering(Profiles.class.getName(), "loadProfilesProperties");
        String value = PropertiesManager.getInstance().getKey("profiles");
        if (value == null) {
            return;
        }
        String[] unparsedProfiles = value.split(";");
        String[][] partiallyParsedProfiles = new String[unparsedProfiles.length][2];
        if (partiallyParsedProfiles.length < 1) {
            return;
        }
        profiles.clear();
        for (int i = 0; i < partiallyParsedProfiles.length; i++) {
            String[] profileData = unparsedProfiles[i].split(",");
            if (profileData.length == 2) {
                profiles.add(new String[] {profileData[0], profileData[1]});
            }
            else {
                profiles.add(new String[] {profileData[0], ""});
            }
        }
        setNewCurrentProfile(PropertiesManager.getInstance().getKey("lastSelectedProfile"));
        Logger.getLogger(Profiles.class.getName()).exiting(Profiles.class.getName(), "loadProfilesProperties");
    }
    
    /**
     * Save the profiles to the properties
     */
    public void saveProfilesProperties() {
        Logger.getLogger(Profiles.class.getName()).entering(Profiles.class.getName(), "saveProfilesProperties");
        StringBuilder profileString = new StringBuilder();
        for (int i = 0; i < profiles.size(); i++) {
            profileString.append(profiles.get(i)[0]).append(",").append(profiles.get(i)[1]).append(";");
        }
        PropertiesManager.getInstance().setKey("profiles", profileString.toString());
        PropertiesManager.getInstance().setKey("lastSelectedProfile", getCurrentProfileName());
        Logger.getLogger(Profiles.class.getName()).exiting(Profiles.class.getName(), "saveProfilesProperties");
    }

}
