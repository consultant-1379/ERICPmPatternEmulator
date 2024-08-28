/*------------------------------------------------------------------------------
 ******************************************************************************
 * (c) Ericsson Inc. 2018 - All rights reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.configmaster.services;

import static com.ericsson.configmaster.constants.Constants.APP_PATH;
import static com.ericsson.configmaster.constants.Constants.DEBUG;
import static com.ericsson.configmaster.constants.Constants.LOG_FILE_PATH;
import static com.ericsson.configmaster.constants.Constants.UNDERSCORE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

/**
 * The Class AppLogger.
 */
@Service
public class AppLogger {

    /** The logger. */
    private final Logger LOGGER = Logger.getLogger("com.ericsson");

    /**
     * Intialize logger.
     */
    @PostConstruct
    public void intializeLogger(){
        try{
            createLogBackup();
            LOGGER.setUseParentHandlers(false);
            LOGGER.setLevel(DEBUG);

            // Creating fileHandler
            final FileHandler fileHandler = new FileHandler(LOG_FILE_PATH);
            fileHandler.setLevel(DEBUG);

            // Creating and assigning SimpleFormatter
            Formatter simpleFormatter = new SimpleFormatter() {
                private static final String format = "[%1$tb %1$td,%1$tY %1$tT] %2$-7s: %3$s %n";

                @Override
                public synchronized String format(LogRecord lr){
                    return String.format(format,new Date(lr.getMillis()),lr.getLevel().getLocalizedName(),lr.getMessage());
                }
            };
            fileHandler.setFormatter(simpleFormatter);
            LOGGER.addHandler(fileHandler);

        } catch(Exception e){
            System.out.println("Exception occurred while initializing logger. " + e.getMessage());
        }
    }

    /**
     * Gets the logger.
     *
     * @return the logger
     */
    public Logger getLOGGER(){
        return LOGGER;
    }

    /**
     * This method create a log backup for all the files present in logs location.
     *
     * @return true, if successful
     */
    public static boolean createLogBackup(){
        try{
            String logLocation = APP_PATH + File.separator + "logs";
            File logLocationObj = new File(logLocation);
            if(!logLocationObj.exists() || logLocationObj.list().length == 0){
                return false;
            }

            Date today = new Date();
            String backupDirName = "PE" + UNDERSCORE + convertDateToDateTimeString(today);
            System.out.println("BackUpDirName : " + backupDirName);

            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(logLocation + File.separator + backupDirName.concat(".zip")));
            addDir(logLocationObj,out,backupDirName);
            File[] files = logLocationObj.listFiles(getFileNameFilter());
            for(File file : files){
                if(file.isDirectory()){
                    file.delete();
                }
            }
            out.close();
        } catch(Exception ex){
            System.out.println("WARN : Unable to create logs backup directory.");
            return false;
        }
        return true;
    }

    /**
     * Convert the date object in the string format : YYYYMMDDHHMM.
     *
     * @param dateObj
     *            the date obj
     * @return the string
     */
    public static String convertDateToDateTimeString(Date dateObj){
        String dateString = "";
        if(dateObj != null){
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateObj);
            StringBuffer tempBuffer = new StringBuffer();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int date = cal.get(Calendar.DATE);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            tempBuffer.append(year);
            if(month > 8){
                tempBuffer.append(month + 1);
            } else{
                tempBuffer.append(0).append(month + 1);
            }
            if(date < 10){
                tempBuffer.append(0).append(date);
            } else{
                tempBuffer.append(date);
            }
            if(hour < 10){
                tempBuffer.append(0).append(hour);
            } else{
                tempBuffer.append(hour);
            }
            if(minute < 10){
                tempBuffer.append(0).append(minute);
            } else{
                tempBuffer.append(minute);
            }

            dateString = tempBuffer.toString();
        }
        return dateString;
    }

    /**
     * This method used to handle files present inside nested directory.
     *
     * @param dirObj
     *            the dir obj
     * @param out
     *            the out
     * @param backupDirName
     *            the backup dir name
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void addDir(File dirObj, ZipOutputStream out, String backupDirName) throws IOException{
        boolean backupFlag = false;
        File[] files = dirObj.listFiles(getFileNameFilter());
        byte[] tmpBuf = new byte[1024];
        for(int i = 0; i < files.length; i++){
            if(files[i].isDirectory()){
                backupFlag = true;
                addDir(files[i],out,backupDirName);
                continue;
            }
            FileInputStream in = null;
            ;
            if(backupFlag){
                in = new FileInputStream(files[i].getPath());
                out.putNextEntry(new ZipEntry(backupDirName + File.separator + files[i].getPath().substring(files[i].getPath().indexOf("logs") + 5)));
            } else{
                in = new FileInputStream(files[i]);
                out.putNextEntry(new ZipEntry(backupDirName + File.separator + files[i].getName()));
            }
            int len;
            while ((len = in.read(tmpBuf)) > 0){
                out.write(tmpBuf,0,len);
            }
            out.closeEntry();
            in.close();
            files[i].delete();
        }
    }

    /**
     * Gets the file name filter.
     *
     * @return the file name filter
     */
    private static FilenameFilter getFileNameFilter(){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name){
                return !name.startsWith("PE" + UNDERSCORE);
            }
        };
    }

}
