package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ElectiveGroupService;
import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.request.CreateElectiveGroupRequestDto;
import com.matmuh.matmuhsite.core.dtos.lecture.request.CreateLectureRequestDto;
import com.matmuh.matmuhsite.core.exceptions.BusinessRuleException;
import com.matmuh.matmuhsite.core.exceptions.ResourceAlreadyExistsException;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.entities.Program;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ProgramsTest {

    @Autowired
    private ElectiveGroupService electiveGroupService;

    @Autowired
    private LectureService lectureService;

    @Autowired
    private LectureDao lectureDao;

    // Bologna (25 Eylül): tezsiz YL'nin SEC0001'i tezli YL ve doktoranın SEC0001'inden farklı bir slot.
    @Test
    void aSlotCodeRepeatsOnlyAcrossProgramsThatDoNotOverlap() {
        var shared = electiveGroupService.createElectiveGroup(group("ZSEC0001", Program.MASTERS_THESIS, Program.DOCTORATE));
        var nonThesis = electiveGroupService.createElectiveGroup(group("ZSEC0001", Program.MASTERS_NON_THESIS));

        assertThrows(ResourceAlreadyExistsException.class,
                () -> electiveGroupService.createElectiveGroup(group("ZSEC0001", Program.DOCTORATE)));
        assertThrows(BusinessRuleException.class, () -> electiveGroupService.getElectiveGroupByCode("ZSEC0001", null));
        assertEquals(nonThesis.getId(), electiveGroupService.getElectiveGroupByCode("ZSEC0001", Program.MASTERS_NON_THESIS).getId());
        assertEquals(shared.getId(), electiveGroupService.getElectiveGroupByCode("zsec0001", Program.DOCTORATE).getId());
    }

    @Test
    void aNewLectureWithoutProgramsTakesThemFromItsDegreeLevelsAndIsFoundByProgram() {
        var request = new CreateLectureRequestDto();
        request.setCode("ZZP5102");
        request.setName("Program süzgeci");
        var created = lectureService.createLecture(request);

        assertEquals(Set.of(Program.MASTERS_THESIS, Program.DOCTORATE), created.getPrograms());
        assertEquals(1, lectureDao.search(null, null, null, Program.DOCTORATE, null, null, null, "ZZP5102",
                PageRequest.of(0, 5)).getTotalElements());
        assertEquals(0, lectureDao.search(null, null, null, Program.MASTERS_NON_THESIS, null, null, null, "ZZP5102",
                PageRequest.of(0, 5)).getTotalElements());
    }

    private CreateElectiveGroupRequestDto group(String code, Program... programs) {
        var request = new CreateElectiveGroupRequestDto();
        request.setCode(code);
        request.setName("Seçmeli " + code);
        request.setPrograms(Set.of(programs));
        return request;
    }
}
