package com.matmuh.matmuhsite.dataAccess.abstracts;

import com.matmuh.matmuhsite.entities.AcademicTerm;
import com.matmuh.matmuhsite.entities.Semester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicTermDao extends JpaRepository<AcademicTerm, UUID> {

    Optional<AcademicTerm> findByAcademicYearAndSemester(String academicYear, Semester semester);

    List<AcademicTerm> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate to, LocalDate from);

    List<AcademicTerm> findAllByOrderByStartDateDesc();
}
