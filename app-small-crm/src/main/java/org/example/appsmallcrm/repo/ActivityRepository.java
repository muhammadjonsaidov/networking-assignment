package org.example.appsmallcrm.repo;

import org.example.appsmallcrm.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    Page<Activity> findByActor(String actor, Pageable pageable);
    Page<Activity> findByAction(String action, Pageable pageable);
}