package com.matmuh.matmuhsite.core.utilities.schema;

import com.matmuh.matmuhsite.entities.Program;
import com.matmuh.matmuhsite.core.utilities.schema.EnumCheckConstraintSynchronizer.EnumColumn;
import com.matmuh.matmuhsite.entities.AuthProvider;
import com.matmuh.matmuhsite.entities.CalendarEventType;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.EvaluationMethod;
import com.matmuh.matmuhsite.entities.ExamPeriod;
import com.matmuh.matmuhsite.entities.ExamType;
import com.matmuh.matmuhsite.entities.InstructionLanguage;
import com.matmuh.matmuhsite.entities.LectureCategory;
import com.matmuh.matmuhsite.entities.LectureType;
import com.matmuh.matmuhsite.entities.NoteReviewStatus;
import com.matmuh.matmuhsite.entities.NoteType;
import com.matmuh.matmuhsite.entities.Role;
import com.matmuh.matmuhsite.entities.Semester;
import com.matmuh.matmuhsite.entities.ServiceKeyCapability;
import com.matmuh.matmuhsite.entities.StaffGroup;
import com.matmuh.matmuhsite.entities.cms.BlockType;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnumCheckConstraintSynchronizerTest {

    // db/2026-08-23_enum_check_constraints.sql listesinin birebir karşılığı, iki fark hariç:
    // media.media_type ayırıcı kolon (enum değil, CHECK'i Hibernate'in), lectures.degree_level
    // ise 2026-08-22 migration'ının düşürdüğü eski tek değerli kolon — artık hiçbir varlık
    // eşlemiyor, yansımanın onu bulması mümkün değil.
    @Test
    void discoversEveryStringEnumColumnFromTheEntities() {
        var expected = List.of(
                column("academic_terms", "semester", Semester.class),
                column("authorities", "role", Role.class),
                column("calendar_events", "exam_type", ExamType.class),
                column("calendar_events", "type", CalendarEventType.class),
                column("content_blocks", "block_type", BlockType.class),
                column("elective_group_degree_levels", "degree_level", DegreeLevel.class),
                column("elective_group_programs", "program", Program.class),
                column("elective_groups", "semester", Semester.class),
                column("exam_statistics", "exam_type", ExamType.class),
                column("grade_results", "evaluation_method", EvaluationMethod.class),
                column("grade_results", "exam_period", ExamPeriod.class),
                column("lecture_degree_levels", "degree_level", DegreeLevel.class),
                column("lecture_languages", "language", InstructionLanguage.class),
                column("lecture_notes", "review_status", NoteReviewStatus.class),
                column("lecture_notes", "type", NoteType.class),
                column("lecture_offering_exam_weights", "exam_type", ExamType.class),
                column("lecture_offerings", "language", InstructionLanguage.class),
                column("lecture_offerings", "semester", Semester.class),
                column("lecture_programs", "program", Program.class),
                column("lectures", "category", LectureCategory.class),
                column("lectures", "semester", Semester.class),
                column("lectures", "type", LectureType.class),
                column("schedule_slots", "day_of_week", DayOfWeek.class),
                column("service_key_capabilities", "capability", ServiceKeyCapability.class),
                column("staff_groups", "staff_group", StaffGroup.class),
                column("staff_office_hours", "day_of_week", DayOfWeek.class),
                column("users", "provider", AuthProvider.class)
        );

        assertEquals(expected, EnumCheckConstraintSynchronizer.discover());
    }

    @Test
    void legacyBlockTypeNamesStayWritable() {
        var blockType = EnumCheckConstraintSynchronizer.discover().stream()
                .filter(c -> c.table().equals("content_blocks"))
                .findFirst()
                .orElseThrow();
        assertTrue(blockType.values().contains("LIST"));
        assertTrue(blockType.values().contains("FILE"));
    }

    @Test
    void readsValueSetOutOfPostgresConstraintDefinition() {
        var hibernateStyle = "CHECK (((block_type)::text = ANY ((ARRAY['SHORT_TEXT'::character varying, 'LONG_TEXT'::character varying])::text[])))";
        var ownStyle = "CHECK (((block_type)::text = ANY (ARRAY['LONG_TEXT'::text, 'SHORT_TEXT'::text])))";
        var singleValue = "CHECK (((media_type)::text = 'IMAGE'::text))";

        assertEquals(Set.of("SHORT_TEXT", "LONG_TEXT"), EnumCheckConstraintSynchronizer.literalsOf(hibernateStyle));
        assertEquals(Set.of("SHORT_TEXT", "LONG_TEXT"), EnumCheckConstraintSynchronizer.literalsOf(ownStyle));
        assertEquals(Set.of("IMAGE"), EnumCheckConstraintSynchronizer.literalsOf(singleValue));
    }

    @Test
    void columnPatternEscapesLikeWildcards() {
        assertEquals("%(block\\_type)::text%", EnumCheckConstraintSynchronizer.columnPattern("block_type"));
    }

    private static EnumColumn column(String table, String column, Class<? extends Enum<?>> enumType) {
        return new EnumColumn(table, column, enumNames(enumType));
    }

    private static Set<String> enumNames(Class<? extends Enum<?>> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
