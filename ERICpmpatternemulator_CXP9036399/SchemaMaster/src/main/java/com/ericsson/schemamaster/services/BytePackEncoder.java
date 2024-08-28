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

package com.ericsson.schemamaster.services;

import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.FILE_HEADER_RECORD_TYPE;
import static com.ericsson.configmaster.constants.Constants.TCP_STREAM_INDICATOR_RECORD_TYPE;

import java.nio.ByteBuffer;

import org.springframework.stereotype.Service;

import com.ericsson.schemamaster.entities.SchemaRelease;
import com.ericsson.utilities.services.Utils;

/**
 * The Class BytePackEncoder.
 */
@Service
public class BytePackEncoder {

    /**
     * Convert long value to byte array.
     *
     * @param value
     *            the value
     * @param length
     *            the length
     * @return the byte[]
     */
    public byte[] convertLongValueToByteArray(long value, int bitsLength){
        if(bitsLength % 8 != 0){
            Utils.logMessage(ERROR,"BytePackEncoder : Long value cannot be converted into byte array due to invalid length");
            return null;
        }
        byte binaryData[] = new byte[bitsLength / 8];
        int index = 0;
        for(int counter = bitsLength; counter >= 8; counter -= 8){
            binaryData[index] = (byte) (value >> counter - 8);
            index++;
        }
        return binaryData;
    }

    /**
     * Handle plmn ID.
     *
     * @param mcc
     *            the mcc
     * @param mnc
     *            the mnc
     * @return the string
     */
    public String handlePlmnID(String mcc, String mnc){
        if(mnc.length() == 2){
            mnc = "F" + mnc;
        } else if(mnc.length() == 1){
            mnc = "F" + "F" + mnc;
        }
        if(mcc.length() == 2){
            mcc = "F" + mcc;
        } else if(mnc.length() == 1){
            mcc = "F" + "F" + mcc;
        }
        long val = Long.parseLong(String.valueOf(mcc.charAt(1)) + mcc.charAt(0) + mnc.charAt(0) + mcc.charAt(2) + mnc.charAt(2) + mnc.charAt(1),16);
        return Utils.byteArrayToBinaryString(convertLongValueToByteArray(val,24));
    }

    /**
     * Gets the TCP header byte buffer.
     *
     * @param fdn
     *            the fdn
     * @param schemaReleaseObj
     *            the schema release obj
     * @return the TCP header byte buffer
     */
    public byte[] getTCPHeaderByteBuffer(String fdn, SchemaRelease schemaReleaseObj){
        int headerByteSize = 412;
        if(schemaReleaseObj.getFfv().equals("T")){
            headerByteSize = 425;
        }
        ByteBuffer headerByteBuffer = ByteBuffer.allocate(425);
        headerByteBuffer.clear();
        headerByteBuffer.putShort((short) headerByteSize); // RECORD_LENGTH (2)
        headerByteBuffer.putShort((short) TCP_STREAM_INDICATOR_RECORD_TYPE); // RECORD_TYPE (2)

        headerByteBuffer.put(String.format("%-5s",schemaReleaseObj.getFfv()).getBytes(),0,5); // FILE_FORMAT_VERSION (5)
        if(schemaReleaseObj.getFfv().equals("T")){
            headerByteBuffer.put(String.format("%-13s",schemaReleaseObj.getDocNumber()).getBytes(),0,13); // PM_RECORDING_PACKAGE_PRODUCT_VERSION (13)
        }
        headerByteBuffer.put(String.format("%-5s",schemaReleaseObj.getRevision()).getBytes(),0,5); // PM_RECORDING_PACKAGE_PRODUCT_REVISION (5)

        byte[] filler = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        headerByteBuffer.put(filler,0,7);
        // headerByteBuffer.put(Utils.getTimeStampByteArray(),0,7); // YYMDhms (7)

        String nodeName = Utils.getNodeName(fdn);
        headerByteBuffer.put(String.format("%-128s",nodeName).getBytes(),0,128); // NE_USER_LABEL (128)
        headerByteBuffer.put(String.format("%-255s",nodeName).getBytes(),0,255); // NE_LOGICAL_NAME (255)

        headerByteBuffer.putShort((short) 10004); // SCANNER (2)
        headerByteBuffer.putShort((short) 0); // CAUSE_OF_HEADER (2)
        headerByteBuffer.putShort((short) 0); // NUMBER_OF_DROPPED_EVENTS (2)

        return headerByteBuffer.array();
    }

    /**
     * Gets the file header byte buffer.
     *
     * @param fdn
     *            the fdn
     * @param schemaReleaseObj
     *            the schema release obj
     * @return the file header byte buffer
     */
    public byte[] getFileHeaderByteBuffer(String fdn, SchemaRelease schemaReleaseObj){
        int headerByteSize = 404;
        if(schemaReleaseObj.getFfv().equals("T")){
            headerByteSize = 417;
        }
        ByteBuffer headerByteBuffer = ByteBuffer.allocate(headerByteSize);
        headerByteBuffer.clear();
        headerByteBuffer.putShort((short) headerByteSize); // RECORD_LENGTH (2)
        headerByteBuffer.putShort((short) FILE_HEADER_RECORD_TYPE); // RECORD_TYPE (2)
        headerByteBuffer.put(String.format("%-5s",schemaReleaseObj.getFfv()).getBytes(),0,5); // FILE_FORMAT_VERSION (5)
        if(schemaReleaseObj.getFfv().equals("T")){
            headerByteBuffer.put(String.format("%-13s",schemaReleaseObj.getDocNumber()).getBytes(),0,13); // PM_RECORDING_PACKAGE_PRODUCT_VERSION (13)
        }
        headerByteBuffer.put(String.format("%-5s",schemaReleaseObj.getRevision()).getBytes(),0,5); // PM_RECORDING_PACKAGE_PRODUCT_REVISION (5)

        byte[] filler = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        headerByteBuffer.put(filler,0,7);
        // headerByteBuffer.put(Utils.getTimeStampByteArray(),0,7); // YYMDhms (7)

        String nodeName = Utils.getNodeName(fdn);
        headerByteBuffer.put(String.format("%-128s",nodeName).getBytes(),0,128); // NE_USER_LABEL (128)
        headerByteBuffer.put(String.format("%-255s",nodeName).getBytes(),0,255); // NE_LOGICAL_NAME (255)

        return headerByteBuffer.array();
    }
}
