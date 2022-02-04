package com.github.stefanhaustein.eliza;

import java.util.List;

/**
 *  Eliza decomposition rule
 */
public class DecompositionRule {
    private String pattern;
    private boolean memoryFlag;
    private List<String> reassemblyList;
    private int currentReassemblyPoint;

    /**
     *  Initialize the decomp rule
     */
    DecompositionRule(String pattern, boolean memoryFlag, List<String> reassemblyList) {
        this.pattern = pattern;
        this.memoryFlag = memoryFlag;
        this.reassemblyList = reassemblyList;
        this.currentReassemblyPoint = 100;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean getMemoryFlag() {
        return memoryFlag;
    }

    /**
     *  Get the next reassembly rule.
     */
    public String getCurrentRule() {
        if (reassemblyList.size() == 0) {
            System.out.println("No reassembly rule.");
            return null;
        }
        return reassemblyList.get(currentReassemblyPoint);
    }

    /**
     *  Step to the next reassembly rule.
     *  If mem is true, pick a random rule.
     */
    public void advanceToNextRule() {
        int size = reassemblyList.size();
        if (memoryFlag) {
            currentReassemblyPoint = (int)(Math.random() * size);
        }
        //  Increment and make sure it is within range.
        currentReassemblyPoint++;
        if (currentReassemblyPoint >= size) currentReassemblyPoint = 0;
    }


}
