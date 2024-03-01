package org.evosuite.junit.writer;

import org.apache.commons.lang3.StringUtils;

public class LineIndent {

    private int level;
    private String value = null;

    private static final int DEFAULT_LEVEL = 0;
    private static final String DEFAULT_INCREASE = "  ";

    public LineIndent() {
        this(DEFAULT_LEVEL);
    }

    public LineIndent(int level){
        if(level<0){
            throw new RuntimeException("LineIndent construction with level=" + level +"<0");
        }
        this.level = level;
    }

    public LineIndent increase(){
        this.level++;
        value = null;
        return this;
    }

    public LineIndent increase(int deltaLvl){
        if(deltaLvl<0){
            throw new RuntimeException("LineIndent trying to increase with deltaLvl=" + deltaLvl +"<0");
        }
        this.level+=deltaLvl;
        value = null;
        return this;
    }

    public LineIndent decrease(){
        this.level--;
        if (level<0){
            throw new RuntimeException("LineIndent level < 0");
        }
        value = null;
        return this;
    }

    @Override
    public String toString() {
        if(value == null){
            value = StringUtils.repeat(DEFAULT_INCREASE, level);
        }
        return value;
    }
}
