package me.kverna.roger.server.repository;

import me.kverna.roger.server.data.WebhookUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WebhookUrlRepository extends JpaRepository<WebhookUrl, Long> {
}
