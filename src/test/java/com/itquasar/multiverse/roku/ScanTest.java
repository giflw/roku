package com.itquasar.multiverse.roku;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class ScanTest {

    @Test
    public void scanForRokuTest(){
        Set<String> expected = new TreeSet<String>(){{
            add("192.168.1.2");
        }};
        Scan scan = new Scan();
        Collection<String> rokus = scan.scanForRokus();
        Assert.assertEquals(expected, rokus);
    }
}
