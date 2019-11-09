package me.kverna.roger.server.repository;

import me.kverna.roger.server.data.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CameraRepository extends JpaRepository<Camera, Integer> {

    Optional<Camera> findByHostAndPort(String host, int port);
}
