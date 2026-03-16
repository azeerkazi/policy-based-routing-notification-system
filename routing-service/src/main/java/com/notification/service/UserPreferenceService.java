package com.notification.service;

import com.notification.model.UserPreference;
import com.notification.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPreferenceService {
    private final UserPreferenceRepository userPreferenceRepository;

    public UserPreference getUserPreference(String userId) {
        log.debug("Fetching user preferences for user: {}", userId);
        return userPreferenceRepository.findByUserId(userId);
    }
}