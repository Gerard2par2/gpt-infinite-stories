package com.simard.infinitestories.repositories;

import com.simard.infinitestories.entities.World;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface WorldRepository extends JpaRepository<World, Long> {
    @Modifying
    @Query("UPDATE World w SET w.name = ?2, w.description = ?3, w.era = ?4 WHERE w.id = ?1")
    World updateWorldById(Long id, String name, String description, String era);
}
