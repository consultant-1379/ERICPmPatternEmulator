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

package com.ericsson.topologyparser.autogen;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * A factory for creating Object objects.
 */
@XmlRegistry
public class ObjectFactory {

    /**
     * Creates a new Object object.
     *
     * @return the attr
     */
    public Attr createAttr(){
        return new Attr();
    }

    /**
     * Creates a new Object object.
     *
     * @return the item
     */
    public Item createItem(){
        return new Item();
    }

    /**
     * Creates a new Object object.
     *
     * @return the model
     */
    public Model createModel(){
        return new Model();
    }

    /**
     * Creates a new Object object.
     *
     * @return the struct
     */
    public Struct createStruct(){
        return new Struct();
    }

    /**
     * Creates a new Object object.
     *
     * @return the seq
     */
    public Seq createSeq(){
        return new Seq();
    }

    /**
     * Creates a new Object object.
     *
     * @return the mo
     */
    public Mo createMo(){
        return new Mo();
    }
}
