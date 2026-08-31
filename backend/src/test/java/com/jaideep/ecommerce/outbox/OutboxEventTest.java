package com.jaideep.ecommerce.outbox;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class OutboxEventTest {
    @Test
    void eventStartsUnpublishedAndCanBePublished() {
        OutboxEvent event = new OutboxEvent(
            "12345678-1234-1234-1234-123456789012",
            "Product",
            1L,
            "UPSERT",
            "product-events",
            "event|UPSERT|1",
            Instant.now()
        );

        assertFalse(event.isPublished());
        assertEquals(0, event.getAttempts());

        event.markAttemptFailed();
        assertEquals(1, event.getAttempts());

        event.markPublished();
        assertTrue(event.isPublished());
        assertNotNull(event.getPublishedAt());
    }
}
