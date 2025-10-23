package com.sunmi.eid_plugin;

import org.junit.Test;

import static org.junit.Assert.*;

import com.sunmi.eid_plugin.utils.BytesUtil;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);

        byte[] eid_ATR = new byte[]{-128, 101, -94, 1, 49, 1, 61, 114, -42, 65};
        String a = BytesUtil.bytesToHex(eid_ATR);
        System.out.println("a: " + a);
    }
}