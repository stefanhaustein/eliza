package com.github.stefanhaustein.eliza;

import java.util.List;

/**
 *  Eliza key.
 *  A key has the key itself, a rank, and a list of decompositon rules.
 */
public class Key {
    /** The key itself */
    private String key;
    /** The numerical rank */
    private int rank;
    /** The list of decompositions */
    private List<Decomposition> decomp;

    /**
     *  Initialize the key.
     */
    Key(String key, int rank, List<Decomposition> decomp) {
        this.key = key;
        this.rank = rank;
        this.decomp = decomp;
    }

    /**
     *  Another initialization for gotoKey.
     */
    Key() {
        key = null;
        rank = 0;
        decomp = null;
    }

    public void copy(Key k) {
        key = k.key();
        rank = k.rank();
        decomp = k.decomp();
    }


    /**
     *  Get the key value.
     */
    public String key() {
        return key;
    }

    /**
     *  Get the rank.
     */
    public int rank() {
        return rank;
    }

    /**
     *  Get the decomposition list.
     */
    public List<Decomposition> decomp() {
        return decomp;
    }
}

