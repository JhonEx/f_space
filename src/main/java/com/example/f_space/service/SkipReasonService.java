package com.example.f_space.service;


import com.example.f_space.model.SkipReason;

import java.util.List;
import java.util.Optional;

public interface SkipReasonService {
    SkipReason saveSkipReason(SkipReason skipReason);
    Optional<SkipReason> getSkipReasonById(Long id);
    List<SkipReason> getSkipReasonsByIntakeId(Long intakeId);
    SkipReason updateSkipReason(Long id, SkipReason skipReasonDetails);
    void deleteSkipReason(Long id);
}