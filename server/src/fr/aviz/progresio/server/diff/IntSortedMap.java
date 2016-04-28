/*****************************************************************************
 * Copyright (C) 2003-2005 Jean-Daniel Fekete and INRIA, France              *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the X11 Software License    *
 * a copy of which has been included with this distribution in the           *
 * license-infovis.txt file.                                                 *
 *****************************************************************************/

package fr.aviz.progresio.server.diff;

import java.util.*;
import java.util.Iterator;
import java.util.NoSuchElementException;

import cern.colt.function.IntComparator;

/**
 * Association Map using ints as keys.
 *
 * @author Jean-Daniel Fekete
 * @version $Revision: 1.10 $
 */
public class IntSortedMap {
    private transient Entry root = null;
    /**
     * The number of entries in the tree
     */
    private transient int size = 0;
    protected IntComparator comparator;

    /**
     * The number of structural modifications to the tree.
     */
    private transient int modCount = 0;

    private void incrementSize()   { modCount++; size++; }
    private void decrementSize()   { modCount++; size--; }

    /**
     * Constructor for IntSortedMap.
     * 
     * @param other the map to copy
     * @param comp the comparator
     */
    public IntSortedMap(IntSortedMap other, IntComparator comp) {
        if (other != null) {
            root = new Entry(other.root, null);
            size = other.size();
        }
        this.comparator = comp;
    }
    
    /**
     * Constructor for IntSortedMap.
     */
    public IntSortedMap() {
        this(null, null);
    }
    
    /**
     * Constructor for IntSortedMap.
     * 
     * @param comparator the comparator
     */
    public IntSortedMap(IntComparator comparator) {
        this(null, comparator);
    }
    
    /**
     * Constructor for IntSortedMap.
     * 
     * @param other the map to copy
     */
    public IntSortedMap(IntSortedMap other) {
        this(other, null);
    }

    /**
     * Compares two keys.
     * @param key1 first key
     * @param key2 second key
     * @return the comparator order or key order if the 
     * comparator is null
     */
    public int compare(int key1, int key2) {
        if (comparator == null) {
            return key1 - key2;
        }
        else {
            return comparator.compare(key1, key2);
        }
    }
    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size() {
        return size;
    }
    
    /**
     * Returns true if the map is empty.
     * @return true if the map is empty
     */
    public boolean isEmpty() {
    	return root == null;
    }
    
    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.
     *
     * @param key key whose presence in this map is to be tested.
     * 
     * @return <tt>true</tt> if this map contains a mapping for the
     *            specified key.
     * @throws ClassCastException if the key cannot be compared with the keys
     *                  currently in the map.
     * @throws NullPointerException key is <tt>null</tt> and this map uses
     *                  natural ordering, or its comparator does not tolerate
     *            <tt>null</tt> keys.
     */
    public boolean containsKey(int key) {
        return getEntry(key) != null;
    }

    /**
     * Returns the value to which this map maps the specified key.  Returns
     * <tt>null</tt> if the map contains no mapping for this key.  A return
     * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to <tt>null</tt>.  The <tt>containsKey</tt>
     * operation may be used to distinguish these two cases.
     *
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *               <tt>null</tt> if the map contains no mapping for the key.
     * @throws    ClassCastException key cannot be compared with the keys
     *                  currently in the map.
     * @throws NullPointerException key is <tt>null</tt> and this map uses
     *                  natural ordering, or its comparator does not tolerate
     *                  <tt>null</tt> keys.
     * 
     * @see #containsKey(int)
     */
    public Object get(int key) {
        Entry p = getEntry(key);
        return (p==null ? null : p.value);
    }
    
    /**
     * Returns the first (lowest) key currently in this sorted map.
     *
     * @return the first (lowest) key currently in this sorted map.
     * @throws    NoSuchElementException Map is empty.
     */
    public int firstKey() {
        return key(firstEntry());
    }

    /**
     * Returns the last (highest) key currently in this sorted map.
     *
     * @return the last (highest) key currently in this sorted map.
     * @throws    NoSuchElementException Map is empty.
     */
    public int lastKey() {
        return key(lastEntry());
    }
	
    /**
     * Returns this map's entry for the given key, or <tt>null</tt> if the map
     * does not contain an entry for the key.
     *
     * @return this map's entry for the given key, or <tt>null</tt> if the map
     *                does not contain an entry for the key.
     * @throws ClassCastException if the key cannot be compared with the keys
     *                  currently in the map.
     * @throws NullPointerException key is <tt>null</tt> and this map uses
     *                  natural order, or its comparator does not tolerate *
     *                  <tt>null</tt> keys.
     */
    private Entry getEntry(int key) {
        Entry p = root;
        while (p != null) {
            int cmp = compare(key,p.key);
            if (cmp == 0)
                return p;
            else if (cmp < 0)
                p = p.left;
            else
                p = p.right;
        }
        return null;
    }

//    /**
//     * Gets the entry corresponding to the specified key; if no such entry
//     * exists, returns the entry for the least key greater than the specified
//     * key; if no such entry exists (i.e., the greatest key in the Tree is less
//     * than the specified key), returns <tt>null</tt>.
//     */
//    private Entry getCeilEntry(int key) {
//        Entry p = root;
//        if (p==null)
//            return null;
//
//        while (true) {
//            int cmp = compare(key, p.key);
//            if (cmp == 0) {
//                return p;
//            } else if (cmp < 0) {
//                if (p.left != null)
//                    p = p.left;
//                else
//                    return p;
//            } else {
//                if (p.right != null) {
//                    p = p.right;
//                } else {
//                    Entry parent = p.parent;
//                    Entry ch = p;
//                    while (parent != null && ch == parent.right) {
//                        ch = parent;
//                        parent = parent.parent;
//                    }
//                    return parent;
//                }
//            }
//        }
//    }
//
//    /**
//     * Returns the entry for the greatest key less than the specified key; if
//     * no such entry exists (i.e., the least key in the Tree is greater than
//     * the specified key), returns <tt>null</tt>.
//     */
//    private Entry getPrecedingEntry(int key) {
//        Entry p = root;
//        if (p==null)
//            return null;
//
//        while (true) {
//            int cmp = compare(key, p.key);
//            if (cmp > 0) {
//                if (p.right != null)
//                    p = p.right;
//                else
//                    return p;
//            } else {
//                if (p.left != null) {
//                    p = p.left;
//                } else {
//                    Entry parent = p.parent;
//                    Entry ch = p;
//                    while (parent != null && ch == parent.left) {
//                        ch = parent;
//                        parent = parent.parent;
//                    }
//                    return parent;
//                }
//            }
//        }
//    }

    /**
     * Returns the key corresonding to the specified Entry.  Throw 
     * NoSuchElementException if the Entry is <tt>null</tt>.
     */
    private static int key(Entry e) {
        if (e==null)
            throw new NoSuchElementException();
        return e.key;
    }
    
    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * 
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the map previously associated <tt>null</tt>
     *         with the specified key.
     * @throws    ClassCastException key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException key is <tt>null</tt> and this map uses
     *         natural order, or its comparator does not tolerate
     *         <tt>null</tt> keys.
     */
    public Object put(int key, Object value) {
        Entry t = root;

        if (t == null) {
            incrementSize();
            root = new Entry(key, value, null);
            return null;
	}

        while (true) {
            int cmp = compare(key, t.key);
            if (cmp == 0) {
                return t.setValue(value);
            } else if (cmp < 0) {
                if (t.left != null) {
                    t = t.left;
                } else {
                    incrementSize();
                    t.left = new Entry(key, value, t);
                    fixAfterInsertion(t.left);
                    return null;
                }
            } else { // cmp > 0
                if (t.right != null) {
                    t = t.right;
                } else {
                    incrementSize();
                    t.right = new Entry(key, value, t);
                    fixAfterInsertion(t.right);
                    return null;
                }
            }
        }
    }

    /**
     * Removes the mapping for this key from this TreeMap if present.
     *
     * @param  key key for which mapping should be removed
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.  A <tt>null</tt> return can
     *         also indicate that the map previously associated
     *         <tt>null</tt> with the specified key.
     * 
     * @throws    ClassCastException key cannot be compared with the keys
     *            currently in the map.
     * @throws NullPointerException key is <tt>null</tt> and this map uses
     *         natural order, or its comparator does not tolerate
     *         <tt>null</tt> keys.
     */
    public Object remove(int key) {
        Entry p = getEntry(key);
        if (p == null)
            return null;

        Object oldValue = p.value;
        deleteEntry(p);
        return oldValue;
    }

    /**
     * Removes all mappings from this TreeMap.
     */
    public void clear() {
        modCount++;
        size = 0;
        root = null;
    }
    
    /**
     * Test two values  for equality.  Differs from o1.equals(o2) only in
     * that it copes with with <tt>null</tt> o1 properly.
     */
    private static boolean valEquals(Object o1, Object o2) {
        return (o1==null ? o2==null : o1.equals(o2));
    }
    
    private static final boolean RED   = false;
    private static final boolean BLACK = true;
    static class Entry {
	int key;
	Object value;
	Entry left = null;
	Entry right = null;
	Entry parent;
	boolean color = BLACK;
		
	/**
         * Make a new cell with given key, value, and parent, and with 
         * <tt>null</tt> child links, and BLACK color. 
         */
        Entry(int key, Object value, Entry parent) { 
            this.key = key;
            this.value = value;
            this.parent = parent;
        }
        
        Entry(Entry other, Entry parent) {
            this.key = other.key;
            this.value = other.value;
            this.parent = parent;
            if (other.left != null) {
                this.left = new Entry(other.left, this);
            }
            if (other.right != null) {
                this.right = new Entry(other.right, this);
            }
        }

        /**
         * Returns the key.
         *
         * @return the key.
         */
        public int getKey() { 
            return key; 
        }

        /**
         * Returns the value associated with the key.
         *
         * @return the value associated with the key.
         */
        public Object getValue() {
            return value;
        }

        /**
         * Replaces the value currently associated with the key with the given
         * value.
         *
         * @return the value associated with the key before this method was
         *           called.
         */
        public Object setValue(Object value) {
            Object oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
	    if (o instanceof Entry) {
		Entry e = (Entry)o;
		return key == e.getKey() && valEquals(value,e.getValue());
	    }
	    return false;
        }

        public int hashCode() {
            int keyHash = key;
            int valueHash = (value==null ? 0 : value.hashCode());
            return keyHash ^ valueHash;
        }

        public String toString() {
            return key + "=" + value;
        }
    }
    /**
     * Returns the first Entry in the TreeMap (according to the TreeMap's
     * key-sort function).  Returns null if the TreeMap is empty.
     */
    private Entry firstEntry() {
        Entry p = root;
        if (p != null)
            while (p.left != null)
                p = p.left;
        return p;
    }
    /**
     * Returns the last Entry in the TreeMap (according to the TreeMap's
     * key-sort function).  Returns null if the TreeMap is empty.
     */
    private Entry lastEntry() {
        Entry p = root;
        if (p != null)
            while (p.right != null)
                p = p.right;
        return p;
    }

    /**
     * Returns the successor of the specified Entry, or null if no such.
     */
    private Entry successor(Entry t) {
        if (t == null)
            return null;
        else if (t.right != null) {
            Entry p = t.right;
            while (p.left != null)
                p = p.left;
            return p;
        } else {
            Entry p = t.parent;
            Entry ch = t;
            while (p != null && ch == p.right) {
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    /**
     * Balancing operations.
     *
     * Implementations of rebalancings during insertion and deletion are
     * slightly different than the CLR version.  Rather than using dummy
     * nilnodes, we use a set of accessors that deal properly with null.  They
     * are used to avoid messiness surrounding nullness checks in the main
     * algorithms.
     */

    private static boolean colorOf(Entry p) {
        return (p == null ? BLACK : p.color);
    }

    private static Entry  parentOf(Entry p) { 
        return (p == null ? null: p.parent);
    }

    private static void setColor(Entry p, boolean c) { 
        if (p != null)  p.color = c; 
    }

    private static Entry  leftOf(Entry p) { 
        return (p == null)? null: p.left; 
    }

    private static Entry  rightOf(Entry p) { 
        return (p == null)? null: p.right; 
    }

    /** From CLR **/
    private void rotateLeft(Entry p) {
        Entry r = p.right;
        p.right = r.left;
        if (r.left != null)
            r.left.parent = p;
        r.parent = p.parent;
        if (p.parent == null)
            root = r;
        else if (p.parent.left == p)
            p.parent.left = r;
        else
            p.parent.right = r;
        r.left = p;
        p.parent = r;
    }

    /** From CLR **/
    private void rotateRight(Entry p) {
        Entry l = p.left;
        p.left = l.right;
        if (l.right != null) l.right.parent = p;
        l.parent = p.parent;
        if (p.parent == null)
            root = l;
        else if (p.parent.right == p)
            p.parent.right = l;
        else p.parent.left = l;
        l.right = p;
        p.parent = l;
    }


    /** From CLR **/
    private void fixAfterInsertion(Entry x) {
        x.color = RED;

        while (x != null && x != root && x.parent.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                Entry y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    if (parentOf(parentOf(x)) != null) 
                        rotateRight(parentOf(parentOf(x)));
                }
            } else {
                Entry y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x),  BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    if (parentOf(parentOf(x)) != null) 
                        rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        root.color = BLACK;
    }

    /**
     * Delete node p, and then rebalance the tree.
     */

    private void deleteEntry(Entry p) {
        decrementSize();

        // If strictly internal, copy successor's element to p and then make p
        // point to successor.
        if (p.left != null && p.right != null) {
            Entry s = successor (p);
            p.key = s.key;       
            p.value = s.value;  
            p = s;
        } // p has 2 children

        // Start fixup at replacement node, if it exists.
        Entry replacement = (p.left != null ? p.left : p.right);

        if (replacement != null) {
            // Link replacement to parent
            replacement.parent = p.parent;
            if (p.parent == null)
                root = replacement;
            else if (p == p.parent.left)
                p.parent.left  = replacement;
            else
                p.parent.right = replacement;

            // Null out links so they are OK to use by fixAfterDeletion.
            p.left = p.right = p.parent = null;

            // Fix replacement
            if (p.color == BLACK)
                fixAfterDeletion(replacement);
        } else if (p.parent == null) { // return if we are the only node.
            root = null;
        } else { //  No children. Use self as phantom replacement and unlink.
            if (p.color == BLACK)
                fixAfterDeletion(p);

            if (p.parent != null) {
                if (p == p.parent.left)
                    p.parent.left = null;
                else if (p == p.parent.right)
                    p.parent.right = null;
                p.parent = null;
            }
        }
    }

    /** From CLR **/
    private void fixAfterDeletion(Entry x) {
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                Entry sib = rightOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }

                if (colorOf(leftOf(sib))  == BLACK && 
                    colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib,  RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            } else { // symmetric
                Entry sib = leftOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }

                if (colorOf(rightOf(sib)) == BLACK && 
                    colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib,  RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }

        setColor(x, BLACK); 
    }

    /**
     * IntIntSortedMap Iterator.
     */
    private class EntryIterator implements Iterator {
        private int expectedModCount;
        private Entry lastReturned = null;
        Entry next;

        EntryIterator() {
            next = firstEntry();
            expectedModCount = modCount;
        }

        // Used by SubMapEntryIterator
        EntryIterator(Entry first) {
            next = first;
            expectedModCount = modCount;
        }

        public boolean hasNext() {
            return next != null;
        }

        final Entry nextEntry() {
            if (next == null)
                throw new NoSuchElementException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            lastReturned = next;
            next = successor(next);
            return lastReturned;
        }

        public Object next() {
            return nextEntry();
        }

        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (lastReturned.left != null && lastReturned.right != null) 
                next = lastReturned; 
            deleteEntry(lastReturned);
            expectedModCount++;
            lastReturned = null;
        }
    }

    private class KeyIterator extends EntryIterator implements RowIterator {
        public KeyIterator() {
            super();
        }
        public KeyIterator(Entry next) {
            super(next);
        }
        public int peekRow() {
            return next.key;
        }
        public Object next() {
            return new Integer(nextRow());
        }
        public int nextRow() {
            return nextEntry().key;
        }
        public RowIterator copy() {
            return new KeyIterator(next);
        }
    }
    
    private class ValueIterator extends EntryIterator {
        public Object peekRow() {
            return next.value;
        }
        
        public Object next() {
            return nextEntry().value;
        }
    }
    
    /**
     * Returns an iterator over the defined keys.
     * @return an iterator over the defined keys
     */
    public RowIterator keyIterator() {
        return new KeyIterator();
    }
    
    /**
     * Returns an iterator over the defined values.
     * @return an iterator over the defined values
     */
    public Iterator valueIterator() {
        return new ValueIterator();
    }
    
    protected Object clone() throws CloneNotSupportedException {
        return new IntSortedMap(this);
    }

    /**
     * Returns the compartor.
     * @return the compartor
     */
    public IntComparator getComparator() {
        return comparator;
    }
}
