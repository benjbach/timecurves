/*****************************************************************************
 * Copyright (C) 2003-2005 Jean-Daniel Fekete and INRIA, France              *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the X11 Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-infovis.txt file.                                                 *
 *****************************************************************************/

package fr.aviz.progresio.server.diff;

import cern.colt.function.IntComparator;

/**
 * Sorted map of integers.
 * 
 * @author Jean-Daniel Fekete
 * @version $Revision: 1.10 $
 */
public class IntIntSortedMap extends RBTree {
    /**
     * Copy constructor.
     * @param other the map to copy
     */
    public IntIntSortedMap(IntIntSortedMap other) {
        super(other);
    }

    /**
     * Constructor.
     */
    public IntIntSortedMap() {
    }

    /**
     * Constructor.
     * @param comparator the comparator
     */
    public IntIntSortedMap(IntComparator comparator) {
        super(comparator);
    }
}

