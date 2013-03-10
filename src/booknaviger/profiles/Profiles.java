/*
 */

package booknaviger.profiles;

import booknaviger.MainInterface;
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

}
