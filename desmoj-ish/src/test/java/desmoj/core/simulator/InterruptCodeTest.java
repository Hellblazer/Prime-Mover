package desmoj.core.simulator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class checks the InterruptCode functionality.
 *
 * @author Sascha Winde, Clara Bluemm
 */
public class InterruptCodeTest {

    InterruptCode interrupt1;
    InterruptCode interrupt2;

    @BeforeEach
    public void setUp() throws Exception {
        this.interrupt1 = new InterruptCode("Test");
        this.interrupt2 = new InterruptCode("Code");
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * This Test checks the given interruptcode Numbers to be not equal.
     */
    @Test
    public void testCodeNumber() {
        int a = interrupt1.getCodeNumber();
        assertTrue(a == interrupt1.getCodeNumber());
        assertFalse(interrupt1.getCodeNumber() == interrupt2.getCodeNumber());
    }

    /**
     * This Test checks if two codes are equal or not
     */
    @Test
    public void testCodesNotEqual() {
        assertFalse(InterruptCode.equals(interrupt2, interrupt1));
        assertTrue(InterruptCode.equals(interrupt2, interrupt2));
    }

}
