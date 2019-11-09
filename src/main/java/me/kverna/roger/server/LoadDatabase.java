package me.kverna.roger.server;

import lombok.extern.java.Log;
import me.kverna.roger.server.data.Camera;
import me.kverna.roger.server.repository.CameraRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Loads a development configuration of the database with predefined cameras.
 */
@Log
@Configuration
public class LoadDatabase {

    private static <T> void addEntities(JpaRepository<T, ?> repository, T[] entities) {
        for (T entity : entities) {
            log.info("Preloading " + repository.save(entity));
        }
    }

    @Bean
    CommandLineRunner addTestCamera(CameraRepository repository) {
        return args -> addEntities(repository, new Camera[]{
                // new Camera("rogercam1", 8080, "rogercam1", "Heftig kamera i stua"),
                new Camera("rogercam2", 8080, "rogercam2", "Kamera på kjøkkenet")
        });
    }
}
