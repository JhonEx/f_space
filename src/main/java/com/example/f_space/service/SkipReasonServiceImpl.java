package com.example.f_space.service;

import com.example.f_space.model.SkipReason;
import com.example.f_space.repository.SkipReasonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SkipReasonServiceImpl implements SkipReasonService {

    @Autowired
    private SkipReasonRepository skipReasonRepository;

    @Override
    public SkipReason saveSkipReason(SkipReason skipReason) {
        return skipReasonRepository.save(skipReason);
    }

    @Override
    public Optional<SkipReason> getSkipReasonById(Long id) {
        return skipReasonRepository.findById(id);
    }

    @Override
    public List<SkipReason> getSkipReasonsByIntakeId(Long intakeId) {
        return null;
    }

    @Override
    public SkipReason updateSkipReason(Long id, SkipReason skipReasonDetails) {
        SkipReason existingReason = skipReasonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SkipReason not found with id " + id));
        existingReason.setReasonType(skipReasonDetails.getReasonType());
        existingReason.setSpecificReason(skipReasonDetails.getSpecificReason());
        existingReason.setAuthorizedBy(skipReasonDetails.getAuthorizedBy());
        return skipReasonRepository.save(existingReason);
    }

    @Override
    public void deleteSkipReason(Long id) {
        SkipReason skipReason = skipReasonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SkipReason not found with id " + id));
        skipReasonRepository.delete(skipReason);
    }
}
