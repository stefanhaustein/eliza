package com.github.stefanhaustein.eliza;

import java.util.List;

/**
 *  Eliza decomposition rule
 */
public class Decomposition {
    /** The decomp pattern */
    private String pattern;
    /** The mem flag */
    private boolean mem;
    /** The reassembly list */
    private List<String> reasemb;
    /** The current reassembly point */
    private int currReasmb;

    /**
     *  Initialize the decomp rule
     */
    Decomposition(String pattern, boolean mem, List<String> reasemb) {
        this.pattern = pattern;
        this.mem = mem;
        this.reasemb = reasemb;
        this.currReasmb = 100;
    }

    /**
     *  Get the pattern.
     */
    public String pattern() {
        return pattern;
    }

    /**
     *  Get the mem flag.
     */
    public boolean mem() {
        return mem;
    }

    /**
     *  Get the next reassembly rule.
     */
    public String nextRule() {
        if (reasemb.size() == 0) {
            System.out.println("No reassembly rule.");
            return null;
        }
        return reasemb.get(currReasmb);
    }

    /**
     *  Step to the next reassembly rule.
     *  If mem is true, pick a random rule.
     */
    public void stepRule() {
        int size = reasemb.size();
        if (mem) {
            currReasmb = (int)(Math.random() * size);
        }
        //  Increment and make sure it is within range.
        currReasmb++;
        if (currReasmb >= size) currReasmb = 0;
    }


}
