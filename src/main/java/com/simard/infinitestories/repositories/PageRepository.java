package com.simard.infinitestories.repositories;

import com.simard.infinitestories.entities.Page;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageRepository extends JpaRepository<Page, Long> {
}
