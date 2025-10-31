package com.doctor.appointment.service;

import com.doctor.appointment.model.Doctor;
import com.doctor.appointment.model.WorkingHour;
import com.doctor.appointment.model.DTO.WorkingHourCreateDTO;
import com.doctor.appointment.model.DTO.WorkingHourDTO;
import com.doctor.appointment.repository.DoctorRepository;
import com.doctor.appointment.repository.WorkingHourRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkingHourServiceImpl implements WorkingHourService {

    @Autowired
    private WorkingHourRepository workingHourRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<WorkingHourDTO> list(int doctorId) {
        return workingHourRepository.findByDoctorIdOrderByDayOfWeekAscSequenceAsc(doctorId)
                .stream()
                .map(wh -> modelMapper.map(wh, WorkingHourDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkingHourDTO create(int doctorId, WorkingHourCreateDTO dto) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + doctorId));

        validateTimes(dto.getStartTime(), dto.getEndTime());
        validateOverlap(doctorId, null, dto);

        WorkingHour wh = new WorkingHour();
        wh.setDoctor(doctor);
        wh.setDayOfWeek(dto.getDayOfWeek());
        wh.setStartTime(dto.getStartTime());
        wh.setEndTime(dto.getEndTime());
        wh.setSequence(dto.getSequence());

        WorkingHour saved = workingHourRepository.save(wh);
        return modelMapper.map(saved, WorkingHourDTO.class);
    }

    @Override
    @Transactional
    public WorkingHourDTO update(int doctorId, Long id, WorkingHourCreateDTO dto) {
        WorkingHour existing = workingHourRepository.findByIdAndDoctorId(id, doctorId)
                .orElseThrow(() -> new RuntimeException("Working hour not found for doctor:" + doctorId + ", id:" + id));

        validateTimes(dto.getStartTime(), dto.getEndTime());
        validateOverlap(doctorId, id, dto);

        existing.setDayOfWeek(dto.getDayOfWeek());
        existing.setStartTime(dto.getStartTime());
        existing.setEndTime(dto.getEndTime());
        existing.setSequence(dto.getSequence());

        WorkingHour saved = workingHourRepository.save(existing);
        return modelMapper.map(saved, WorkingHourDTO.class);
    }

    @Override
    @Transactional
    public void delete(int doctorId, Long id) {
        WorkingHour existing = workingHourRepository.findByIdAndDoctorId(id, doctorId)
                .orElseThrow(() -> new RuntimeException("Working hour not found for doctor:" + doctorId + ", id:" + id));
        workingHourRepository.delete(existing);
    }

    private void validateTimes(LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            throw new RuntimeException("Start and end time are required");
        }
        if (!end.isAfter(start)) {
            throw new RuntimeException("End time must be after start time");
        }
    }

    private void validateOverlap(int doctorId, Long excludeId, WorkingHourCreateDTO dto) {
        var sameDay = workingHourRepository.findByDoctorIdAndDayOfWeekOrderByStartTimeAsc(doctorId, dto.getDayOfWeek());
        for (WorkingHour wh : sameDay) {
            if (excludeId != null && wh.getId().equals(excludeId)) continue;
            if (overlaps(wh.getStartTime(), wh.getEndTime(), dto.getStartTime(), dto.getEndTime())) {
                throw new RuntimeException("Working hours overlap with existing interval");
            }
            if (wh.getSequence() == dto.getSequence()) {
                // sequence should be unique for the day
                throw new RuntimeException("Sequence already exists for this day");
            }
        }
    }

    private boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }
}
