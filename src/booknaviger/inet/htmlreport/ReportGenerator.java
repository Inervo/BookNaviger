/*
 */

package booknaviger.inet.htmlreport;

import booknaviger.booksfolder.BooksFolderAnalyser;
import booknaviger.exceptioninterface.InfoInterface;
import booknaviger.osbasics.OSBasics;
import booknaviger.picturehandler.FolderHandler;
import booknaviger.picturehandler.ImageReader;
import booknaviger.picturehandler.PdfHandler;
import booknaviger.picturehandler.RarHandler;
import booknaviger.picturehandler.ZipHandler;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileSystemView;

/**
 * Class used to create the html report
 * @author Inervo
 */
public class ReportGenerator extends SwingWorker<Integer, String> {

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
     * Constructor of the html reporting generator
     * @param advancedMode mode of the report.<br />false for simple<br />true for advanced with thumbnail
     * @param baseDir Folder containing the books
     * @param gp Instance of GenerationProgress to communicate the current status
     */
    public ReportGenerator(boolean advancedMode, File baseDir, final GenerationProgress gp) {
        super();
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "ReportGenerator", new Object[] {advancedMode, baseDir, gp});
        this.advancedMode = advancedMode;
        this.baseDir = baseDir;
        this.generationProgressDialog = gp;
        Logger.getLogger(ReportGenerator.class.getName()).log(Level.INFO, "Starting the html report");
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
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "ReportGenerator");
    }

    @Override
    protected void done() {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "done");
        generationProgressDialog.setActionLabelValue(resourceBundle.getString("finished.text"));
        generationProgressDialog.setActionProgressBarValue(100);
        generationProgressDialog.setVisible(false);
        generationProgressDialog.dispose();
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "done");
    }

    @Override
    protected void process(List<String> chunks) {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "process", chunks);
        for (String string : chunks) {
            generationProgressDialog.setActionLabelValue(string);
        }
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "process");
    }

    @Override
    protected Integer doInBackground() {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "doInBackground");
        nbrOfAlbums = 0;
        nbrOfProcessedAlbums = 0;
        findNbrOfAlbums();
        try {
            pageXWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(reportFolder + "index.html")), "UTF-8"));
            cssFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(reportFolder + "style.css")), "UTF-8"));
            createCss();
            copyImages();
            createHeader(pageXWriter);
            createContent();
            if (cancelAsked) {
                Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "doInBackground", 7);
                return 7;
            }
            createFooter();
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, "Error with the files during the reporting", ex);
            new InfoInterface(InfoInterface.ERROR, "report-files");
        } catch (IOException ex) {
            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, "IO error during the reporting", ex);
            new InfoInterface(InfoInterface.ERROR, "report-IO");
        }
        try {
            pageXWriter.close();
            cssFileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(ReportGenerator.class.getName()).log(Level.SEVERE, "IO error while closing the report files", ex);
            new InfoInterface(InfoInterface.ERROR, "report-IO");
        }
        if (cancelAsked) {
            Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "doInBackground", 7);
            return 7;
        }
        OSBasics.openFile(reportFolder);
        OSBasics.openURI("file://" + reportFolder.replace('\\', '/').concat("index.html"));
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "doInBackground", 0);
        return 0;
    }

    /**
     * Create the css file from the one in the resource
     * @throws IOException if the one from the resource cannot be read
     */
    private void createCss() throws IOException {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "createCss");
        BufferedReader initialCssFile = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("booknaviger/resources/graphics/htmlreport/style.css")));
        while(true) {
            String tampon = initialCssFile.readLine();
            if (tampon == null) {
                break;
            }
            cssFileWriter.write(tampon);
            cssFileWriter.newLine();
        }
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "createCss");
    }

    /**
     * Create the header of the html file
     * @param bw BufferedWriter to write the header to
     * @throws IOException If an error occur during the writing of the BufferedWriter
     */
    private void createHeader(BufferedWriter bw) throws IOException {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "createHeader", bw);
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
        header = header.append(System.getProperty("line.separator")).append("  <div id=\"contents\">");
        bw.write(header.toString());
        bw.newLine();
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "createHeader");
    }
    
    /**
     * Create the TableOfContent header of the html file
     * @throws IOException If an error occur during the writing of the BufferedWriter
     */
    private void createTOCHead() throws IOException {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "createTOCHead");
        StringBuilder content = new StringBuilder();
        // TOC
        content = content.append(System.getProperty("line.separator")).append("    <div id=\"tableOfContents\">");
        content = content.append(System.getProperty("line.separator")).append("      <table align=\"center\" cellspacing=\"0\">");
        content = content.append(System.getProperty("line.separator")).append("        <thead>");
        content = content.append(System.getProperty("line.separator")).append("          <td colspan=\"4\">").append(resourceBundle.getString("TocTitle.text")).append("</td>");
        content = content.append(System.getProperty("line.separator")).append("        </thead>");
        content = content.append(System.getProperty("line.separator")).append("        <tbody>");
        pageXWriter.write(content.toString());
        pageXWriter.newLine();
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "createTOCHead");
    }
    
    /**
     * Create the menu to navigate between the pages
     * @param numberOfPages the total number of pages during this report
     * @param currentPage the current page we're creating the header for
     * @throws IOException If an error occur during the writing of the BufferedWriter
     */
    private void createNavigationMenu(int numberOfPages, int currentPage) throws IOException {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "createNavigationMenu", new Object[] {numberOfPages, currentPage});
        if (numberOfPages==0) {
            return;
        }
        StringBuilder content = new StringBuilder();
        content = content.append(System.getProperty("line.separator")).append("    <table align=\"center\" id=\"navigation\">");
        content = content.append(System.getProperty("line.separator")).append("      <tbody>");
        content = content.append(System.getProperty("line.separator")).append("        <tr>");
        content = content.append(System.getProperty("line.separator")).append("          <td>");
        content = content.append(System.getProperty("line.separator")).append("            ");
        if (currentPage != 0) {
            if (currentPage == 1) {
                content = content.append("&nbsp;<a href=\"index.html\">").append(resourceBundle.getString("previousPage.text")).append("</a>");
            } else {
                content = content.append("&nbsp;<a href=\"page").append(currentPage - 1).append(".html\">").append(resourceBundle.getString("previousPage.text")).append("</a>");
            }
        }
        for (int i = 0; i <= numberOfPages; i++) {
            if (currentPage == i) {
                content = content.append("&nbsp;<u>").append(i).append("</u>");
            } else {
                if (i == 0) {
                    content = content.append("&nbsp;<a href=\"index.html\">").append(i).append("</a>");
                } else {
                    content = content.append("&nbsp;<a href=\"page").append(i).append(".html\">").append(i).append("</a>");
                }
            }
        }
        if (currentPage != numberOfPages) {
            content = content.append("&nbsp;<a href=\"page").append(currentPage + 1).append(".html\">").append(resourceBundle.getString("nextPage.text")).append("</a>");
        }
        content = content.append(System.getProperty("line.separator")).append("          </td>");
        content = content.append(System.getProperty("line.separator")).append("        </tr>");
        content = content.append(System.getProperty("line.separator")).append("      </tbody>");
        content = content.append(System.getProperty("line.separator")).append("    </table>");
        pageXWriter.write(content.toString());
        pageXWriter.newLine();
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "createNavigationMenu");
    }

    /**
     * Create the main content of the html page
     * @throws IOException If an error occur during the writing of the BufferedWriter
     */
    private void createContent() throws IOException {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "createContent");
        StringBuilder content = new StringBuilder();
        String[] series = listSeries();
        if (advancedMode) {
            createNavigationMenu((series.length-1) / 20, 0);
        }
        createTOCHead();
        setProgress(1);
        publish(resourceBundle.getString("TOCgen.text"));
        if (advancedMode) {
            int i;
            for (i = 0; i < series.length; i++) {
                if (i%20 == 0 && i != 0) {
                    if (i/20 > 0) {
                        content = content.append(System.getProperty("line.separator")).append("          </tr>");
                        pageXWriter.write(content.toString());
                        pageXWriter.newLine();
                        createTOCFoot();
                        createNavigationMenu((series.length-1) / 20, i/20 - 1);
                        pageXWriter.write(System.getProperty("line.separator") + "  </div>");
                        pageXWriter.newLine();
                        createFooter();
                        pageXWriter.close();
                    }
                    pageXFile = new File(reportFolder + "page" + i/20 + ".html");
                    pageXWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pageXFile), "UTF-8"));
                    createHeader(pageXWriter);
                    createNavigationMenu((series.length-1) / 20, i/20);
                    createTOCHead();
                    content = new StringBuilder();
                }
                String[] albums = listAlbums(series[i]);
                if (albums.length > 0) {
                    createThumbnail(series[i], albums[0], i, 0);
                } else {
                    Image noImageThumbnail = ImageIO.read(ClassLoader.getSystemResourceAsStream("booknaviger/resources/graphics/htmlreport/noImageAvailable.png"));
                    ImageIO.write((RenderedImage) noImageThumbnail, "png", new FileOutputStream(new File(reportFolder + "thumbnails" + File.separatorChar + "Comic" + i + "-0.png")));
                }
                if (i%4 == 0) {
                    if (i != 0 && i%20 != 0) {
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
        if (advancedMode) {
            createNavigationMenu((series.length-1) / 20, (series.length-1)/20);
        }
        // Listing
        if (!advancedMode) {
            content = content.append(System.getProperty("line.separator")).append("    <div id=\"listing\">");
            for (int i = 0; i < series.length; i++) {
                content = content.append(System.getProperty("line.separator")).append("      <table align=\"center\" cellspacing=\"0\" id=\"Comic").append(i).append("\">");
                content = content.append(System.getProperty("line.separator")).append("        <thead>");
                String[] albums = listAlbums(series[i]);
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
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "createContent");
    }
    
    /**
     * Create the TableOfContent footer
     * @throws IOException If an error occur during the writing of the BufferedWriter
     */
    private void createTOCFoot() throws IOException {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "createTOCFoot");
        StringBuilder content = new StringBuilder();
        content = content.append(System.getProperty("line.separator")).append("        </tbody>");
        content = content.append(System.getProperty("line.separator")).append("        <tfoot>");
        content = content.append(System.getProperty("line.separator")).append("          <td colspan=\"4\">Copyright &copy; Inervo</td>");
        content = content.append(System.getProperty("line.separator")).append("        </tfoot>");
        content = content.append(System.getProperty("line.separator")).append("      </table>");
        content = content.append(System.getProperty("line.separator")).append("    </div>");
        pageXWriter.write(content.toString());
        pageXWriter.newLine();
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "createTOCFoot");
    }

    /**
     * When in advance mode, create the webpage specific to a serie
     * @param serie the serie name we create the webpage for
     * @param albums the albums this serie have
     * @param id unique id of this serie to create the html page name
     * @throws IOException If an error occur during the writing the file
     */
    private void createAdvancedContent(String serie, String[] albums, int id) throws IOException {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "createAdvancedContent", new Object[] {serie, albums, id});
        BufferedWriter comicFile;
        comicFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFolder + "Comic" + id + ".html"), "UTF-8"));
        createHeader(comicFile);
        StringBuilder content = new StringBuilder();
        content = content.append(System.getProperty("line.separator")).append("    <div id=\"listing\">");
        content = content.append(System.getProperty("line.separator")).append("      <table align=\"center\" cellspacing=\"0\">");
        content = content.append(System.getProperty("line.separator")).append("        <thead>");
        content = content.append(System.getProperty("line.separator")).append("          <td colspan=\"2\">").append(serie).append(" (").append(albums.length).append(" ").append(resourceBundle.getString("nbrOfAlbums.text")).append(") - <a href=\"index.html\">Menu</a></td>");
        content = content.append(System.getProperty("line.separator")).append("        </thead>");
        content = content.append(System.getProperty("line.separator")).append("        <tbody>");
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
            content = content.append(System.getProperty("line.separator")).append("            <td>").append(albums[i]).append("</td>");
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
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "createAdvancedContent");
    }

    /**
     * Create the thumbnail to a specific serie and album
     * @param serie the serie name
     * @param albumString the album name
     * @param serieId the serie unique id (used for file name)
     * @param albumId the album unique id (used for file name)
     * @throws IOException If an error occur during the creation of the thumbnail
     */
    private void createThumbnail(String serie, String albumString, int serieId, int albumId) throws IOException {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "createThumbnail", new Object[] {serie, albumString, serieId, albumId});
        File album = new File(baseDir.toString() + File.separatorChar + serie + File.separatorChar + albumString);
        File destinationFile = new File(reportFolder + "thumbnails" + File.separatorChar + "Comic" + serieId + "-" + albumId + ".png");
        BufferedImage previewImage = null;
        try {
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
        } catch(Exception ex) {
            Image noImageThumbnail = ImageIO.read(ClassLoader.getSystemResourceAsStream("booknaviger/resources/graphics/htmlreport/noImageAvailable.png"));
            ImageIO.write((RenderedImage) noImageThumbnail, "png", new FileOutputStream(destinationFile));
        }
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "createThumbnail");
    }

    /**
     * Create the footer of the html file
     * @throws IOException If an error occur during the writing of the BufferedWriter
     */
    private void createFooter() throws IOException {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "createFooter");
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
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "createFooter");
    }
    
    /**
     * Get a folder size
     * @param folder folder to get the size from
     * @return Formated string of the folder size
     */
    private String getFolderSize(File folder) {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "getFolderSize", folder);
        double size = getFolderSizeAsLong(folder);

        DecimalFormat df =new DecimalFormat("#.##");
        String sizeString = df.format(size / (1024*1024));
        if (Double.valueOf(sizeString.replace(',', '.')) > 1024) {
            return df.format(size / (1024*1024*1024)) + " " + resourceBundle.getString("GB.text");
        }
        sizeString = sizeString + " " + resourceBundle.getString("MB.text");
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "getFolderSize", sizeString);
        return sizeString;
    }
    
    /**
     * Return the folder size
     * @param folder folder to get the size from
     * @return Size of the folder as long
     */
    private long getFolderSizeAsLong(File folder) {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "getFolderSizeAsLong", folder);
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
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "getFolderSize", foldersize);
        return foldersize;
    }

    /**
     * Create the basic part of the footer
     * @param bw The bufferedWriter to write the footer to.
     * @throws IOException If an error occur during the writing of the BufferedWriter
     */
    private void createSimpleFooter(BufferedWriter bw) throws IOException {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "createSimpleFooter", bw);
        StringBuilder footer = new StringBuilder("</body>");
        footer = footer.append(System.getProperty("line.separator")).append("</html>");
        bw.write(footer.toString());
        bw.newLine();
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "createSimpleFooter");
    }
    
    /**
     * Copy the images from the resources to the destination report folder
     * @throws IOException If an error occur during the loading from the resources
     */
    private void copyImages() throws IOException {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "copyImages");
        String imagefolder = reportFolder + "images";
        new File(imagefolder).mkdir();
        imagefolder = imagefolder.concat(File.separator);
        Image header = ImageIO.read(ClassLoader.getSystemResourceAsStream("booknaviger/resources/graphics/htmlreport/header.png"));
        Image background = ImageIO.read(ClassLoader.getSystemResourceAsStream("booknaviger/resources/graphics/htmlreport/background.png"));
        Image tableTray = ImageIO.read(ClassLoader.getSystemResourceAsStream("booknaviger/resources/graphics/htmlreport/table_tray.png"));
        ImageIO.write((RenderedImage) header, "png", new FileOutputStream(imagefolder + "header.png"));
        ImageIO.write((RenderedImage) background, "png", new FileOutputStream(imagefolder + "background.png"));
        ImageIO.write((RenderedImage) tableTray, "png", new FileOutputStream(imagefolder + "table_tray.png"));
        if (advancedMode) {
            Image emptyThumbnail = ImageIO.read(ClassLoader.getSystemResourceAsStream("booknaviger/resources/graphics/htmlreport/emptyThumbnail.png"));
            ImageIO.write((RenderedImage) emptyThumbnail, "png", new FileOutputStream(imagefolder + "emptyThumbnail.png"));
        }
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "copyImages");
    }

    /**
     * List the series from the base directory
     * @return The series names
     */
    private String[] listSeries() {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "listSeries");
        Object[][] seriesData = new BooksFolderAnalyser(baseDir).listSeries();
        if (seriesData == null) {
            Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "listSeries", null);
            return new String[] {};
        }
        String[] seriesNames = new String[seriesData.length];
        for (int i = 0; i < seriesData.length; i++) {
            seriesNames[i] = (String) seriesData[i][0];
        }
        nbrOfSeries = seriesNames.length;
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "listSeries", seriesNames);
        return seriesNames;
    }

    /**
     * List the albums for a specific serie
     * @param serieName the serie name to list the albums
     * @return the albums names
     */
    private String[] listAlbums(String serieName) {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "listAlbums", serieName);
        File albumFile = new File(baseDir.getPath() + File.separatorChar + serieName);
        String[][] albumsData = new BooksFolderAnalyser(albumFile).listAlbums();
        if (albumsData == null) {
            Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "listAlbums", null);
            return new String[] {};
        }
        String[] albumsNames = new String[albumsData.length];
        for (int i = 0; i < albumsData.length; i++) {
            albumsNames[i] = albumsData[i][0] + albumsData[i][1];
        }
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "listAlbums", albumsNames);
        return albumsNames;
    }

    /**
     * Delete the specified folder
     * @param path The folder to delete
     */
    private void deleteHTMLReportDirectory(File path) {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "deleteHTMLReportDirectory", path);
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
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "deleteHTMLReportDirectory");
    }

    /**
     * Find the number of albums (total of all series)
     */
    private void findNbrOfAlbums() {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "findNbrOfAlbums");
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
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "findNbrOfAlbums");
    }

    /**
     * Set when the user want to cancel the reporting
     * @param cancelAsked Should be true to cancel the reporting
     */
    public void setCancelAsked(boolean cancelAsked) {
        Logger.getLogger(ReportGenerator.class.getName()).entering(ReportGenerator.class.getName(), "setCancelAsked", cancelAsked);
        this.cancelAsked = cancelAsked;
        Logger.getLogger(ReportGenerator.class.getName()).exiting(ReportGenerator.class.getName(), "setCancelAsked");
    }

}
