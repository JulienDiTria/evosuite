package org.evosuite.junit.writer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LineIndentTest {

    @Test
    public void testDefault(){
        LineIndent lineIndent = new LineIndent();
        assertEquals("", lineIndent.toString());

        lineIndent = new LineIndent(0);
        assertEquals("", lineIndent.toString());

        lineIndent = new LineIndent(1);
        assertEquals("  ", lineIndent.toString());
    }

    @Test
    public void testFailConstruct(){
        try{
            LineIndent lineIndent = new LineIndent(-1);
            fail("should fail with construct level < 0");
        } catch (RuntimeException e){
            //
        }
    }

    @Test
    public void testIncrease(){
        LineIndent lineIndent = new LineIndent();
        lineIndent.increase();

        assertEquals("  ", lineIndent.toString());

        lineIndent.increase(1);
        assertEquals("    ", lineIndent.toString());
    }

    @Test
    public void testDecreaseFail(){
        LineIndent lineIndent = new LineIndent();
        try {
            lineIndent.decrease();
            fail("should fail with decrease level < 0");
        } catch (RuntimeException e){
            //
        }
    }

    @Test
    public void testDecrease(){
        LineIndent lineIndent = new LineIndent(4);
        assertEquals("        ", lineIndent.toString());
        lineIndent.decrease();
        assertEquals("      ", lineIndent.toString());
    }

    @Test
    public void testIncreaseFail(){
        LineIndent lineIndent = new LineIndent(4);
        try {
            lineIndent.increase(-1);
            fail("should fail with increase level < 0");
        } catch (RuntimeException e){
            //
        }
    }

    @Test
    public void testPrint(){
        LineIndent lineIndent = new LineIndent(4);
        assertEquals("        ", lineIndent.toString());
        assertEquals("        ", lineIndent.toString());
    }
}
