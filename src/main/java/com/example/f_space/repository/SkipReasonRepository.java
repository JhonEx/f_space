package com.example.f_space.repository;

import com.example.f_space.model.Schedule;
import com.example.f_space.model.SkipReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkipReasonRepository extends JpaRepository<SkipReason, Long> {
}
