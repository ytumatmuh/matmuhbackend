package com.matmuh.matmuhsite.core.utilities.schema;

import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.Lecture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ProgramsBackfillTest {

    @Autowired
    private ProgramsBackfill programsBackfill;

    @Autowired
    private LectureDao lectureDao;

    @Autowired
    private JdbcTemplate jdbc;

    // Program alanı gelmeden önceki dersler: öğrenim düzeyi dolu, program tablosu boş.
    @Test
    void existingLecturesTakeTheirProgramsFromDegreeLevelsOnce() {
        var lecture = new Lecture();
        lecture.setCode("ZZP5101");
        lecture.setSlug("zzp5101");
        lecture.setName("Program göçü");
        lecture.setDegreeLevels(new LinkedHashSet<>(Set.of(DegreeLevel.MASTERS, DegreeLevel.DOCTORATE)));
        var saved = lectureDao.saveAndFlush(lecture);
        jdbc.update("DELETE FROM lecture_programs");

        programsBackfill.backfill();

        var programs = jdbc.queryForList("SELECT program FROM lecture_programs WHERE lecture_id = ? ORDER BY program",
                String.class, saved.getId());
        assertEquals(List.of("DOCTORATE", "MASTERS_THESIS"), programs);

        var other = new Lecture();
        other.setCode("ZZP1101");
        other.setSlug("zzp1101");
        other.setName("Başka ders");
        other = lectureDao.saveAndFlush(other);
        jdbc.update("DELETE FROM lecture_programs");
        jdbc.update("INSERT INTO lecture_programs (lecture_id, program) VALUES (?, 'UNDERGRADUATE')", other.getId());
        programsBackfill.backfill();
        assertTrue(jdbc.queryForList("SELECT program FROM lecture_programs WHERE lecture_id = ?", String.class, saved.getId()).isEmpty(),
                "a lecture whose programs were cleared on purpose stays empty once the table has rows");
    }
}
