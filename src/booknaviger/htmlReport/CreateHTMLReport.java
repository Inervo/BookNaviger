/*
 */

package booknaviger.htmlReport;

import booknaviger.StaticWorld;
import booknaviger.errorhandler.KnownErrorBox;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileSystemView;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;

/**
 * @author Inervo
 *
 */
public class CreateHTMLReport extends Task<Integer, String> {

    private File baseFile = null;
    private File cssFile = null;
    private BufferedWriter baseFileWriter = null;
    private BufferedWriter cssFileWriter = null;
    private boolean advancedMode;
    private ResourceMap rm = Application.getInstance(booknaviger.BookNavigerApp.class).getContext().getResourceMap(CreateHTMLReport.class);
    private String reportFolder = null;
    private File baseDir = null;
    private int nbrOfSeries = 0;
    private int nbrOfAlbums = 0;
    private Frame bnvf = null;
    private GenerationProgress gp = null;
    private int nbrOfProcessedAlbums = 0;
    private boolean cancelAsked = false;


    /**
     * Constructeur pour la création du rapport html
     * @param advancedMode mode de création du rapport (simple vs. advanced (avec miniatures)
     * @param baseDir Dossier contenant les séries
     * @param bnvf BookNavigerView Frame
     * @param gp Instance de GenerationProgress afin de communiquer les avancements de l'execution
     */
    public CreateHTMLReport(boolean advancedMode, File baseDir, final Frame bnvf, final GenerationProgress gp) {
        super(Application.getInstance());
        this.advancedMode = advancedMode;
        this.baseDir = baseDir;
        this.bnvf = bnvf;
        this.gp = gp;
        String folder = System.getProperty("user.home");
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            folder = FileSystemView.getFileSystemView().getDefaultDirectory().toString();
        }
        folder = folder.concat(File.separatorChar + "HTMLReport");
        deleteHTMLReportDirectory(new File(folder));
        new File(folder).mkdir();
        reportFolder = folder + File.separatorChar;
        if (advancedMode)
            new File(reportFolder + "thumbnails").mkdir();
        baseFile = new File(reportFolder + "index.html");
        cssFile = new File(reportFolder + "style.css");
        addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if("progress".equals(evt.getPropertyName())) {
                    int newValue = (Integer) evt.getNewValue();
                    if (gp.getStateProgressBarValue() > newValue)
                            return;
                    gp.setStateProgressBarValue(newValue);
                }
            }
        });
    }

    /**
     * Executé à la fin de la tâche principale
     */
    @Override
    protected void finished() {
        gp.setStateLabelValue(rm.getString("finished.text"));
        gp.setStateProgressBarValue(100);
        gp.setVisible(false);
        gp.dispose();
    }

    /**
     * Gestion des strings données par l'execution de la tâche en arrière plan
     * @param chunks Liste des String d'informations
     */
    @Override
    protected void process(List<String> chunks) {
        for (String string : chunks) {
            gp.setStateLabelValue(string);
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
            baseFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(baseFile), "UTF-8"));
            cssFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cssFile), "UTF-8"));
            createCss();
            createHeader(baseFileWriter);
            createContent();
            if (cancelAsked)
                return 7;
            createFooter(baseFileWriter);
            copyImages();
        } catch (FileNotFoundException ex) {
            new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Opening_Report_Files");
        } catch (UnsupportedEncodingException ex) {
            new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Report_Encoding_Unsupported");
        } catch (IOException ex) {
            new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Reading_Report_Files_Template");
        }
        try {
            baseFileWriter.close();
            cssFileWriter.close();
        } catch (IOException ex) {
            new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Close_Report_Files");
        }
        if (cancelAsked)
            return 7;
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(new URI("file://" + reportFolder.replace('\\', '/').concat("index.html")));
                    return 0;
                }
            } catch (IOException ex) {
            } catch(URISyntaxException ex) {
                Logger.getLogger(CreateHTMLReport.class.getName()).log(Level.SEVERE, "cannot show generated report. Please check index.html in the folder " + System.getProperty("user.dir").concat(File.separatorChar + "HTMLReport"), ex);
            }
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                IndexFilePosition ifp = new IndexFilePosition(bnvf, true, reportFolder + "index.html");
                ifp.setVisible(true);
            }
        });
        return 0;
    }

    private void createCss() throws IOException {
        String css = "";
        BufferedReader initialCssFile = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("booknaviger/htmlReport/resources/style.css")));
        while(true) {
            String tampon = initialCssFile.readLine();
            if (tampon == null)
                break;
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
        header = header.append(System.getProperty("line.separator")).append("  <title>").append(rm.getString("title.text")).append("</title>");
        header = header.append(System.getProperty("line.separator")).append("</head>");
        header = header.append(System.getProperty("line.separator")).append("<body>");
        header = header.append(System.getProperty("line.separator")).append("  <div id=\"header\">");
        header = header.append(System.getProperty("line.separator")).append("    <a href=\"http://software.inervo.fr/\" target=\"_blank\"><img src=\"images/header.png\" alt=\"BookNaviger\" /></a>");
        header = header.append(System.getProperty("line.separator")).append("    <p>");
        header = header.append(System.getProperty("line.separator")).append("    Généré par <a href=\"http://software.inervo.fr/\" target=\"_blank\">BookNaviger</a> v").append(rm.getString("Application.version"));
        header = header.append(System.getProperty("line.separator")).append("    <br />");
        header = header.append(System.getProperty("line.separator")).append("    ").append(DateFormat.getDateTimeInstance().format(new Date()));
        header = header.append(System.getProperty("line.separator")).append("  </div>");
        bw.write(header.toString());
        bw.newLine();
    }

    private void createContent() throws IOException {
        StringBuilder content = new StringBuilder("  <div id=\"contents\">");
        // TOC
        content = content.append(System.getProperty("line.separator")).append("    <div id=\"tableOfContents\">");
        content = content.append(System.getProperty("line.separator")).append("      <table align=\"center\" cellspacing=\"0\">");
        content = content.append(System.getProperty("line.separator")).append("        <thead>");
        content = content.append(System.getProperty("line.separator")).append("          <td colspan=\"4\">").append(rm.getString("TocTitle.text")).append("</td>");
        content = content.append(System.getProperty("line.separator")).append("        </thead>");
        content = content.append(System.getProperty("line.separator")).append("        <tbody>");
        String[] series = listSeries();
        setProgress(1);
        publish(rm.getString("TOCgen.text"));
        if (advancedMode) {
            int i = 0;
            for (i = 0; i < series.length; i++) {
                String[] albums = listAlbums(series[i]);
                createThumbnail(series[i], albums[0], i, 0);
                if (i%4 == 0) {
                    if (i != 0)
                        content = content.append(System.getProperty("line.separator")).append("          </tr>");
                    content = content.append(System.getProperty("line.separator")).append("          <tr>");
                }
                content = content.append(System.getProperty("line.separator")).append("            <td>");
                content = content.append(System.getProperty("line.separator")).append("              <a href=\"Comic").append(i).append(".html\"><img src=\"thumbnails/Comic").append(i).append("-0.png\" alt=\"").append(series[i]).append("\" title=\"").append(series[i]).append("\" class=\"thumbnail\" /></a>");
                content = content.append(System.getProperty("line.separator")).append("            </td>");
                publish(rm.getString("TOCgen.text") + " : " + series[i]);
                createAdvancedContent(series[i], albums, i);
                if (cancelAsked)
                    return;
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
                    if (i != 0)
                        content = content.append(System.getProperty("line.separator")).append("          </tr>");
                    content = content.append(System.getProperty("line.separator")).append("          <tr class=\"pair\">");
                } else if (i%4 == 0) {
                    content = content.append(System.getProperty("line.separator")).append("          </tr>");
                    content = content.append(System.getProperty("line.separator")).append("          <tr class=\"impair\">");
                }
                content = content.append(System.getProperty("line.separator")).append("            <td>");
                content = content.append(System.getProperty("line.separator")).append("              <a href=\"#Comic").append(i).append("\">").append(series[i]).append("</a>");
                content = content.append(System.getProperty("line.separator")).append("            </td>");
                setProgress(i, 0, nbrOfSeries + nbrOfAlbums);
                publish(rm.getString("TOCgen.text") + " : " + series[i]);
                if (cancelAsked)
                    return;
            }
            content = content.append(System.getProperty("line.separator")).append("          </tr>");
        }
        content = content.append(System.getProperty("line.separator")).append("        </tbody>");
        content = content.append(System.getProperty("line.separator")).append("        <tfoot>");
        content = content.append(System.getProperty("line.separator")).append("          <td colspan=\"4\">Copyright &copy; Inervo</td>");
        content = content.append(System.getProperty("line.separator")).append("        </tfoot>");
        content = content.append(System.getProperty("line.separator")).append("      </table>");
        content = content.append(System.getProperty("line.separator")).append("    </div>");
        // Listing
        if (!advancedMode) {
            content = content.append(System.getProperty("line.separator")).append("    <div id=\"listing\">");
            for (int i = 0; i < series.length; i++) {
                content = content.append(System.getProperty("line.separator")).append("      <table align=\"center\" cellspacing=\"0\" id=\"Comic").append(i).append("\">");
                content = content.append(System.getProperty("line.separator")).append("        <thead>");
                String[] albums = listAlbums(series[i]);
                albums = cleanAlbumsNames(series[i], albums);
                content = content.append(System.getProperty("line.separator")).append("          <td>").append(series[i]).append(" (").append(albums.length).append(" ").append(rm.getString("nbrOfAlbums.text")).append(") - <a href=\"#header\">").append(rm.getString("menu.text")).append("</a></td>");
                content = content.append(System.getProperty("line.separator")).append("        </thead>");
                content = content.append(System.getProperty("line.separator")).append("        <tbody>");
                for (int j = 0; j < albums.length; j++) {
                    if (cancelAsked)
                        return;
                    if (j%2 == 0) {
                        if (j != 0)
                            content = content.append(System.getProperty("line.separator")).append("          </tr>");
                        content = content.append(System.getProperty("line.separator")).append("          <tr class=\"pair\">");
                    } else {
                        content = content.append(System.getProperty("line.separator")).append("          </tr>");
                        content = content.append(System.getProperty("line.separator")).append("          <tr class=\"impair\">");
                    }
                    content = content.append(System.getProperty("line.separator")).append("            <td>").append(albums[j]).append("</td>");
                    setProgress(++nbrOfProcessedAlbums + nbrOfSeries, 0, nbrOfAlbums + nbrOfSeries);
                    publish(rm.getString("simpleListingGen.text", series[i], albums[j]));
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
        publish(rm.getString("finalPhase.text"));
        content = content.append(System.getProperty("line.separator")).append("  </div>");
        baseFileWriter.write(content.toString());
        baseFileWriter.newLine();
    }

    private void createAdvancedContent(String serie, String[] albums, int id) throws IOException {
        BufferedWriter comicFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFolder + "Comic" + id + ".html"), "UTF-8"));
        createHeader(comicFile);
        StringBuilder content = new StringBuilder("  <div id=\"contents\">");
        content = content.append(System.getProperty("line.separator")).append("    <div id=\"listing\">");
        content = content.append(System.getProperty("line.separator")).append("      <table align=\"center\" cellspacing=\"0\">");
        content = content.append(System.getProperty("line.separator")).append("        <thead>");
        content = content.append(System.getProperty("line.separator")).append("          <td colspan=\"2\">").append(serie).append(" (").append(albums.length).append(" ").append(rm.getString("nbrOfAlbums.text")).append(") - <a href=\"index.html\">Menu</a></td>");
        content = content.append(System.getProperty("line.separator")).append("        </thead>");
        content = content.append(System.getProperty("line.separator")).append("        <tbody>");
        String[] cleanAlbumsNames = cleanAlbumsNames(serie, albums);
        for (int i = 0; i < albums.length; i++) {
            if (cancelAsked)
                return;
            if (i != 0)
                createThumbnail(serie, albums[i], id, i);
            if (i%2 == 0) {
                if (i != 0)
                    content = content.append(System.getProperty("line.separator")).append("          </tr>");
                content = content.append(System.getProperty("line.separator")).append("          <tr class=\"pair\">");
            } else {
                content = content.append(System.getProperty("line.separator")).append("          </tr>");
                content = content.append(System.getProperty("line.separator")).append("          <tr class=\"impair\">");
            }
            content = content.append(System.getProperty("line.separator")).append("            <td class=\"thumbnail\"><img src=\"thumbnails/Comic").append(id).append("-").append(i).append(".png\" alt=\"").append(albums[i]).append("\" /></td>");
            content = content.append(System.getProperty("line.separator")).append("            <td>").append(cleanAlbumsNames[i]).append("</td>");
            setProgress(++nbrOfProcessedAlbums, 0, nbrOfAlbums);
            publish(rm.getString("simpleListingGen.text", serie, albums[i]));
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
        Image realSizeImage = extractFirstImage(album);
        if (realSizeImage == null)
            return;
        int srcWidth = realSizeImage.getWidth(null);
        int srcHeight = realSizeImage.getHeight(null);
        float ratio = (float)srcHeight / (float)srcWidth;
        int destWidth = 300;
        int destHeight = (int) (300 * ratio);
        BufferedImage bi = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bi.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(realSizeImage, 0, 0, destWidth, destHeight, null);
        g2d.dispose();
        ImageIO.write((RenderedImage) bi, "png", destinationFile);
    }

    @SuppressWarnings("unchecked")
    private Image extractFirstImage(final File album) {
        Image firstImage = null;
        if (album.isDirectory()) {
            File[] pictures = album.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return StaticWorld.typeIsImage(pathname.getName());
                }
            });
            Arrays.sort(pictures);
            if (StaticWorld.typeSupportToolkit(pictures[0].getName())) {
                firstImage = Toolkit.getDefaultToolkit().createImage(pictures[0].toString());
                MediaTracker mt = new MediaTracker(new Component() {});
                mt.addImage(firstImage, 0);
                try {
                    mt.waitForID(0);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CreateHTMLReport.class.getName()).log(Level.SEVERE, "wait for image loading interrupted", ex);
                }
                if (firstImage.getWidth(null) == -1 && firstImage.getHeight(null) == -1) {
                    new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", pictures[0].toString());
                    firstImage = null;
                }
            } else if (StaticWorld.typeSupportImageIO(pictures[0].getName())) {
                try {
                    firstImage = ImageIO.read(pictures[0]).getScaledInstance(-1, -1, 1);
                } catch (IOException ex) {
                    new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", pictures[0].toString());
                } catch (NullPointerException ex) {
                    new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", pictures[0].toString());
                }
            }
        } else if (album.getName().toLowerCase().endsWith(".zip") || album.getName().toLowerCase().endsWith(".cbz")) {
            ZipFile zf = null;
            InputStream is = null;

            try {
                try {
                    zf = new ZipFile(album, "IBM437");
                } catch (IOException ex) {
                    new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Read_Zip", album.toString());
                    return null;
                }
                Enumeration zipEntries = zf.getEntries();
                List<ZipArchiveEntry> zipEntriesList = Collections.list(zipEntries);
                Collections.sort(zipEntriesList, new Comparator<ZipArchiveEntry>() {

                @Override
                public int compare(ZipArchiveEntry o1, ZipArchiveEntry o2) {
                    return o1.getName().compareTo(o2.getName());
                }
                });
                ListIterator it = zipEntriesList.listIterator();
                while(it.hasNext()) {
                    ZipArchiveEntry currentEntry = (ZipArchiveEntry) it.next();
                    if (!currentEntry.isDirectory() && StaticWorld.typeIsImage(currentEntry.getName())) {
                        try {
                            is = zf.getInputStream(currentEntry);
                            firstImage = ImageIO.read(is).getScaledInstance(-1, -1, 1);
                            is.close();
                        } catch (ZipException ex) {
                            new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Unsupported_Zip_Compression", currentEntry.getName());
                        } catch (IOException ex) {
                            new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", currentEntry.getName());
                        } catch (NullPointerException ex) {
                            new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", currentEntry.getName());
                        }
                        break;
                    }
                }
            } finally {
                try {
                    if (zf != null)
                        zf.close();
                } catch (IOException ex) {
                    new KnownErrorBox(bnvf, KnownErrorBox.WARNING_lOGO, "Warning_Close_File", album.toString());
                }
            }
        } else if (album.getName().toLowerCase().endsWith(".rar") || album.getName().toLowerCase().endsWith(".cbr")) {
            Archive archive = null;

            class ExtractFileFromRarToOs extends Thread {
                Archive a = null;
                FileHeader fh = null;
                OutputStream os = null;

                public ExtractFileFromRarToOs(Archive a, FileHeader fh, OutputStream os) {
                    this.a = a;
                    this.fh = fh;
                    this.os = os;
                }

                @Override
                public void run() {
                    try {
                        a.extractFile(fh, os);
                    } catch (RarException ex) {
                        new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Rar_Entity_Malformed", fh.getFileNameString());
                    } finally {
                        try {
                            os.close();
                        } catch (IOException ex) {
                            new KnownErrorBox(bnvf, KnownErrorBox.WARNING_lOGO, "Warning_Close_Entity_Stream", fh.getFileNameString());
                        }
                    }
                }

            }
            try {
                Logger.getLogger(Archive.class.getName()).setFilter(new Filter() {

                    @Override
                    public boolean isLoggable(LogRecord record) {
                        if (record.getMessage().equals("exception in archive constructor maybe file is encrypted or currupt")) {
                            new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Read_Rar", album.toString());
                            return false;
                        }
                        return true;
                    }
                });
                archive = new Archive(new File(album.toString()));
            } catch (RarException ex) {
                new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Read_Rar", album.toString());
            } catch (IOException ex) {
                new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Read_Rar", album.toString());
            }
            if (archive != null) {
                List<FileHeader> fhl = archive.getFileHeaders();
                Collections.sort(fhl, new Comparator<FileHeader>() {

                    @Override
                    public int compare(FileHeader o1, FileHeader o2) {
                        return o1.getFileNameString().compareTo(o2.getFileNameString());
                    }
                });
                PipedInputStream pis = new PipedInputStream();
                PipedOutputStream pos = null;
                try {
                    pos = new PipedOutputStream(pis);
                } catch (IOException ex) {
                    Logger.getLogger(CreateHTMLReport.class.getName()).log(Level.SEVERE, null, ex);
                }
                ListIterator it = fhl.listIterator();
                while(it.hasNext()) {
                    FileHeader currentEntry = (FileHeader) it.next();
                    if (!currentEntry.isDirectory() && StaticWorld.typeIsImage(currentEntry.getFileNameString())) {
                        new ExtractFileFromRarToOs(archive, currentEntry, pos).start();
                        try {
                            firstImage = ImageIO.read(pis).getScaledInstance(-1, -1, 1);
                            pis.close();
                        } catch (IOException ex) {
                            new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", currentEntry.getFileNameString());
                        } catch (NullPointerException ex) {
                            new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Reading_Image", currentEntry.getFileNameString());
                        }
                        break;
                    }
                }
            }
            try {
                if (archive != null)
                    archive.close();
            } catch (IOException ex) {
                new KnownErrorBox(bnvf, KnownErrorBox.WARNING_lOGO, "Warning_Close_File", album.toString());
            }
        } else if (album.getName().toLowerCase().endsWith(".pdf")) {
            RandomAccessFile raf = null;
            FileChannel channel = null;
            try {
                raf = new RandomAccessFile(album, "r");
                channel = raf.getChannel();
                ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                PDFFile pdfFile = new PDFFile(buf);
                PDFPage page = pdfFile.getPage(0);
                Rectangle rect = new Rectangle(page.getBBox().getBounds());
                firstImage = page.getImage(rect.width * 2, rect.height * 2, //width & height
                    page.getBBox(), // clip rect
                    null, // null for the ImageObserver
                    true, // fill background with white
                    true  // block until drawing is done
                    );
            } catch (IOException ex) {
                new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Read_Pdf", album.toString());
            } catch (IllegalArgumentException ex) {
                new KnownErrorBox(bnvf, KnownErrorBox.ERROR_LOGO, "Error_Read_Pdf", album.toString());
            }
            try {
                channel.close();
                raf.close();
            } catch (IOException ex) {
                new KnownErrorBox(bnvf, KnownErrorBox.WARNING_lOGO, "Warning_Close_File", album.toString());
            }
        }
        return firstImage;
    }



    private void createFooter(BufferedWriter bw) throws IOException {
        StringBuilder footer = new StringBuilder("  <div id=\"footer\">");
        footer = footer.append(System.getProperty("line.separator")).append("    ").append(nbrOfSeries).append(" ").append(rm.getString("nbrOfSeries.text"));
        footer = footer.append(System.getProperty("line.separator")).append("    <br />");
        footer = footer.append(System.getProperty("line.separator")).append("    ").append(nbrOfAlbums).append(" ").append(rm.getString("nbrOfAlbums.text"));
        footer = footer.append(System.getProperty("line.separator")).append("    <br />");
        footer = footer.append(System.getProperty("line.separator")).append("    ").append(StaticWorld.getFolderSize(baseDir));
        footer = footer.append(System.getProperty("line.separator")).append("  </div>");
        bw.write(footer.toString());
        bw.newLine();
        createSimpleFooter(bw);
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
        Image header = ImageIO.read(ClassLoader.getSystemResourceAsStream("booknaviger/htmlReport/resources/header.png"));
        Image background = ImageIO.read(ClassLoader.getSystemResourceAsStream("booknaviger/htmlReport/resources/background.png"));
        Image tableTray = ImageIO.read(ClassLoader.getSystemResourceAsStream("booknaviger/htmlReport/resources/table_tray.png"));
        ImageIO.write((RenderedImage) header, "png", new FileOutputStream(imagefolder + "header.png"));
        ImageIO.write((RenderedImage) background, "png", new FileOutputStream(imagefolder + "background.png"));
        ImageIO.write((RenderedImage) tableTray, "png", new FileOutputStream(imagefolder + "table_tray.png"));
        if (advancedMode) {
            Image emptyThumbnail = ImageIO.read(ClassLoader.getSystemResourceAsStream("booknaviger/htmlReport/resources/emptyThumbnail.png"));
            ImageIO.write((RenderedImage) emptyThumbnail, "png", new FileOutputStream(imagefolder + "emptyThumbnail.png"));
        }

    }

    private String[] listSeries() {
        File[] seriesFiles = baseDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName();
                if (!pathname.isHidden() && pathname.isDirectory())
                    return true;
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
                if (!pathname.isHidden() && (pathname.isDirectory() || name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith("cbz") || name.toLowerCase().endsWith(".rar") || name.toLowerCase().endsWith(".cbr") || name.toLowerCase().endsWith(".pdf")))
                    return true;
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
            if (!albumFile.isDirectory())
                cleanAlbumsNames[i] = albumsNames[i].substring(0, albumsNames[i].length() - 4);
            else
                cleanAlbumsNames[i] = albumsNames[i];
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
                if (!pathname.isHidden() && pathname.isDirectory())
                    return true;
                return false;
            }
        });
        for (File serie : series) {
            nbrOfAlbums += serie.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    String name = pathname.getName();
                    if (!pathname.isHidden() && (pathname.isDirectory() || name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith("cbz") || name.toLowerCase().endsWith(".rar") || name.toLowerCase().endsWith(".cbr") || name.toLowerCase().endsWith(".pdf")))
                        return true;
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
