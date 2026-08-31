package com.jaideep.ecommerce.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    List<OutboxEvent> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
    long countByPublishedAtIsNull();
    long countByPublishedAtIsNotNull();
}
