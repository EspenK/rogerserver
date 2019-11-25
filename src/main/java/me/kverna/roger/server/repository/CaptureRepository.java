package me.kverna.roger.server.repository;

import me.kverna.roger.server.data.Capture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaptureRepository extends JpaRepository<Capture, Long> {
}
