package com.example.f_space.bootstrap;


import com.example.f_space.model.Intake;
import com.example.f_space.model.Medication;
import com.example.f_space.model.Schedule;
import com.example.f_space.model.SkipReason;
import com.example.f_space.repository.IntakeRepository;
import com.example.f_space.repository.MedicationRepository;
import com.example.f_space.repository.ScheduleRepository;
import com.example.f_space.repository.SkipReasonRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class DataLoader implements CommandLineRunner {

    private MedicationRepository medicationRepository;

    private ScheduleRepository scheduleRepository;

    private IntakeRepository intakeRepository;

    private SkipReasonRepository skipReasonRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create Medications
        Medication paracetamol = new Medication();
        paracetamol.setName("Paracetamol");
        paracetamol.setDosageForm("Tablet");
        paracetamol.setStrength("500mg");

        Medication ibuprofen = new Medication();
        ibuprofen.setName("Ibuprofen");
        ibuprofen.setDosageForm("Capsule");
        ibuprofen.setStrength("200mg");

        medicationRepository.saveAll(List.of(paracetamol, ibuprofen));

        // Create Schedules
        Schedule schedule1 = new Schedule();
        schedule1.setMedication(paracetamol);
        schedule1.setUserId(1L);
        schedule1.setScheduledTime(Time.valueOf("08:00:00"));
        schedule1.setDaysOfWeek(Arrays.asList(1, 3, 5));

        Schedule schedule2 = new Schedule();
        schedule2.setMedication(ibuprofen);
        schedule2.setUserId(2L);
        schedule2.setScheduledTime(Time.valueOf("14:00:00"));
        schedule2.setDaysOfWeek(Arrays.asList(2, 4, 6));

        scheduleRepository.saveAll(List.of(schedule1, schedule2));



        for (int i = 1; i <= 30; i++) {
            Intake intake = new Intake();
            intake.setSchedule(schedule1);

            if (i % 5 == 0) {
                // Every 5th intake is skipped
                intake.setStatus("SKIPPED");
                intake.setScheduledFor(Timestamp.valueOf(LocalDateTime.now().minusDays(i)));
                intake.setTakenAt(null);
            } else if (i % 3 == 0) {
                // Every 3rd intake is missed
                intake.setStatus("MISSED");
                intake.setScheduledFor(Timestamp.valueOf(LocalDateTime.now().minusDays(i)));
                intake.setTakenAt(null);
            } else {
                // All other intakes are taken
                intake.setStatus("TAKEN");
                intake.setScheduledFor(Timestamp.valueOf(LocalDateTime.now().minusDays(i)));
                intake.setTakenAt(Timestamp.valueOf(LocalDateTime.now().minusDays(i)));
            }

            intakeRepository.save(intake);
        }


        List<Intake> intakes = intakeRepository.findAll();

        for (Intake intake : intakes) {
            if ("SKIPPED".equals(intake.getStatus())) {
                SkipReason reason = new SkipReason();
                reason.setIntake(intake);
                reason.setReasonType("Patient Request");
                reason.setSpecificReason("Patient was feeling unwell and chose to skip the dose.");
                reason.setAuthorizedBy(intake.getId() % 2 == 0 ? "Doctor Smith" : null);
                skipReasonRepository.save(reason);
            }
        }

        System.out.println("Sample data loaded successfully.");
    }
}
