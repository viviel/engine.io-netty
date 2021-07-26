package com.github.viviel.engine.io;

import org.junit.jupiter.api.BeforeAll;

public abstract class AbstractTest {

    @BeforeAll
    static void beforeAll() {
//        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");
    }
}
