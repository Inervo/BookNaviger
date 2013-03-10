/*
 */

package booknaviger.profiles;

import booknaviger.MainInterface;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Inervo
 *
 */
public final class Profiles {
    
    private List<String[]> profiles = new ArrayList<>();
    private int currentProfile = 0;

    public Profiles() {
        setNewProfile("Default", "");
    }
    
    public void setNewProfile(String name, String folder) {
        profiles.add(new String[]{name, folder});
    }
    
    public void deleteProfile(String profileName) {
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i)[0].equals(profileName)) {
                profiles.remove(i);
                if (i == currentProfile) {
                    currentProfile = 0;
                    MainInterface.getInstance().refreshProfilesList();
                }
            }
        }
    }

    public String getCurrentProfileName() {
        return profiles.get(currentProfile)[0];
    }
    
    public String getCurrentProfileFolder() {
        return profiles.get(currentProfile)[1];
    }
    
    public void setCurrentProfileName(String profileName) {
        profiles.get(currentProfile)[0] = profileName;
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
    
    public void setProfileName(String profileName, String profileFolder) {
        profiles.set(getIndexFromProfileFolder(profileFolder), new String[] {profileName, profileFolder});
        if (profileFolder.equals(getCurrentProfileFolder())) {
            MainInterface.getInstance().refreshProfilesList();
        }
    }
    
    public String setNewCurrentProfile(String profileName) {
        currentProfile = getIndexFromProfileName(profileName);
        return getCurrentProfileFolder();
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
    
    private int getIndexFromProfileFolder(String profileFolder) {
        int profileIndex = -1;
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i)[1].equals(profileFolder)) {
                profileIndex = i;
            }
        }
        return profileIndex;
    }

}
