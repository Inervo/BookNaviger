/*
 */

package booknaviger.inet.htmlreport;

import booknaviger.picturehandler.FolderHandler;
import booknaviger.picturehandler.ImageReader;
import booknaviger.picturehandler.PdfHandler;
import booknaviger.picturehandler.RarHandler;
import booknaviger.picturehandler.ZipHandler;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileSystemView;

/**
 * @author Inervo
 *
 */
public class ReportGenerator extends SwingWorker<Integer, String> {

    private File indexFile = null;
    private File cssFile = null;
    private File pageXFile = null;
    private BufferedWriter cssFileWriter = null;
    private BufferedWriter pageXWriter = null;
    private boolean advancedMode;
    private ResourceBundle resourceBundle = ResourceBundle.getBundle("booknaviger/resources/ReportGenerator");
    private String reportFolder = null;
    private File baseDir = null;
    private int nbrOfSeries = 0;
    private int nbrOfAlbums = 0;
    private GenerationProgress generationProgressDialog = null;
    private int nbrOfProcessedAlbums = 0;
    private boolean cancelAsked = false;


    /**
     * Constructeur pour la création du rapport html
     * @param advancedMode mode de création du rapport (simple vs. advanced (avec miniatures)
     * @param baseDir Dossier contenant les séries
     * @param bnvf BookNavigerView Frame
     * @param gp Instance de GenerationProgress afin de communiquer les avancements de l'execution
     */
    public ReportGenerator(boolean advancedMode, File baseDir, final GenerationProgress gp) {
        super();
        this.advancedMode = advancedMode;
        this.baseDir = baseDir;
        this.generationProgressDialog = gp;
        String folder = System.getProperty("user.home");
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            folder = FileSystemView.getFileSystemView().getDefaultDirectory().toString();
        }
        folder = folder.concat(File.separatorChar + "HTMLReport");
        deleteHTMLReportDirectory(new File(folder));
        new File(folder).mkdir();
        reportFolder = folder + File.separatorChar;
        if (advancedMode) {
            new File(reportFolder + "thumbnails").mkdir();
        }
        indexFile = new File(reportFolder + "index.html");
        cssFile = new File(reportFolder + "style.css");
        addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if("progress".equals(evt.getPropertyName())) {
                    int newValue = (Integer) evt.getNewValue();
                    if (gp.getActionProgressBarValue() > newValue) {
                        return;
                    }
                    gp.setActionProgressBarValue(newValue);
                }
            }
        });
    }

    /**
     * Executé à la fin de la tâche principale
     */
    @Override
    protected void done() {
        generationProgressDialog.setActionLabelValue(resourceBundle.getString("finished.text"));
        generationProgressDialog.setActionProgressBarValue(100);
        generationProgressDialog.setVisible(false);
        generationProgressDialog.dispose();
    }

    /**
     * Gestion des strings données par l'execution de la tâche en arrière plan
     * @param chunks Liste des String d'informations
     */
    @Override
    protected void process(List<String> chunks) {
        for (String string : chunks) {
            generationProgressDialog.setActionLabelValue(string);
        }
    }

    /**
     * fonction racine dans la création du rapport
     * @return Valeur de retour (osef, non utilisé)
     */
    @Override
    protected Integer doInBackground() {
        nbrOfAlbums = 0;
        nbrOfProcessedAlbums = 0;
        findNbrOfAlbums();
        try {
            pageXWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(indexFile), "UTF-8"));
            cssFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cssFile), "UTF-8"));
            createCss();
            createHeader(pageXWriter);
            createContent();
            if (cancelAsked) {
                return 7;
            }
            createFooter();
            copyImages();
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
//            new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Opening_Report_Files");
        } catch (IOException ex) {
            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
//            new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Reading_Report_Files_Template");
        }
        try {
            pageXWriter.close();
            cssFileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, null, ex);
//            new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Close_Report_Files");
        }
        if (cancelAsked) {
            return 7;
        }
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI("file://" + reportFolder.replace('\\', '/').concat("index.html")));
                    return 0;
                }
            } catch (IOException ex) {
            } catch(URISyntaxException ex) {
                Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, "cannot show generated report. Please check index.html in the folder " + System.getProperty("user.dir").concat(File.separatorChar + "HTMLReport"), ex);
            }
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
//                IndexFilePosition ifp = new IndexFilePosition(bnvf, true, reportFolder + "index.html");
//                ifp.setVisible(true);
            }
        });
        return 0;
    }

    private void createCss() throws IOException {
        String css = "";
        BufferedReader initialCssFile = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("booknaviger/resources/graphics/htmlReport/style.css")));
        while(true) {
            String tampon = initialCssFile.readLine();
            if (tampon == null) {
                break;
            }
            cssFileWriter.write(tampon);
            cssFileWriter.newLine();
        }
    }

    /**
     * Créé l'header du fichier html
     * @param bw BufferedWriter sur le fichier dans lequel écrire le header
     * @throws IOException Si erreur dans l'écriture du fichier
     */
    protected void createHeader(BufferedWriter bw) throws IOException {
        StringBuilder header = new StringBuilder("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        header = header.append(System.getProperty("line.separator")).append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        header = header.append(System.getProperty("line.separator")).append("<head>");
        header = header.append(System.getProperty("line.separator")).append("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
        header = header.append(System.getProperty("line.separator")).append("  <meta name=\"Author\" content=\"Inervo\" />");
        header = header.append(System.getProperty("line.separator")).append("  <meta name=\"copyright\" content=\"Copyright 2009-2010\">");
        header = header.append(System.getProperty("line.separator")).append("  <link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"/>");
        header = header.append(System.getProperty("line.separator")).append("  <title>").append(resourceBundle.getString("title.text")).append("</title>");
        header = header.append(System.getProperty("line.separator")).append("</head>");
        header = header.append(System.getProperty("line.separator")).append("<body>");
        header = header.append(System.getProperty("line.separator")).append("  <div id=\"header\">");
        header = header.append(System.getProperty("line.separator")).append("    <a href=\"").append(ResourceBundle.getBundle("booknaviger/resources/Application").getString("appHomepage")).append("\" target=\"_blank\"><img src=\"images/header.png\" alt=\"BookNaviger\" /></a>");
        header = header.append(System.getProperty("line.separator")).append("    <p>");
        header = header.append(System.getProperty("line.separator")).append("    Généré par <a href=\"").append(ResourceBundle.getBundle("booknaviger/resources/Application").getString("appHomepage")).append("\" target=\"_blank\">BookNaviger</a> v").append(ResourceBundle.getBundle("booknaviger/resources/Application").getString("appVersion"));
        header = header.append(System.getProperty("line.separator")).append("    <br />");
        header = header.append(System.getProperty("line.separator")).append("    ").append(DateFormat.getDateTimeInstance().format(new Date()));
        header = header.append(System.getProperty("line.separator")).append("  </div>");
        bw.write(header.toString());
        bw.newLine();
    }
    
    private void createTOCHead() throws IOException {
        StringBuilder content = new StringBuilder("  <div id=\"contents\">");
        // TOC
        content = content.append(System.getProperty("line.separator")).append("    <div id=\"tableOfContents\">");
        content = content.append(System.getProperty("line.separator")).append("      <table align=\"center\" cellspacing=\"0\">");
        content = content.append(System.getProperty("line.separator")).append("        <thead>");
        content = content.append(System.getProperty("line.separator")).append("          <td colspan=\"4\">").append(resourceBundle.getString("TocTitle.text")).append("</td>");
        content = content.append(System.getProperty("line.separator")).append("        </thead>");
        content = content.append(System.getProperty("line.separator")).append("        <tbody>");
        pageXWriter.write(content.toString());
        pageXWriter.newLine();
    }

    private void createContent() throws IOException {
        StringBuilder content = new StringBuilder();
        createTOCHead();
        String[] series = listSeries();
        setProgress(1);
        publish(resourceBundle.getString("TOCgen.text"));
        if (advancedMode) {
            int i;
            for (i = 0; i < series.length; i++) {
                if (i%3 == 0 && i != 0) { // TODO: changer 3 par 40 !!!!
                    if (i/3 > 0) {
                        pageXWriter.write(content.toString());
                        pageXWriter.newLine();
                        createTOCFoot();
                        createFooter();
                        pageXWriter.close();
                    }
                    pageXFile = new File(reportFolder + "page" + i/3 + ".html");
                    pageXWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pageXFile), "UTF-8"));
                    createHeader(pageXWriter);
                    createTOCHead();
                    content = new StringBuilder();
                }
                String[] albums = listAlbums(series[i]);
                createThumbnail(series[i], albums[0], i, 0);
                if (i%4 == 0) {
                    if (i != 0) {
                        content = content.append(System.getProperty("line.separator")).append("          </tr>");
                    }
                    content = content.append(System.getProperty("line.separator")).append("          <tr>");
                }
                content = content.append(System.getProperty("line.separator")).append("            <td>");
                content = content.append(System.getProperty("line.separator")).append("              <a href=\"Comic").append(i).append(".html\"><img src=\"thumbnails/Comic").append(i).append("-0.png\" alt=\"").append(series[i]).append("\" title=\"").append(series[i]).append("\" class=\"thumbnail\" /></a>");
                content = content.append(System.getProperty("line.separator")).append("            </td>");
                publish(resourceBundle.getString("TOCgen.text") + " : " + series[i]);
                createAdvancedContent(series[i], albums, i);
                if (cancelAsked) {
                    return;
                }
            }
            for (; i%4 != 0; i++) {
                content = content.append(System.getProperty("line.separator")).append("            <td>");
                content = content.append(System.getProperty("line.separator")).append("              <img src=\"images/emptyThumbnail.png\" class=\"thumbnail\" />");
                content = content.append(System.getProperty("line.separator")).append("            </td>");
            }
            content = content.append(System.getProperty("line.separator")).append("          </tr>");
        } else {
            for (int i = 0; i < series.length; i++) {
                if (i%8 == 0) {
                    if (i != 0) {
                        content = content.append(System.getProperty("line.separator")).append("          </tr>");
                    }
                    content = content.append(System.getProperty("line.separator")).append("          <tr class=\"pair\">");
                } else if (i%4 == 0) {
                    content = content.append(System.getProperty("line.separator")).append("          </tr>");
                    content = content.append(System.getProperty("line.separator")).append("          <tr class=\"impair\">");
                }
                content = content.append(System.getProperty("line.separator")).append("            <td>");
                content = content.append(System.getProperty("line.separator")).append("              <a href=\"#Comic").append(i).append("\">").append(series[i]).append("</a>");
                content = content.append(System.getProperty("line.separator")).append("            </td>");
                
                setProgress(i * 100 / (nbrOfSeries + nbrOfAlbums));
                publish(resourceBundle.getString("TOCgen.text") + " : " + series[i]);
                if (cancelAsked) {
                    return;
                }
            }
            content = content.append(System.getProperty("line.separator")).append("          </tr>");
        }
        pageXWriter.write(content.toString());
        pageXWriter.newLine();
        content = new StringBuilder();
        createTOCFoot();
        // Listing
        if (!advancedMode) {
            content = content.append(System.getProperty("line.separator")).append("    <div id=\"listing\">");
            for (int i = 0; i < series.length; i++) {
                content = content.append(System.getProperty("line.separator")).append("      <table align=\"center\" cellspacing=\"0\" id=\"Comic").append(i).append("\">");
                content = content.append(System.getProperty("line.separator")).append("        <thead>");
                String[] albums = listAlbums(series[i]);
                albums = cleanAlbumsNames(series[i], albums);
                content = content.append(System.getProperty("line.separator")).append("          <td>").append(series[i]).append(" (").append(albums.length).append(" ").append(resourceBundle.getString("nbrOfAlbums.text")).append(") - <a href=\"#header\">").append(resourceBundle.getString("menu.text")).append("</a></td>");
                content = content.append(System.getProperty("line.separator")).append("        </thead>");
                content = content.append(System.getProperty("line.separator")).append("        <tbody>");
                for (int j = 0; j < albums.length; j++) {
                    if (cancelAsked) {
                        return;
                    }
                    if (j%2 == 0) {
                        if (j != 0) {
                            content = content.append(System.getProperty("line.separator")).append("          </tr>");
                        }
                        content = content.append(System.getProperty("line.separator")).append("          <tr class=\"pair\">");
                    } else {
                        content = content.append(System.getProperty("line.separator")).append("          </tr>");
                        content = content.append(System.getProperty("line.separator")).append("          <tr class=\"impair\">");
                    }
                    content = content.append(System.getProperty("line.separator")).append("            <td>").append(albums[j]).append("</td>");
                    setProgress((++nbrOfProcessedAlbums + nbrOfSeries) * 100 / (nbrOfSeries + nbrOfAlbums));
                    publish(MessageFormat.format(resourceBundle.getString("simpleListingGen.text"), series[i], albums[j]));
                }
                content = content.append(System.getProperty("line.separator")).append("          </tr>");
                content = content.append(System.getProperty("line.separator")).append("        </tbody>");
                content = content.append(System.getProperty("line.separator")).append("        <tfoot>");
                content = content.append(System.getProperty("line.separator")).append("          <td>&nbsp;</td>");
                content = content.append(System.getProperty("line.separator")).append("        </tfoot>");
                content = content.append(System.getProperty("line.separator")).append("      </table>");
            }
            content = content.append(System.getProperty("line.separator")).append("    </div>");
        }
        publish(resourceBundle.getString("finalPhase.text"));
        content = content.append(System.getProperty("line.separator")).append("  </div>");
        pageXWriter.write(content.toString());
        pageXWriter.newLine();
    }
    
    private void createTOCFoot() throws IOException {
        StringBuilder content = new StringBuilder();
        content = content.append(System.getProperty("line.separator")).append("        </tbody>");
        content = content.append(System.getProperty("line.separator")).append("        <tfoot>");
        content = content.append(System.getProperty("line.separator")).append("          <td colspan=\"4\">Copyright &copy; Inervo</td>");
        content = content.append(System.getProperty("line.separator")).append("        </tfoot>");
        content = content.append(System.getProperty("line.separator")).append("      </table>");
        content = content.append(System.getProperty("line.separator")).append("    </div>");
        pageXWriter.write(content.toString());
        pageXWriter.newLine();
    }

    private void createAdvancedContent(String serie, String[] albums, int id) throws IOException {
        BufferedWriter comicFile;
        comicFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFolder + "Comic" + id + ".html"), "UTF-8"));
        createHeader(comicFile);
        StringBuilder content = new StringBuilder("  <div id=\"contents\">");
        content = content.append(System.getProperty("line.separator")).append("    <div id=\"listing\">");
        content = content.append(System.getProperty("line.separator")).append("      <table align=\"center\" cellspacing=\"0\">");
        content = content.append(System.getProperty("line.separator")).append("        <thead>");
        content = content.append(System.getProperty("line.separator")).append("          <td colspan=\"2\">").append(serie).append(" (").append(albums.length).append(" ").append(resourceBundle.getString("nbrOfAlbums.text")).append(") - <a href=\"index.html\">Menu</a></td>");
        content = content.append(System.getProperty("line.separator")).append("        </thead>");
        content = content.append(System.getProperty("line.separator")).append("        <tbody>");
        String[] cleanAlbumsNames = cleanAlbumsNames(serie, albums);
        for (int i = 0; i < albums.length; i++) {
            if (cancelAsked) {
                return;
            }
            if (i != 0) {
                createThumbnail(serie, albums[i], id, i);
            }
            if (i%2 == 0) {
                if (i != 0) {
                    content = content.append(System.getProperty("line.separator")).append("          </tr>");
                }
                content = content.append(System.getProperty("line.separator")).append("          <tr class=\"pair\">");
            } else {
                content = content.append(System.getProperty("line.separator")).append("          </tr>");
                content = content.append(System.getProperty("line.separator")).append("          <tr class=\"impair\">");
            }
            content = content.append(System.getProperty("line.separator")).append("            <td class=\"thumbnail\"><img src=\"thumbnails/Comic").append(id).append("-").append(i).append(".png\" alt=\"").append(albums[i]).append("\" /></td>");
            content = content.append(System.getProperty("line.separator")).append("            <td>").append(cleanAlbumsNames[i]).append("</td>");
            setProgress(++nbrOfProcessedAlbums * 100 / nbrOfAlbums);
            publish(MessageFormat.format(resourceBundle.getString("simpleListingGen.text"), serie, albums[i]));
        }
        content = content.append(System.getProperty("line.separator")).append("          </tr>");
        content = content.append(System.getProperty("line.separator")).append("        </tbody>");
        content = content.append(System.getProperty("line.separator")).append("        <tfoot>");
        content = content.append(System.getProperty("line.separator")).append("          <td colspan=\"2\">Copyright &copy; Inervo</td>");
        content = content.append(System.getProperty("line.separator")).append("        </tfoot>");
        content = content.append(System.getProperty("line.separator")).append("      </table>");
        content = content.append(System.getProperty("line.separator")).append("    </div>");
        content = content.append(System.getProperty("line.separator")).append("  </div>");
        comicFile.write(content.toString());
        comicFile.newLine();
        createSimpleFooter(comicFile);
        comicFile.close();
    }

    private void createThumbnail(String serie, String albumString, int serieId, int albumId) throws IOException {
        File album = new File(baseDir.toString() + File.separatorChar + serie + File.separatorChar + albumString);
        File destinationFile = new File(reportFolder + "thumbnails" + File.separatorChar + "Comic" + serieId + "-" + albumId + ".png");
        BufferedImage previewImage = null;
        if (album.isDirectory()) {
            previewImage = new FolderHandler(album).getImage(1);
        } else if (album.getName().toLowerCase().endsWith(".zip") || album.getName().toLowerCase().endsWith(".cbz")) {
            previewImage = new ZipHandler(album).getImage(1);
        } else if (album.getName().toLowerCase().endsWith(".rar") || album.getName().toLowerCase().endsWith(".cbr")) {
            previewImage = new RarHandler(album).getImage(1);
        } else if (album.getName().toLowerCase().endsWith(".pdf")) {
            previewImage = new PdfHandler(album).getImage(1);
        }
        BufferedImage thumbnailImage = ImageReader.getThumbnailImage(previewImage);
        previewImage.flush();
        if (thumbnailImage != null) {
            ImageIO.write(thumbnailImage, "png", destinationFile);
        }
    }

    private void createFooter() throws IOException {
        StringBuilder footer = new StringBuilder("  <div id=\"footer\">");
        footer = footer.append(System.getProperty("line.separator")).append("    ").append(nbrOfSeries).append(" ").append(resourceBundle.getString("nbrOfSeries.text"));
        footer = footer.append(System.getProperty("line.separator")).append("    <br />");
        footer = footer.append(System.getProperty("line.separator")).append("    ").append(nbrOfAlbums).append(" ").append(resourceBundle.getString("nbrOfAlbums.text"));
        footer = footer.append(System.getProperty("line.separator")).append("    <br />");
        footer = footer.append(System.getProperty("line.separator")).append("    ").append(getFolderSize(baseDir));
        footer = footer.append(System.getProperty("line.separator")).append("  </div>");
        pageXWriter.write(footer.toString());
        pageXWriter.newLine();
        createSimpleFooter(pageXWriter);
    }
    
    /**
     * Retourne la taille d'un dossier
     * @param folder dossier racine
     * @return String formatée de la taille du dossier
     */
    private String getFolderSize(File folder) {
        double size = getFolderSizeAsLong(folder);

        DecimalFormat df =new DecimalFormat("#.##");
        String sizeString = df.format(size / (1024*1024));
        if (Double.valueOf(sizeString.replace(',', '.')) > 1024) {
            return df.format(size / (1024*1024*1024)) + " " + resourceBundle.getString("GB.text");
        }
        return sizeString + " " + resourceBundle.getString("MB.text");
    }
    
    /**
     * Retourne la taille d'un dossier
     * @param folder dossier racine
     * @return long de la taille du dossier
     */
    private long getFolderSizeAsLong(File folder) {
            long foldersize = 0;

            File[] filelist = folder.listFiles();
            for (int i = 0; i < filelist.length; i++) {
                if (filelist[i].isDirectory()) {
                    foldersize += getFolderSizeAsLong(filelist[i]);
                }
                else {
                    foldersize += filelist[i].length();
                }
            }
            return foldersize;
    }

    private void createSimpleFooter(BufferedWriter bw) throws IOException {
        StringBuilder footer = new StringBuilder("</body>");
        footer = footer.append(System.getProperty("line.separator")).append("</html>");
        bw.write(footer.toString());
        bw.newLine();
    }
    

    private void copyImages() throws IOException {
        String imagefolder = reportFolder + "images";
        new File(imagefolder).mkdir();
        imagefolder = imagefolder.concat(File.separator);
        Image header = ImageIO.read(ClassLoader.getSystemResourceAsStream("booknaviger/resources/graphics/htmlReport/header.png"));
        Image background = ImageIO.read(ClassLoader.getSystemResourceAsStream("booknaviger/resources/graphics/htmlReport/background.png"));
        Image tableTray = ImageIO.read(ClassLoader.getSystemResourceAsStream("booknaviger/resources/graphics/htmlReport/table_tray.png"));
        ImageIO.write((RenderedImage) header, "png", new FileOutputStream(imagefolder + "header.png"));
        ImageIO.write((RenderedImage) background, "png", new FileOutputStream(imagefolder + "background.png"));
        ImageIO.write((RenderedImage) tableTray, "png", new FileOutputStream(imagefolder + "table_tray.png"));
        if (advancedMode) {
            Image emptyThumbnail = ImageIO.read(ClassLoader.getSystemResourceAsStream("booknaviger/resources/graphics/htmlReport/emptyThumbnail.png"));
            ImageIO.write((RenderedImage) emptyThumbnail, "png", new FileOutputStream(imagefolder + "emptyThumbnail.png"));
        }

    }

    private String[] listSeries() {
        File[] seriesFiles = baseDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();
                if (!pathname.isHidden() && pathname.isDirectory()) {
                    return true;
                }
                return false;
            }
        });
        String[] seriesNames = new String[seriesFiles.length];
        for (int i = 0; i < seriesFiles.length; i++) {
            seriesNames[i] = seriesFiles[i].getName();
        }
        nbrOfSeries = seriesFiles.length;
        Arrays.sort(seriesNames);
        return seriesNames;
    }

    private String[] listAlbums(String serieName) {
        File albumFile = new File(baseDir.getPath() + File.separatorChar + serieName);
        File[] albumsFiles = albumFile.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();
                if (!pathname.isHidden() && (pathname.isDirectory() || name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith("cbz") || name.toLowerCase().endsWith(".rar") || name.toLowerCase().endsWith(".cbr") || name.toLowerCase().endsWith(".pdf"))) {
                    return true;
                }
                return false;
            }
        });
        String[] albumsNames = new String[albumsFiles.length];
        for (int i = 0; i < albumsFiles.length; i++) {
            albumsNames[i] = albumsFiles[i].getName();
        }
        Arrays.sort(albumsNames);
        return albumsNames;
    }

    private String[] cleanAlbumsNames(String serieName, String[] albumsNames) {
        String[] cleanAlbumsNames = albumsNames.clone();
        for (int i = 0; i < albumsNames.length; i++) {
            File albumFile = new File(baseDir.getPath() + File.separatorChar + serieName + File.separatorChar + albumsNames[i]);
            if (!albumFile.isDirectory()) {
                cleanAlbumsNames[i] = albumsNames[i].substring(0, albumsNames[i].length() - 4);
            }
            else {
                cleanAlbumsNames[i] = albumsNames[i];
            }
        }
        return cleanAlbumsNames;
    }

    private void deleteHTMLReportDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i < files.length; i++) {
               if(files[i].isDirectory()) {
                 deleteHTMLReportDirectory(files[i]);
               }
               else {
                 files[i].delete();
               }
            }
            path.delete();
        }
    }

    private void findNbrOfAlbums() {
        File[] series = baseDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();
                if (!pathname.isHidden() && pathname.isDirectory()) {
                    return true;
                }
                return false;
            }
        });
        for (File serie : series) {
            nbrOfAlbums += serie.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    String name = pathname.getName();
                    if (!pathname.isHidden() && (pathname.isDirectory() || name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith("cbz") || name.toLowerCase().endsWith(".rar") || name.toLowerCase().endsWith(".cbr") || name.toLowerCase().endsWith(".pdf"))) {
                        return true;
                    }
                    return false;
                }
            }).length;
        }
    }

    /**
     * Set la demande d'annulation de la génération du rapport html
     * @param cancelAsked valeur de la demande
     */
    public void setCancelAsked(boolean cancelAsked) {
        this.cancelAsked = cancelAsked;
    }

}
