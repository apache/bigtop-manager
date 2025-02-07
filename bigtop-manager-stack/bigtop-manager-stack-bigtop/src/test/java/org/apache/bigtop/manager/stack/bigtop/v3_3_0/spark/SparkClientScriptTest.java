package org.apache.bigtop.manager.stack.bigtop.v3_3_0.spark;

import org.apache.bigtop.manager.stack.core.spi.param.Params;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class SparkClientScriptTest {

    private final SparkClientScript sparkClientScript = new SparkClientScript();

    @Test
    public void testGetComponentName() {
        assertEquals("spark_client", sparkClientScript.getComponentName());
    }

    @Test
    public void testAddParamsNull() {
        Params params = null;
        assertThrows(NullPointerException.class, () -> sparkClientScript.add(params));
    }

    @Test
    public void testConfigureParamsNull() {
        Params params = null;
        assertThrows(NullPointerException.class, () -> sparkClientScript.configure(params));
    }
}
