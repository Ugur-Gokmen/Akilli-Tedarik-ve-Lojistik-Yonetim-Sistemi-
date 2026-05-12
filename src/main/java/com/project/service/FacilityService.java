package com.project.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.config.AppProperties;
import com.project.domain.facility.Facility;
import com.project.repository.FacilityRepository;

@Service
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final AppProperties appProperties;

    public FacilityService(FacilityRepository facilityRepository, AppProperties appProperties) {
        this.facilityRepository = Objects.requireNonNull(facilityRepository, "facilityRepository");
        this.appProperties = Objects.requireNonNull(appProperties, "appProperties");
    }

    @Transactional
    public List<Facility> listFacilitiesEnsuringDemoData() {
        List<Facility> facilities = facilityRepository.findAll();
        if (!facilities.isEmpty()) {
            return facilities;
        }

        if (!appProperties.getFacilities().isSeedEnabled()) {
            return facilities;
        }

        List<AppProperties.Facilities.FacilitySeed> seed = appProperties.getFacilities().getSeed();
        if (seed == null || seed.isEmpty()) {
            return facilities;
        }

        facilities = seed.stream()
            .map(s -> new Facility(
                s.getName(),
                s.getLocation(),
                s.getType(),
                s.getCurrentCapacity(),
                s.getMaxCapacity(),
                s.getManager(),
                s.getStatus()))
            .toList();

        facilityRepository.saveAll(facilities);
        return facilities;
    }
}

