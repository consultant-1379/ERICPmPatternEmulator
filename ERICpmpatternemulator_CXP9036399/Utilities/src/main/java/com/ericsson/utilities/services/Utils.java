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

package com.ericsson.utilities.services;

import static com.ericsson.configmaster.constants.Constants.BIN_EXTENSION;
import static com.ericsson.configmaster.constants.Constants.CELL_META_DATA_FILE_NAME;
import static com.ericsson.configmaster.constants.Constants.COLON;
import static com.ericsson.configmaster.constants.Constants.DOT;
import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.GZ_EXTENSION;
import static com.ericsson.configmaster.constants.Constants.HOUR;
import static com.ericsson.configmaster.constants.Constants.MINUTES;
import static com.ericsson.configmaster.constants.Constants.PATTERN_INFO_CSV_FILE_NAME;
import static com.ericsson.configmaster.constants.Constants.PATTERN_META_DATA_FILE_NAME;
import static com.ericsson.configmaster.constants.Constants.SECONDS;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.ericsson.configmaster.services.AppConfigLoader;
import com.ericsson.configmaster.services.AppLogger;
import com.ericsson.utilities.entities.PatternToCellMetaDataWrapper;
import com.ericsson.utilities.entities.SessionPerCallWrapper;

/**
 * The Class Utils.
 */
public class Utils {

    /** The app config loader. */
    private static AppConfigLoader appConfigLoader;

    /** The logger. */
    private static Logger LOGGER;

    /** The app config loader. */
    @Autowired
    private AppConfigLoader appConfigLoaderObj;

    /** The app logger. */
    @Autowired
    private AppLogger appLogger;

    /**
     * Inits the Utils class.
     */
    @PostConstruct
    public void init(){
        appConfigLoader = this.appConfigLoaderObj;
        LOGGER = this.appLogger.getLOGGER();
    }

    /**
     * Read binary file.
     *
     * @param binaryFile
     *            the binary file
     * @return the input stream
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static InputStream readBinaryFile(File binaryFile) throws IOException{
        FileInputStream fis = new FileInputStream(binaryFile);
        if(binaryFile.getName().contains(GZ_EXTENSION)){
            return new BufferedInputStream(new GZIPInputStream(fis));
        } else if(binaryFile.getName().contains(BIN_EXTENSION)){
            return new BufferedInputStream(new FileInputStream(binaryFile));
        } else{
            logMessage(ERROR,"Invalid File Format. This tool supports only GZ and BIN format.");
            return null;
        }
    }

    /**
     * Gets the buffer size for binary files.
     *
     * @param binaryFile
     *            the binary file
     * @return the buffer size for binary files
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static int getBufferSizeForBinaryFiles(File binaryFile) throws IOException{
        int size = 0;
        if(binaryFile.getName().contains(GZ_EXTENSION)){
            RandomAccessFile raf = new RandomAccessFile(binaryFile, "r");
            raf.seek(raf.length() - 4);
            int b4 = raf.read();
            int b3 = raf.read();
            int b2 = raf.read();
            int b1 = raf.read();
            size = b1 << 24 | (b2 << 16) + (b3 << 8) + b4;
            raf.close();
        } else{
            size = (int) binaryFile.length();
        }
        return size;
    }

    /**
     * Checks if is numeric.
     *
     * @param s
     *            the s
     * @return true, if is numeric
     */
    public static boolean isNumeric(String s){
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");
    }

    /**
     * Log message.
     *
     * @param level
     *            the level
     * @param message
     *            the message
     */
    public static void logMessage(Level level, String message){
        LOGGER.log(level,message);
    }

    /**
     * Log message.
     *
     * @param level
     *            the level
     * @param message
     *            the message
     * @param consoleDisplayFlag
     *            the console display flag
     */
    public static void logMessage(Level level, String message, boolean consoleDisplayFlag){
        System.out.println(message);
        logMessage(level,message);
    }

    /**
     * Gets the attribute from file name.
     *
     * @param filePath
     *            the file path
     * @param fileName
     *            the file name
     * @param dataSource
     *            the data source
     * @param attribute
     *            the attribute
     * @return the attribute from file name
     */
    public static String getAttributeFromFileName(String filePath, String fileName, String dataSource, String attribute){
        String patternString = appConfigLoader.getAppConfigDatasourceAutogenMap().get(dataSource).getFileNamePattern().trim();
        if(null == patternString || patternString.isEmpty() || !patternString.contains(attribute)){
            return "";
        }
        try{
            Pattern patttern = Pattern.compile(patternString);
            Matcher match = patttern.matcher(fileName);
            if(match.matches()){
                return match.group(attribute);
            }
        } catch(IllegalArgumentException e){
            logMessage(ERROR,"Error in fetching " + attribute + " from filename." + e.getMessage());
            throw e;
        }
        return "";
    }

    /**
     * Gets the attribute from file name.
     *
     * @param filePath
     *            the file path
     * @param fileName
     *            the file name
     * @param dataSource
     *            the data source
     * @param attributeList
     *            the attribute list
     * @return the attribute from file name
     */
    public static Map<String, String> getAttributeFromFileName(String filePath, String fileName, String dataSource, List<String> attributeList){
        String patternString = appConfigLoader.getAppConfigDatasourceAutogenMap().get(dataSource).getFileNamePattern().trim();
        if(null == patternString || patternString.isEmpty()){
            return null;
        }
        Map<String, String> attributeValueMap = new HashMap<String, String>(attributeList.size());
        try{
            Pattern patttern = Pattern.compile(patternString);
            Matcher match = patttern.matcher(fileName);
            if(match.matches()){
                for(String attribute : attributeList){
                    try{
                        attributeValueMap.put(attribute,match.group(attribute));
                    } catch(IllegalArgumentException e){
                        attributeValueMap.put(attribute,"");
                    }
                }
            }
        } catch(IllegalArgumentException e){
            logMessage(ERROR,"Error in fetching " + attributeList + " from filename." + e.getMessage());
            throw e;
        }
        return attributeValueMap;
    }

    /**
     * Gets the file name filter.
     *
     * @return the file name filter
     */
    public static FilenameFilter getInputFileNameFilter(){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name){
                if(name.contains(BIN_EXTENSION) || name.contains(GZ_EXTENSION)){
                    return true;
                }
                return false;
            }
        };
    }

    /**
     * Gets the pattern location file name filter.
     *
     * @param patternIDFilteredList
     *            the pattern ID filtered list
     * @param outputLocation
     *            the output location
     * @return the pattern location file name filter
     */
    public static FilenameFilter getPatternLocationFileNameFilter(final List<String> patternIDFilteredList, final String outputLocation){
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name){
                if(!name.contains(PATTERN_INFO_CSV_FILE_NAME) && !name.contains(PATTERN_META_DATA_FILE_NAME) && !name.contains(
                        CELL_META_DATA_FILE_NAME) && patternIDFilteredList.contains(name)){
                    return true;
                } else if(!name.contains(PATTERN_INFO_CSV_FILE_NAME)){
                    File file = new File(dir.getPath() + File.separator + name);
                    try{
                        if(file.isDirectory()){
                            FileUtils.copyDirectory(file,new File(outputLocation + File.separator + name));
                        } else{
                            FileUtils.copyFile(file,new File(outputLocation + File.separator + name));
                        }
                    } catch(IOException e){
                        logMessage(ERROR,"Error in while copy pattern." + e.getMessage());
                    }
                    return false;
                }
                return false;
            }
        };
    }

    /**
     * Num to binary.
     *
     * @param num
     *            the num
     * @param length
     *            the length
     * @return the string
     */
    public static String numToBinary(int num, int length){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < length; i++){
            sb.append((num & 1) == 1 ? '1' : '0');
            num >>= 1;
        }
        return sb.reverse().toString();
    }

    /**
     * Byte array to binary string.
     *
     * @param b
     *            the b
     * @return the string
     */
    public static String byteArrayToBinaryString(byte[] b){
        final char[] bits = new char[8 * b.length];
        for(int i = 0; i < b.length; i++){
            final byte byteval = b[i];
            int bytei = i << 3;
            int mask = 0x1;
            for(int j = 7; j >= 0; j--){
                final int bitval = byteval & mask;
                if(bitval == 0){
                    bits[bytei + j] = '0';
                } else{
                    bits[bytei + j] = '1';
                }
                mask <<= 1;
            }
        }
        return String.valueOf(bits);
    }

    /**
     * Gets the node name.
     *
     * @param fdn
     *            the fdn
     * @return the node name
     */
    public static String getNodeName(String fdn){
        String nodeName;
        if(fdn.contains("MeContext=")){
            nodeName = fdn.split("MeContext=")[1];
            if(nodeName.contains("netsim")){
                nodeName = nodeName.split("_")[1];
            }
        } else if(fdn.contains("ManagedElement=")){
            nodeName = fdn.split("ManagedElement=")[1];
        } else{
            nodeName = fdn;
        }
        return nodeName;
    }

    /**
     * Gets the time stamp byte array.
     *
     * @return the time stamp byte array
     */
    public static byte[] getTimeStampByteArray(){
        ByteBuffer timeBuf = ByteBuffer.allocate(9);
        // get the current timestamp in UTC
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(System.currentTimeMillis());

        timeBuf.putShort((short) cal.get(Calendar.YEAR)); // 2 bytes
        timeBuf.put((byte) (cal.get(Calendar.MONTH) + 1)); // 1 byte = calender month counts from zero
        timeBuf.put((byte) cal.get(Calendar.DAY_OF_MONTH)); // 1 byte
        timeBuf.put((byte) cal.get(Calendar.HOUR_OF_DAY)); // 1 byte
        timeBuf.put((byte) cal.get(Calendar.MINUTE)); // 1 byte
        timeBuf.put((byte) cal.get(Calendar.SECOND)); // 1 byte
        timeBuf.putShort((short) cal.get(Calendar.MILLISECOND)); // 2 bytes

        return timeBuf.array();
    }

    /**
     * Checks if is address I pv 4.
     *
     * @param ipAddress
     *            the ip address
     * @return true, if is address I pv 4
     */
    public static boolean isAddressIPv4(String ipAddress){
        boolean result = false;
        if(ipAddress.contains(DOT) && Character.isDigit(ipAddress.charAt(0))){
            result = true;
        }
        return result;
    }

    /**
     * Checks if is address I pv 6.
     *
     * @param ipAddress
     *            the ip address
     * @return true, if is address I pv 6
     */
    public static boolean isAddressIPv6(String ipAddress){
        boolean result = false;
        if(ipAddress.contains(COLON)){
            result = true;
        }
        return result;

    }

    /**
     * Gets the date obj.
     *
     * @param date
     *            the date
     * @param time
     *            the time
     * @param offset
     *            the offset
     * @return the date obj
     */
    public static Date getDateObj(String date, String time, String offset){
        String dateString = date + " " + time + offset;
        DateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmssZ");
        Date selectedDate = null;
        try{
            selectedDate = formatter.parse(dateString);
        } catch(ParseException e){
            Utils.logMessage(ERROR,"Could not parse rop file date " + dateString + " " + e);
        }
        return selectedDate;
    }

    /**
     * Calc date diff.
     *
     * @param newDate
     *            the new date
     * @param pastDate
     *            the past date
     * @param resultType
     *            the result type
     * @return the int
     */
    public static int calcDateDiff(Date newDate, Date pastDate, String resultType){
        long diff = newDate.getTime() - pastDate.getTime();
        int diffValue = 0;
        if(diff > 0){
            switch(resultType){
            case HOUR:
                diffValue = (int) (diff / (60 * 60 * 1000));
                break;
            case MINUTES:
                diffValue = (int) (diff / (60 * 1000));
                break;
            case SECONDS:
                diffValue = (int) (diff / 1000);
                break;
            }
        }
        return diffValue;
    }

    /**
     * Deserialize pattern meta data file.
     *
     * @param patternLocation
     *            the pattern location
     * @return the pattern to cell meta data wrapper
     * @throws Exception
     *             the exception
     */
    public static PatternToCellMetaDataWrapper deserializePatternMetaDataFile(String patternLocation) throws Exception{
        try{
            File patternMetaDataFile = new File(patternLocation + File.separator + PATTERN_META_DATA_FILE_NAME);
            FileInputStream fis = new FileInputStream(patternMetaDataFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            PatternToCellMetaDataWrapper patternToCellMetaDataWrapper = (PatternToCellMetaDataWrapper) ois.readObject();
            ois.close();
            fis.close();
            return patternToCellMetaDataWrapper;
        } catch(Exception e){
            Utils.logMessage(ERROR,"Unable to read pattern meta data file " + e);
            throw e;
        }
    }

    /**
     * Deserialize session file.
     *
     * @param sessionFile
     *            the session file
     * @return the session per call wrapper
     * @throws Exception
     *             the exception
     */
    public static SessionPerCallWrapper deserializeSessionFile(File sessionFile) throws Exception{
        try{
            GZIPInputStream fis = new GZIPInputStream(new FileInputStream(sessionFile));
            ObjectInputStream ois = new ObjectInputStream(fis);
            SessionPerCallWrapper sessionPerCallObj = (SessionPerCallWrapper) ois.readObject();
            ois.close();
            fis.close();
            return sessionPerCallObj;
        } catch(Exception e){
            Utils.logMessage(ERROR,"Unable to read pattern meta data file " + e);
            throw e;
        }
    }

}