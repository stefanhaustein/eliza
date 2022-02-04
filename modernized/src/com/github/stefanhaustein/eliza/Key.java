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
    private List<DecompositionRule> decompositionRuleList;

    /**
     *  Initialize the key.
     */
    Key(String key, int rank, List<DecompositionRule> decompositionRuleList) {
        this.key = key;
        this.rank = rank;
        this.decompositionRuleList = decompositionRuleList;
    }

    /**
     *  Initialization for the gotoKey case.
     */
    Key() {
        key = null;
        rank = 0;
        decompositionRuleList = null;
    }

    public void copy(Key k) {
        key = k.getKey();
        rank = k.getRank();
        decompositionRuleList = k.getDecompositionRuleList();
    }


    public String getKey() {
        return key;
    }

    public int getRank() {
        return rank;
    }


    public List<DecompositionRule> getDecompositionRuleList() {
        return decompositionRuleList;
    }
}

