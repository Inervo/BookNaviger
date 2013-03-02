/*
 */

package booknaviger.exceptioninterface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Inervo
 *
 */
public class ExceptionHandler extends Handler {
    
    LogInterface logInterface = LogInterface.getInstance();
    
    private ExceptionHandler() {
        setFormatter(new ExceptionHandlerFormatter());
    }
    
    class ExceptionHandlerFormatter extends Formatter {

        @Override
        public String format(LogRecord record) {
            Date date = new Date();
            date.setTime(record.getMillis());
            String message = formatMessage(record);
            String level = record.getLevel().getLocalizedName();
            return (new SimpleDateFormat("HH:mm:ss:SSS").format(date) + "  [" + level + "] : " + message);
        }
        
    }

    @Override
    public void publish(LogRecord record) {
        logInterface.publishNewLog(getFormatter().format(record));
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
    
    public static void registerExceptionHandler(String className) {
        Handler[] handlers = Logger.getLogger(className).getHandlers();
        boolean registerHandler = true;
        for (Handler handler : handlers) {
            if (handler instanceof ExceptionHandler) {
                registerHandler = false;
            }
        }
        if (registerHandler) {
            Logger.getLogger(className).addHandler(new ExceptionHandler());
        }
    }

}
