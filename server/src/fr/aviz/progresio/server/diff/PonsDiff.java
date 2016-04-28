//File: Diff.java
//-----------------------------------------------------------------------------
//WikipediaParser -- Analyse the wikipedia xml archives download.wikimedia.org
//Copyright (C) 2006 Pascal Pons
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//-----------------------------------------------------------------------------
//Author   : Pascal Pons 
//Email    : pons@liafa.jussieu.fr
//Web page : http://www.liafa.jussieu.fr/~pons/
//Location : Paris, France
//Time     : June 2006
//-----------------------------------------------------------------------------
package fr.aviz.progresio.server.diff;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;

import cern.colt.function.IntIntProcedure;


/**
 * Compute the Diffs between two strings.
 * 
 * Presumably works by minimizing the Levenshtein distance:
 * 
 *   V. I. Levenshtein, Binary codes capable of correcting deletions, insertions, and reversals.
 *   Soviet Physics Doklady 10 (1966):707���710.
 * 
 * @author Pascal Pons and Aviz people
 * @version $Revision$
 */
public class PonsDiff implements Diff {
	
    private char[]       oldString;
    private char[]       newString;

    private int[]        oldMap;
    private int[]        newMap;

    private int          oldBegin;
    private int          newBegin;
    private int          oldLength;
    private int          newLength;

    private int[]        oldHash;

    private int[]        oldH;
    private int[]        newH;

    private ArrayList<int[]> B;
    // blocks : old_pos -- new_pos -- length -- new_order --
    // old_order_of_the_ith_new_block

    private ArrayList<int[]> M;

    private int           insertCount;
    private int           deleteCount;
    
    private boolean  considerMoves;

    /**
     * Computes the difference between two strings.
     * @param o the old string
     * @param n the new string
     */
    public PonsDiff(String o, String n) {
        considerMoves = false;
    	build(o,n);
    }

    
    public PonsDiff(String o, String n, boolean moves){
    	considerMoves = moves;
    	build(o,n);
    }
    
    private void build(String o, String n){
    	oldString = o.toCharArray();
        newString = n.toCharArray();

        oldBegin = 0;
        newBegin = 0;
        oldLength = o.length();
        newLength = n.length();

        oldMap = new int[oldLength];
        Arrays.fill(oldMap, -1);
        newMap = new int[newLength];
        Arrays.fill(newMap, -1);

        M = new ArrayList<int[]>();
        B = new ArrayList<int[]>();

        oldHash = new int[oldLength];
        oldH = new int[2 * oldLength + 1];
        newH = new int[2 * newLength + 1];
        computeDiff();
//System.out.println("\n\n\n\n\n**************************************\n\n\n\n\n");
//System.out.println(this);
    }
    
    public String toString() {
    	return DiffUtils.toString(getDiffs());
    }
    
    private static int hash(int i, int L) {
        if (i > 0)
            return (i % L);
        else
            return (-(i % L));
    }

    private int detectSameBegin() {
        int i = 0;
        while (i < oldLength && i < newLength
                && oldString[oldBegin + i] == newString[newBegin + i]) {
            oldMap[oldBegin + i] = newBegin + i;
            newMap[newBegin + i] = oldBegin + i;
            i++;
        }
        return i;
    }

    private int detectSameEnd() {
        int i = 0;
        while (i < oldLength
                && i < newLength
                && oldString[oldBegin + oldLength - 1 - i] 
                             == newString[newBegin + newLength - 1 - i]) {
            if (oldMap[oldBegin + oldLength - 1 - i] != -1) {
                if (Character.isWhitespace(oldString[oldBegin + oldLength - 1 - i]))
                    break;
                newMap[oldMap[oldBegin + oldLength - 1 - i]] = -1;
            }
            if (newMap[newBegin + newLength - 1 - i] != -1) {
                if (Character.isWhitespace(newString[newBegin + newLength - 1 - i]))
                    break;
                oldMap[newMap[newBegin + newLength - 1 - i]] = -1;
            }
            oldMap[oldBegin + oldLength - 1 - i] = newBegin + newLength - 1 - i;
            newMap[newBegin + newLength - 1 - i] = oldBegin + oldLength - 1 - i;
            i++;
        }
        return i;
    }

    private void computeHash(
            int block_size,
            int start_old,
            int length_old,
            int start_new,
            int length_new) {
        int d = 32 / block_size;
        int dist_last_mapped = 0;
        int h = 0;

        for (int i = start_old; i < start_old + length_old; i++) {
            h <<= d;
            h ^= oldString[i];

            dist_last_mapped++;
            if (oldMap[i] != -1)
                dist_last_mapped = 0;

            if (dist_last_mapped >= block_size) {
                oldHash[i - block_size + 1] = h;
                int r = hash(h, oldH.length);
                if (oldH[r] == 0)
                    oldH[r] = i - block_size + 1;
                else
                    oldH[r] = -1;
            }
        }

        h = 0;
        dist_last_mapped = 0;
        for (int i = start_new; i < start_new + length_new; i++) {
            h <<= d;
            h ^= newString[i];

            dist_last_mapped++;
            if (newMap[i] != -1)
                dist_last_mapped = 0;

            if (dist_last_mapped >= block_size) {
                int r = hash(h, newH.length);
                if (newH[r] == 0)
                    newH[r] = i - block_size + 1;
                else
                    newH[r] = -1;
            }
        }
    }

    private void findBlocks() {
        B.clear();
        int total_block_length = 0;

        int from = oldBegin;
        IntIntSortedMap T = new IntIntSortedMap();
       // TreeMap<Integer, Integer> T = new TreeMap<Integer, Integer>();

        while (true) {
            while (from < oldBegin + oldLength && oldMap[from] == -1)
                from++;
            if (from >= oldBegin + oldLength)
                break;

            int length = 1;
            while (from + length < oldBegin + oldLength
                    && oldMap[from + length] == oldMap[from + length - 1] + 1)
                length++;

            int[] tmp = new int[5];
            tmp[0] = from;
            tmp[1] = oldMap[from];
            tmp[2] = length;
            B.add(tmp);
            T.put(oldMap[from], B.size() - 1);

            total_block_length += length;
            from += length;
        }

        //int i = 0;
//        for (Integer it : T.values()) {
//            B.get(it)[3] = i;
//            B.get(i)[4] = it;
//            i++;
//        }
        T.forEachPair(new IntIntProcedure() {
            int i = 0;
            public boolean apply(int key, int val) {
                B.get(val)[3] = i;
                B.get(i)[4] = val;
                i++;
                return true;
            }
        });

        insertCount = newLength - total_block_length;
        deleteCount = oldLength - total_block_length;
    }

    private void findMoves() {
        M.clear();
        if (B.size() == 0)
            return;

        int[][] P = new int[B.size()][2];
        // P : pos_new -- from after insertions -- length

        for (int k = 0; k < B.size(); k++) {
            int[] T = B.get(k);
            P[k][0] = T[3];
            if (T[3] == B.size() - 1)
                P[k][1] = newBegin + newLength - T[1];
            else
                P[k][1] = B.get(B.get(T[3] + 1)[4])[1] - T[1];
        }

        int[][] tmp = new int[B.size()][2];
        boolean[] seen = new boolean[B.size()];

        while (true) {

            // out.println("$$$");
            // for(int k = 0; k < B.size(); k++) out.println(P[k][0]);
            // out.println("$$$");

            boolean stop = true;
            for (int k = 0; k < B.size(); k++)
                stop = stop && (P[k][0] == k);
            if (stop)
                break;

            int best_a = 0;
            int best_b = 0;
            int best_c = 0;
            int best_nb_good = -1;
            int best_size = newLength;

            int a = 0;
            int b = 0;
            int c = 0;

            // find best transposition

            if (B.size() < 20) {

                a = 0;
                while (P[a][0] == a)
                    a++;

                while (a < B.size()) {
                    while (a < B.size() && a > 0 && P[a][0] == P[a - 1][0] + 1)
                        a++;

                    b = a + 1;
                    while (b < B.size()) {
                        while (b < B.size() && P[b][0] == P[b - 1][0] + 1)
                            b++;

                        c = b;
                        while (c < B.size()) {
                            while (c + 1 < B.size()
                                    && P[c + 1][0] == P[c][0] + 1)
                                c++;

                            int nb_good = 0;
                            if (P[a][0] == P[c][0] + 1)
                                nb_good++;
                            if (a == 0 && P[b][0] == 0)
                                nb_good++;
                            if (a > 0 && P[b][0] == P[a - 1][0] + 1)
                                nb_good++;
                            if (c == B.size() - 1
                                    && P[b - 1][0] == B.size() - 1)
                                nb_good++;
                            if (c < B.size() - 1
                                    && P[c + 1][0] == P[b - 1][0] + 1)
                                nb_good++;

                            if (nb_good >= best_nb_good) {
                                int size = 0;
                                for (int k = a; k <= c; k++)
                                    size += P[k][1];
                                if (nb_good > best_nb_good || size < best_size) {
                                    best_nb_good = nb_good;
                                    best_size = size;
                                    best_a = a;
                                    best_b = b;
                                    best_c = c;
                                }
                            }
                            c++;
                        }
                        b++;
                    }
                    a++;
                }

            }
            else { // B.size() >= 20
                for (int k = 0; k < B.size(); k++)
                    seen[k] = false;
                for (b = 1; b < B.size(); b++) {
                    seen[P[b - 1][0]] = true;
                    if (P[b][0] == P[b - 1][0] + 1)
                        continue;
                    if ((P[b][0] == 0 || seen[P[b][0] - 1])
                            && (P[b - 1][0] == B.size() - 1 || !seen[P[b - 1][0]])) {
                        for (a = b - 1; a > 0 && P[a - 1][0] != P[b][0] - 1; a--)
                            ;
                        for (c = b; c < B.size() - 1
                                && P[c + 1][0] != P[b - 1][0] + 1; c++)
                            ;
                        break;
                    }
                }

                if (b == B.size()) {
                    a = 0;
                    while (P[a][0] == a)
                        a++;
                    b = a + 1;
                    while (P[b][0] == P[b - 1][0] + 1)
                        b++;
                    c = b;
                    while (P[c][0] != P[a][0] - 1)
                        c++;
                }

                best_a = a;
                best_b = b;
                best_c = c;
            }

            /*
             * a = 0; while(P[a][0] == a) a++; b = a + 1; while(P[b][0] ==
             * P[b-1][0] + 1) b++; c = b; while(P[c][0] != P[a][0] - 1) c++;
             * 
             * best_a = a; best_b = b; best_c = c;
             */

            // out.println("&&&" + best_a + " " + best_b + " " + best_c);

            // do the transposition
            int from = oldBegin + B.get(B.get(0)[4])[1] - newBegin;
            for (int k = 0; k < best_a; k++)
                from += P[k][1];
            int l1 = 0;
            for (int k = best_a; k < best_b; k++)
                l1 += P[k][1];
            int l2 = 0;
            for (int k = best_b; k <= best_c; k++)
                l2 += P[k][1];
            int[] T = new int[3];
            T[0] = from;
            T[1] = l1;
            T[2] = l2;
            M.add(T);

            for (int k = best_a; k < best_b; k++) {
                tmp[k][0] = P[k][0];
                tmp[k][1] = P[k][1];
            }
            for (int k = best_b; k <= best_c; k++) {
                P[k + best_a - best_b][0] = P[k][0];
                P[k + best_a - best_b][1] = P[k][1];
            }
            for (int k = best_a; k < best_b; k++) {
                P[k + best_c - best_b + 1][0] = tmp[k][0];
                P[k + best_c - best_b + 1][1] = tmp[k][1];
            }
        }
    }

    private void computeDiff() {

        int same_begin = detectSameBegin();
        int same_end = 0;
        if (same_begin != oldLength && same_begin != newLength)
            same_end = detectSameEnd();

        if (same_begin + same_end > oldLength)
            same_begin -= same_begin + same_end - oldLength;
        if (same_begin + same_end > newLength)
            same_begin -= same_begin + same_end - newLength;

        computeHash(16, oldBegin + same_begin, oldLength - same_begin
                - same_end, newBegin + same_begin, newLength - same_begin
                - same_end);
        findIdem(16, oldBegin + same_begin, oldLength - same_begin
                - same_end);

        // for(int i = 0; i < hash_old.length; i++) hash_old[i] = 0;

        oldH = new int[523];
        newH = new int[523];

        findBlocks();

        for (int k = 0; k < B.size(); k++) {
            int[] T = B.get(k);
            int start_old = T[0] + T[2];
            int start_new = T[1] + T[2];
            int length_old, length_new;
            if (k == B.size() - 1)
                length_old = oldBegin + oldLength - start_old;
            else
                length_old = B.get(k + 1)[0] - start_old;
            if (T[3] == B.size() - 1)
                length_new = newBegin + newLength - start_new;
            else
                length_new = B.get(B.get(T[3] + 1)[4])[1] - start_new;

            if (length_old > 4 && length_new > 4 && length_old < 512
                    && length_new < 512) {
                for (int i = start_old; i < start_old + length_old; i++)
                    oldHash[i] = 0;
                for (int i = 0; i < oldH.length; i++)
                    oldH[i] = 0;
                for (int i = 0; i < newH.length; i++)
                    newH[i] = 0;
                computeHash(4, start_old, length_old, start_new, length_new);
                findIdem(4, start_old, length_old);
            }

        }

        // for(int i = 0; i < H_old.length; i++) H_old[i] = 0;
        // for(int i = 0; i < H_new.length; i++) H_new[i] = 0;

        findBlocks();

        // FIXME
        if (considerMoves)
        	findMoves();
        // print_diff();
    }

    private void findIdem(int block_size, int start_old, int length_old) {
        for (int i = start_old; i < start_old + length_old; i++) {
            if (oldHash[i] == 0)
                continue;
            int r_old = hash(oldHash[i], oldH.length);
            if (oldH[r_old] == -1)
                continue;
            int r_new = hash(oldHash[i], newH.length);
            if (newH[r_new] <= 0)
                continue;

            int idem_from_old = oldH[r_old];
            int idem_from_new = newH[r_new];
            int idem_length = 0;

            while (idem_from_old + idem_length < oldBegin + oldLength
                    && idem_from_new + idem_length < newBegin + newLength
                    && oldString[idem_from_old + idem_length] == newString[idem_from_new
                            + idem_length]) {
                if ((oldMap[idem_from_old + idem_length] != -1 || newMap[idem_from_new
                        + idem_length] != -1)
                        && Character.isWhitespace(newString[idem_from_new
                                + idem_length]))
                    break;
                idem_length++;
            }

            while (idem_from_old > oldBegin && idem_from_new > newBegin
                    && oldString[idem_from_old - 1] == newString[idem_from_new - 1]) {
                if ((oldMap[idem_from_old - 1] != -1 || newMap[idem_from_new - 1] != -1)
                        && Character.isWhitespace(newString[idem_from_new - 1]))
                    break;
                idem_from_old--;
                idem_from_new--;
                idem_length++;
            }

            if (idem_length >= block_size) {
                i = idem_from_old + idem_length;
                for (int k = 0; k < idem_length; k++) {
                    if (oldMap[idem_from_old + k] != -1)
                        newMap[oldMap[idem_from_old + k]] = -1;
                    oldMap[idem_from_old + k] = idem_from_new + k;
                    if (newMap[idem_from_new + k] != -1)
                        oldMap[newMap[idem_from_new + k]] = -1;
                    newMap[idem_from_new + k] = idem_from_old + k;
                }
            }
        } // for i
    }
    
    /**
     * @return the list of operations.
     */
    public Operation[] getDiffs() {
        ArrayList<Operation> details = new ArrayList<Operation>();
        
        int next_old_block = oldBegin + oldLength;
        int next_new_block;
        for (int k = B.size() - 1; k >= 0; k--) {
            int[] T = B.get(k);

            if (T[0] + T[2] < next_old_block) {
                int from = T[0] + T[2];
                int len = next_old_block - from;
                details.add(
                        new Operation(from, len));
            }
            next_old_block = T[0];

            if (T[3] != B.size() - 1)
                next_new_block = B.get(B.get(T[3] + 1)[4])[1];
            else
                next_new_block = newBegin + newLength;

            if (T[1] + T[2] < next_new_block) {
                int from = T[1] + T[2];
                int len = next_new_block - from;
                details.add(
                        new Operation(
                                T[0] + T[2],
                                len,
                                new String(newString, from, len)));
            }
        }

        if (oldBegin < next_old_block) {
            details.add(new Operation(oldBegin, next_old_block-oldBegin));
        }

        if (B.size() > 0)
            next_new_block = B.get(B.get(0)[4])[1];
        else
            next_new_block = newBegin + newLength;

        if (newBegin < next_new_block) {
            int len = next_new_block - newBegin;
            details.add(new Operation(oldBegin, len, new String(newString, newBegin, len)));
        }

        for (int k = 0; k < M.size(); k++) {
            int[] T = M.get(k);
            details.add(
                    new Operation(T[0] + T[1], T[2], T[0]));
        }

//        if (!considerMoves)
//        	details = removeMoves(oldString, details);
        
        Collections.sort(details);
                
        Operation[] ops = new Operation[details.size()];
        return details.toArray(ops);
    }

    /**
     * @return the insertCount
     */
    public int getInsertCount() {
        return insertCount;
    }

    /**
     * @return the deleteCount
     */
    public int getDeleteCount() {
        return deleteCount;
    }
    
    public String getOldString() {
    	return new String(oldString);
    }

    public String getNewString() {
    	return new String(newString);
    }
    
    /**
     * FIXME: This does not work. Moves should be applied after all previous operations
     *        have been applied.
     * @param o
     * @param ops
     * @return
     */
    public static ArrayList<Operation> removeMoves(char[] o, ArrayList<Operation> ops) {
    	ArrayList<Operation> ops2 = new ArrayList<Operation>();
    	for (Operation op : ops) {
    		if (op.type == Diff.Type.MOVE) {
    			String text = new String(o, op.from, op.length);
    			ops2.add(new Operation(Diff.Type.DELETE, op.from, op.length, -1, null));
    			ops2.add(new Operation(Diff.Type.INSERT, op.from, op.length, -1, text));
    		} else {
    			ops2.add(op);
    		}
    	}
    	return ops2;
    }
    
    // move test
    public static void main(String[] args) {
    	//          0-4   6-10  12-16 18-22
    	String o = "abcde fghij lmnop rstuv";
    	String n = "abcdz lmnop fghik rstux";
    	
    	PonsDiff diff1 = new PonsDiff(o, n);
    	System.out.println(diff1);
    	//System.out.println(DiffChecker.applyOperations(o, diff1.getDiffs()));
    	System.out.println("Correct: " + DiffChecker.check(o, n, diff1.getDiffs()));
    	System.out.println();

    }
}
