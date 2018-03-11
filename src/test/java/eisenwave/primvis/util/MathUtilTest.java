package eisenwave.primvis.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class MathUtilTest {
    
    @Test
    public void normalize180() {
        //System.out.println(-10 % 360);
        assertEquals(-90, MathUtil.normalize180(270), 0);
        assertEquals(  0, MathUtil.normalize180(360), 0);
        assertEquals( 30, MathUtil.normalize180(-330), 0);
        assertEquals( 30, MathUtil.normalize180(390), 0);
        assertEquals( 90, MathUtil.normalize180(-270), 0);
    }
    
    @Test
    public void normalize360() {
        assertEquals(270, MathUtil.normalize360(270), 0);
        assertEquals(  0, MathUtil.normalize360(360), 0);
        assertEquals( 30, MathUtil.normalize360(-330), 0);
        assertEquals( 30, MathUtil.normalize360(390), 0);
        assertEquals( 90, MathUtil.normalize360(-270), 0);
    }
}
