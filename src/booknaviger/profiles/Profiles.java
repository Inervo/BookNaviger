/*
 */

package booknaviger.profiles;

import booknaviger.MainInterface;
import booknaviger.properties.PropertiesManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author Inervo
 *
 */
public final class Profiles {
    
    private List<String[]> profiles = new ArrayList<>();
    private int currentProfile = 0;

    public Profiles() {
        profiles.add(new String[]{"Default", ""});
        loadProfilesProperties();
    }

    public String getCurrentProfileName() {
        return profiles.get(currentProfile)[0];
    }
    
    public String getCurrentProfileFolder() {
        return profiles.get(currentProfile)[1];
    }
    
    public void setCurrentProfileFolder(String profileFolder) {
        setProfileFolder(profileFolder, getCurrentProfileName());
    }
    
    public void setProfileFolder(String profileFolder, String profileName) {
        profiles.set(getIndexFromProfileName(profileName), new String[] {profileName, profileFolder});
        if (profileName.equals(getCurrentProfileName())) {
            MainInterface.getInstance().refreshProfilesList();
        }
    }
    
    public String setNewCurrentProfile(String profileName) {
        currentProfile = getIndexFromProfileName(profileName);
        if (currentProfile == -1) {
            currentProfile = 0;
        }
        return getCurrentProfileFolder();
    }
    
    public void setProfiles(Vector<Vector<String>> profiles) {
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
    }
    
    public String[] getProfilesNames() {
        String[] profilesNames = new String[profiles.size()];
        for (int i = 0; i < profiles.size(); i++) {
            profilesNames[i] = profiles.get(i)[0];
        }
        return profilesNames;
    }
    
    public String[] getProfilesFolders() {
        String[] profilesFolder = new String[profiles.size()];
        for (int i = 0; i < profiles.size(); i++) {
            profilesFolder[i] = profiles.get(i)[1];
        }
        return profilesFolder;
    }
    
    public int getProfilesCount() {
        return profiles.size();
    }
    
    private int getIndexFromProfileName(String profileName) {
        int profileIndex = -1;
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i)[0].equals(profileName)) {
                profileIndex = i;
            }
        }
        return profileIndex;
    }
    
    public void loadProfilesProperties() {
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
    }
    
    public void saveProfilesProperties() {
        StringBuilder profileString = new StringBuilder();
        for (int i = 0; i < profiles.size(); i++) {
            profileString.append(profiles.get(i)[0]).append(",").append(profiles.get(i)[1]).append(";");
        }
        PropertiesManager.getInstance().setKey("profiles", profileString.toString());
        PropertiesManager.getInstance().setKey("lastSelectedProfile", getCurrentProfileName());
    }

}
