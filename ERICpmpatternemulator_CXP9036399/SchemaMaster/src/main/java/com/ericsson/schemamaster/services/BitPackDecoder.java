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

import java.util.BitSet;

import org.springframework.stereotype.Service;

/**
 * The Class BitPackDecoder.
 */
@Service
public class BitPackDecoder {

    /**
     * Decode bits.
     *
     * @param startBit
     *            the start bit
     * @param lengthBits
     *            the length bits
     * @param data
     *            the data
     * @return the string
     */
    public String decodeBits(int startBit, int lengthBits, byte[] data){
        BitSet bitSetToDecode = BitSet.valueOf(reverse(data));
        int startIndex = startBit - 1; // Start index will be 1 for first bit
        int endIndex = startIndex + lengthBits;
        return String.valueOf(convertBitsToValue(bitSetToDecode.get(startIndex,endIndex),lengthBits));
    }

    /**
     * Convert bits to value.
     *
     * @param bits
     *            the bits
     * @param length
     *            the length
     * @return the int
     */
    private int convertBitsToValue(BitSet bits, int length){
        int value = 0;
        // Set the increment to the difference between the therocial length and the effective lenght of the bitset
        // to take into account the fact that the BitSet just represent significative bits
        // (i.e instead of 110, the bitset while contains 11 since the 0 is irrelevant in his representation)
        int increment = length - bits.length();
        // Browse the BitSet from the end to the begining to handle the little endian representation
        for(int i = bits.length() - 1; i >= 0; --i){
            value += bits.get(i) ? 1L << increment : 0L;
            increment++;
        }
        return value;
    }

    /**
     * Reverse bit order of each byte of the array.
     *
     * @param data
     *            the bytes array
     * @return the bytes array with bit order reversed for each byte
     */
    private byte[] reverse(byte[] data){
        byte[] bytes = data.clone();
        for(int i = 0; i < bytes.length; i++){
            bytes[i] = (byte) (Integer.reverse(bytes[i]) >>> 24);
        }
        return bytes;
    }

}
