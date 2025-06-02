package org.example.appsmallcrm.service;

import lombok.RequiredArgsConstructor;
import org.example.appsmallcrm.entity.Activity;
import org.example.appsmallcrm.repo.ActivityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;

    // Run in a new transaction to ensure activity is logged even if main transaction rolls back
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordActivity(String actor, String action, String details) {
        Activity activity = new Activity(actor, action, details);
        activityRepository.save(activity);
    }
    
    @Transactional(readOnly = true)
    public Page<Activity> getActivities(Pageable pageable) {
        return activityRepository.findAll(pageable);
    }
}