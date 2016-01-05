/**
 * The MIT License (MIT)
 * Wrath Utils Copyright (c) 2015 Trent Spears
 */
package wrath.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Flexible and expandable logging system. Includes Time stamps, console verbose and file logging.
 * @author Trent Spears
 */
public class Logger extends PrintStream
{
    public static final PrintStream SYS_ERR = System.err;
    public static final PrintStream SYS_OUT = System.out;
    private static final DateFormat format = new SimpleDateFormat("[MM/dd/yyyy][HH:mm:ss]");
    private static final Calendar now = Calendar.getInstance();
    
    private boolean closed = false;
    private final boolean console;
    private final boolean file;
    private final File logFile;
    private PrintWriter fout;
    private final boolean time;
    
    /**
     * Constructor.
     * @param logFile The {@link java.io.File} to save the log to.
     */
    public Logger(File logFile)
    {
        this(logFile, true, true, true);
    }
    
    /**
     * Constructor.
     * @param logFile The {@link java.io.File} to save the log to.
     * @param timeStamp If true, a time stamp and the name of the logger will be displayed before the output is printed.
     * @param writeToConsole If true, outputs to the console.
     * @param writeToFile If true, writes to the file.
     */
    public Logger(File logFile, boolean timeStamp, boolean writeToConsole, boolean writeToFile)
    {
        super(SYS_OUT, true);
        
        this.logFile = logFile;
        this.console = writeToConsole;
        this.file = writeToFile;
        this.time = timeStamp;
        
        if(file)
        {
            if(!logFile.exists())
                try
                {
                    logFile.createNewFile();
                }
                catch(IOException e)
                {
                    System.err.println("Could not create new file for logger '" + logFile.getName() + "'! I/O Error!");
                }
            
            try
            {
                fout = new PrintWriter(new FileWriter(logFile, true), true);
            }
            catch(IOException ex)
            {
                System.err.println("Could not log to file for logger '" + logFile.getName() + "'! I/O Error!");
            }
        }
    }
    
    /**
     * Method meant to be overridden. Method takes in original String and returns modified String to be printed.
     * If method returns null, nothing will be printed to the console.
     * @param message The original message to be decided whether to print or not.
     * @return If a String is returned, it is what will be printed to the console. If returns null, nothing is printed.
     */
    public String filterConsole(String message)
    {
        return message;
    }
    
    /**
     * Method meant to be overridden. Method takes in original String and returns modified String to be printed.
     * If method returns null, nothing will be printed to the log file.
     * @param message The original message to be decided whether to print or not.
     * @return If a String is returned, it is what will be printed to the log file. If returns null, nothing is printed.
     */
    public String filterLog(String message)
    {
        return message;
    }
    
    /**
     * If true, the logger is saved and closed, and cannot be written to anymore.
     * @return Returns true if the logger is closed.
     */
    public boolean isClosed()
    {
        return closed;
    }
    
    @Override
    public void close()
    {
        if(closed) return;
        closed = true;
        if(fout != null) fout.close();
    }
    
    @Override
    public void print(String string)
    {
        if(console)
        {
            String prnt = filterConsole(string);
            if(prnt != null)
            {
                if(time) super.print(format.format(now.getTime()) + " " + prnt);
                else super.print(prnt);
            }
        }
        
        if(file && !closed)
        {
            String fin = filterLog(string);
            if(fin != null)
            {
                if(fin.endsWith("\n")) fout.println(format.format(now.getTime()) + " " + string);
                else fout.print(format.format(now.getTime()) + " " + string);
                fout.flush();
            }
        }
    }
    
    @Override
    public void println(String string)
    {
        print(string + '\n');
    }
    
    /**
     * Changes the System's standard output, to replace System.out.
     * @param logger The {@link wrath.util.Logger} to make the system's standard output. If null, resets the System.out logger back to default.
     */
    public static void registerOutputLogger(Logger logger)
    {
        if(logger == null) System.setOut(SYS_OUT);
        System.setOut(logger);
    }
    
    /**
     * Changes the System's standard error output, to replace System.err.
     * @param logger The {@link wrath.util.Logger} to make the system's standard error output. If null, resets the System.err logger back to default.
     */
    public static void registerErrorLogger(Logger logger)
    {
        if(logger == null) System.setErr(SYS_ERR);
        System.setErr(logger);
    }
}

