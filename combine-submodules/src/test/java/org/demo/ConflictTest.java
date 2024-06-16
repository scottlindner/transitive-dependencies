package org.demo;

import com.google.common.util.concurrent.Futures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConflictTest {

    @Test
    public void whenVersionCollisionDoesNotExist_thenShouldCompile() {
        assertNotNull(Futures.immediateVoidFuture());
    }

}
