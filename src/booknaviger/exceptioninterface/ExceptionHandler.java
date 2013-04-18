/*
 */

package booknaviger.exceptioninterface;

import booknaviger.osbasics.OSBasics;
import booknaviger.properties.PropertiesManager;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Handler for the exception which get logged
 * @author Inervo
 */
public class ExceptionHandler extends Handler {
    
    private static LogInterface logInterface = LogInterface.getInstance();
    
    /**
     * Reinitialize the link for {@link ExceptionHandler#logInterface} 
     */
    public static void reinitializeLogInterfaceLink() {
        logInterface = LogInterface.getInstance();
    }
    
    /**
     * Constructor. Set the formater and the log level from the properties
     */
    private ExceptionHandler() {
        Logger.getLogger(ExceptionHandler.class.getName()).entering(ExceptionHandler.class.getName(), "ExceptionHandler");
        setFormatter(new ExceptionHandlerFormatter());
        String logLevel = PropertiesManager.getInstance().getKey("logLevel");
        if (logLevel != null) {
            setLevel(Level.parse(logLevel));
        } else {
            setLevel(Level.OFF);
        }
        Logger.getLogger(ExceptionHandler.class.getName()).exiting(ExceptionHandler.class.getName(), "ExceptionHandler");
    }
    
    /**
     * Class to format the exception message for the {@link LogInterface}
     */
    class ExceptionHandlerFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            Date date = new Date();
            date.setTime(record.getMillis());
            String message = formatMessage(record);
            String level = record.getLevel().getLocalizedName();
            String className = record.getSourceClassName();
            String methodName = record.getSourceMethodName();
            return (new SimpleDateFormat("HH:mm:ss:SSS").format(date) + "  [" + level + "] - " + className + "§" + methodName + " : " + message);
        }
        
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            logInterface.publishNewLog(getFormatter().format(record));
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
    
    /**
     * register the handlers (log file and interface) for the logged messages
     */
    public static void registerExceptionHandler() {
        Logger.getLogger(ExceptionHandler.class.getName()).entering(ExceptionHandler.class.getName(), "registerExceptionHandler");
        Enumeration<String> loggers = LogManager.getLogManager().getLoggerNames();
        while (loggers.hasMoreElements()) {
            Logger.getLogger(loggers.nextElement()).setLevel(Level.SEVERE);
        }
        Handler[] handlers = Logger.getLogger("").getHandlers();
        String logLevel = PropertiesManager.getInstance().getKey("logLevel");
        if (logLevel != null && Level.parse(logLevel).intValue() < Level.INFO.intValue()) {
            Logger.getLogger("").setLevel(Level.parse(logLevel));
        } else {
            Logger.getLogger("").setLevel(Level.INFO); // Min level to accept log message
        }
        boolean registerHandlers = true;
        for (Handler handler : handlers) {
                if (handler instanceof ConsoleHandler) {
                    if (logLevel != null) {
                        handler.setLevel(Level.parse(logLevel));
                        Logger.getLogger(ExceptionHandler.class.getName()).log(Level.INFO, "Log level set to {0}", logLevel);
                    } else {
                        handler.setLevel(Level.SEVERE);
                    }
                }
                if (handler instanceof ExceptionHandler) {
                    registerHandlers = false;
                }
            }
        if (registerHandlers) {
            try {
                Logger.getLogger("").addHandler(new FileHandler(new File(OSBasics.getAppDataDir(), "log.txt").toString()));
            } catch (IOException | SecurityException ex) {
                Logger.getLogger(ExceptionHandler.class.getName()).log(Level.SEVERE, "Can't log messages to the log file", ex);
            }
            Logger.getLogger("").addHandler(new ExceptionHandler());
        }
        Logger.getLogger(ExceptionHandler.class.getName()).exiting(ExceptionHandler.class.getName(), "registerExceptionHandler");
    }

}
