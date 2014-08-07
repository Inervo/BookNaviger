/*
 */

package booknaviger.booksfolder;

import booknaviger.exceptioninterface.InfoInterface;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class allow to list and analyse the directory off the books folder
 * @author Inervo
 */
public class BooksFolderAnalyser {
    
    File booksDirectory = null;

    /**
     * Constructor. Set the folder which will get analysed with one of the underlying method
     * @param booksFolder the folder which will be analysed
     */
    public BooksFolderAnalyser(File booksFolder) {
        Logger.getLogger(BooksFolderAnalyser.class.getName()).entering(BooksFolderAnalyser.class.getName(), "BooksFolderAnalyser", booksFolder);
        this.booksDirectory = booksFolder;
        Logger.getLogger(BooksFolderAnalyser.class.getName()).entering(BooksFolderAnalyser.class.getName(), "BooksFolderAnalyser");
    }

    /**
     * List the series of the set folder in {@link #booksDirectory}
     * @return an Array of String[] containing the series name
     */
    public Object[][] listSeries() {
        Logger.getLogger(BooksFolderAnalyser.class.getName()).entering(BooksFolderAnalyser.class.getName(), "listSeries");
        try {
            File[] allfiles = null;
            if (booksDirectory == null || !booksDirectory.exists()) {
                Logger.getLogger(BooksFolderAnalyser.class.getName()).exiting(BooksFolderAnalyser.class.getName(), "listSeries", null);
                return null;
            }
            try {
                allfiles = booksDirectory.listFiles((File pathname) -> {
                    return pathname.isDirectory() && !pathname.isHidden();
                });
            } catch(SecurityException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                new InfoInterface(InfoInterface.InfoLevel.ERROR, "rights", booksDirectory);
            }
            if (allfiles == null) {
                Logger.getLogger(BooksFolderAnalyser.class.getName()).exiting(BooksFolderAnalyser.class.getName(), "listSeries", null);
                return null;
            }
            Arrays.sort(allfiles);
            final File[] allFilesValue = allfiles;
            List<Object[]> series = new ArrayList<>();
            for (int i = 0; allFilesValue.length > i; i++) {
                File[] albumsFiles = new BooksFolderAnalyser(allFilesValue[i]).listAlbumsFiles();
                if (albumsFiles != null) {
                    series.add(new Object[] {allFilesValue[i].getName(), albumsFiles.length});
                }
            }
            Logger.getLogger(BooksFolderAnalyser.class.getName()).exiting(BooksFolderAnalyser.class.getName(), "listSeries", series.toArray(new Object[0][0]));
            return series.toArray(new Object[0][0]);
        } catch (Exception ex) {
            Logger.getLogger(BooksFolderAnalyser.class.getName()).log(Level.SEVERE, "Unknown exception", ex);
            new InfoInterface(InfoInterface.InfoLevel.ERROR, "unknown");
        }
        Logger.getLogger(BooksFolderAnalyser.class.getName()).exiting(BooksFolderAnalyser.class.getName(), "listSeries", new Object[][] {});
        return new Object[0][0];
    }
    
    /**
     * List the albums of the set folder in {@link #booksDirectory}
     * @return an Array of String[][] containing the albums name and the extension
     */
    public String[][] listAlbums() {
        Logger.getLogger(BooksFolderAnalyser.class.getName()).entering(BooksFolderAnalyser.class.getName(), "listAlbums");
        try {
            File[] allfiles = listAlbumsFiles();
            if (allfiles == null) {
                Logger.getLogger(BooksFolderAnalyser.class.getName()).exiting(BooksFolderAnalyser.class.getName(), "listAlbums", null);
                return null;
            }
            Arrays.sort(allfiles);
            final File[] allFilesValue = allfiles;
            final List<String[]> albums = new ArrayList<>();
            for (File allFilesValue1 : allFilesValue) {
                if (allFilesValue1.isDirectory()) {
                    albums.add(new String[]{allFilesValue1.getName(), ""});
                } else {
                    String albumFullName = allFilesValue1.getName();
                    int indexOfExtension = albumFullName.lastIndexOf(".");
                    albums.add(new String [] {albumFullName.substring(0, indexOfExtension), albumFullName.substring(indexOfExtension)});
                }
            }
            Logger.getLogger(BooksFolderAnalyser.class.getName()).exiting(BooksFolderAnalyser.class.getName(), "listAlbums", albums.toArray(new String[0][0]));
            return albums.toArray(new String[0][0]);
        } catch (Exception ex) {
            Logger.getLogger(BooksFolderAnalyser.class.getName()).log(Level.SEVERE, "Unknown exception", ex);
            new InfoInterface(InfoInterface.InfoLevel.ERROR, "unknown");
        }
        Logger.getLogger(BooksFolderAnalyser.class.getName()).exiting(BooksFolderAnalyser.class.getName(), "listAlbums", new String[0][0]);
        return new String[0][0];
    }
   
    /**
     * List the albums files, but no formating or sorting released.
     * @return The albums files
     */
    public File[] listAlbumsFiles() {
        Logger.getLogger(BooksFolderAnalyser.class.getName()).entering(BooksFolderAnalyser.class.getName(), "listAlbumsFiles");
        try {
            if (booksDirectory == null || !booksDirectory.exists()) {
                Logger.getLogger(BooksFolderAnalyser.class.getName()).exiting(BooksFolderAnalyser.class.getName(), "listAlbumsFiles", null);
                return null;
            }
            File[] allFiles = null;
            try {
                allFiles = booksDirectory.listFiles((File pathname) -> {
                    return !pathname.isHidden() && (pathname.isDirectory() || pathname.getName().toLowerCase().endsWith(".zip")
                            || pathname.getName().toLowerCase().endsWith(".cbz") || pathname.getName().toLowerCase().endsWith(".rar")
                            || pathname.getName().toLowerCase().endsWith(".cbr") || pathname.getName().toLowerCase().endsWith(".pdf"));
                });
            } catch(SecurityException ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                new InfoInterface(InfoInterface.InfoLevel.ERROR, "rights", booksDirectory);
            }
            Logger.getLogger(BooksFolderAnalyser.class.getName()).entering(BooksFolderAnalyser.class.getName(), "listAlbumsFiles", allFiles);
            return allFiles;
        } catch (Exception ex) {
            Logger.getLogger(BooksFolderAnalyser.class.getName()).log(Level.SEVERE, "Unknown exception", ex);
            new InfoInterface(InfoInterface.InfoLevel.ERROR, "unknown");
        }
        Logger.getLogger(BooksFolderAnalyser.class.getName()).entering(BooksFolderAnalyser.class.getName(), "listAlbumsFiles", null);
        return null;
    }
    
}
