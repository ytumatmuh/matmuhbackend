package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CalendarAdminService;
import com.matmuh.matmuhsite.business.abstracts.ElectiveGroupService;
import com.matmuh.matmuhsite.business.abstracts.LectureOfferingService;
import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.business.abstracts.StaffService;
import com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.mappers.LectureMapper;
import com.matmuh.matmuhsite.core.mappers.StaffMapper;
import com.matmuh.matmuhsite.dataAccess.abstracts.AcademicTermDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.ElectiveGroupDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureOfferingDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.StaffDao;
import com.matmuh.matmuhsite.entities.AcademicTerm;
import com.matmuh.matmuhsite.entities.ElectiveGroup;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.LectureOffering;
import com.matmuh.matmuhsite.entities.Semester;
import com.matmuh.matmuhsite.entities.Staff;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// SDK her koleksiyonda sil düğmesi çiziyor: DELETE /cms/collections/{key}/{slug} sağlayıcı
// tabanlı koleksiyonda da satırı gerçekten silmeli. Silme kendi REST servisinden geçer ki
// bağımlı temizliği (OfferingDependents) tek yerde kalsın.
class CmsCollectionProviderDeleteTest {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private static jakarta.validation.Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void lectureDeleteResolvesTheSlugAndCallsTheService() {
        var service = mock(LectureService.class);
        var dao = mock(LectureDao.class);
        var provider = new LectureCollectionProvider(service, dao, Mappers.getMapper(LectureMapper.class),
                MAPPER, validator());

        var lecture = Lecture.builder().id(UUID.randomUUID()).code("MTM1501").slug("mtm1501").version(4).build();
        when(dao.findBySlug("mtm1501")).thenReturn(Optional.of(lecture));

        provider.delete("mtm1501", 4);

        verify(service).deleteLecture(lecture.getId());
    }

    @Test
    void lectureDeleteRefusesAStaleVersionAndAnUnknownSlug() {
        var service = mock(LectureService.class);
        var dao = mock(LectureDao.class);
        var provider = new LectureCollectionProvider(service, dao, Mappers.getMapper(LectureMapper.class),
                MAPPER, validator());

        var lecture = Lecture.builder().id(UUID.randomUUID()).code("MTM1501").slug("mtm1501").version(4).build();
        when(dao.findBySlug("mtm1501")).thenReturn(Optional.of(lecture));
        when(dao.findBySlug("yok")).thenReturn(Optional.empty());

        assertThrows(ConcurrencyConflictException.class, () -> provider.delete("mtm1501", 3));
        assertThrows(ResourceNotFoundException.class, () -> provider.delete("yok", 1));
        verify(service, never()).deleteLecture(any());
    }

    @Test
    void staffDeleteResolvesTheSlugAndCallsTheService() {
        var service = mock(StaffService.class);
        var dao = mock(StaffDao.class);
        var provider = new StaffCollectionProvider(service, dao, Mappers.getMapper(StaffMapper.class),
                MAPPER, validator());

        var staff = Staff.builder().id(UUID.randomUUID()).firstName("Ayşe").lastName("Yılmaz")
                .slug("ayse-yilmaz").version(2).build();
        when(dao.findBySlug("ayse-yilmaz")).thenReturn(Optional.of(staff));

        provider.delete("ayse-yilmaz", 2);

        verify(service).deleteStaff(staff.getId());
    }

    @Test
    void staffDeleteRefusesAStaleVersionAndAnUnknownSlug() {
        var service = mock(StaffService.class);
        var dao = mock(StaffDao.class);
        var provider = new StaffCollectionProvider(service, dao, Mappers.getMapper(StaffMapper.class),
                MAPPER, validator());

        var staff = Staff.builder().id(UUID.randomUUID()).slug("ayse-yilmaz").version(2).build();
        when(dao.findBySlug("ayse-yilmaz")).thenReturn(Optional.of(staff));
        when(dao.findBySlug("yok")).thenReturn(Optional.empty());

        assertThrows(ConcurrencyConflictException.class, () -> provider.delete("ayse-yilmaz", 7));
        assertThrows(ResourceNotFoundException.class, () -> provider.delete("yok", 1));
        verify(service, never()).deleteStaff(any());
    }

    @Test
    void electiveGroupDeleteResolvesTheSlugAndCallsTheService() {
        var service = mock(ElectiveGroupService.class);
        var dao = mock(ElectiveGroupDao.class);
        var provider = new ElectiveGroupCollectionProvider(service, dao, MAPPER, validator());

        var group = ElectiveGroup.builder().id(UUID.randomUUID()).code("MES2-3G").slug("mes2-3g").version(1).build();
        when(dao.findBySlug("mes2-3g")).thenReturn(Optional.of(group));

        provider.delete("mes2-3g", 1);

        verify(service).deleteElectiveGroup(group.getId());
    }

    @Test
    void electiveGroupDeleteRefusesAStaleVersionAndAnUnknownSlug() {
        var service = mock(ElectiveGroupService.class);
        var dao = mock(ElectiveGroupDao.class);
        var provider = new ElectiveGroupCollectionProvider(service, dao, MAPPER, validator());

        var group = ElectiveGroup.builder().id(UUID.randomUUID()).slug("mes2-3g").version(1).build();
        when(dao.findBySlug("mes2-3g")).thenReturn(Optional.of(group));
        when(dao.findBySlug("yok")).thenReturn(Optional.empty());

        assertThrows(ConcurrencyConflictException.class, () -> provider.delete("mes2-3g", 9));
        assertThrows(ResourceNotFoundException.class, () -> provider.delete("yok", 1));
        verify(service, never()).deleteElectiveGroup(any());
    }

    // LectureOffering'de @Version yok; upsert sürümü yok saydığı için silme de saymaz.
    @Test
    void offeringDeleteResolvesTheCompositeSlugAndIgnoresTheVersion() {
        var service = mock(LectureOfferingService.class);
        var offeringDao = mock(LectureOfferingDao.class);
        var lectureDao = mock(LectureDao.class);
        var provider = new LectureOfferingCollectionProvider(service, mock(CalendarAdminService.class),
                offeringDao, lectureDao, mock(StaffDao.class), MAPPER, mock(MessageResolver.class));

        var lecture = Lecture.builder().id(UUID.randomUUID()).code("MTM1501").slug("mtm1501").build();
        var offering = LectureOffering.builder().id(UUID.randomUUID()).lecture(lecture)
                .academicYear("2026-2027").semester(Semester.FALL).groupNumber(1).build();
        when(lectureDao.findByCodeIgnoreCase("mtm1501")).thenReturn(Optional.of(lecture));
        when(offeringDao.findByLectureIdAndAcademicYearAndSemesterAndGroupNumber(
                lecture.getId(), "2026-2027", Semester.FALL, 1)).thenReturn(Optional.of(offering));

        provider.delete("mtm1501-2026-2027-fall-1", 99);

        verify(service).deleteOffering(offering.getId());
    }

    @Test
    void offeringDeleteRefusesAnUnknownSlug() {
        var service = mock(LectureOfferingService.class);
        var lectureDao = mock(LectureDao.class);
        var provider = new LectureOfferingCollectionProvider(service, mock(CalendarAdminService.class),
                mock(LectureOfferingDao.class), lectureDao, mock(StaffDao.class), MAPPER, mock(MessageResolver.class));

        when(lectureDao.findByCodeIgnoreCase(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> provider.delete("yok-2026-2027-fall-1", 1));
        verify(service, never()).deleteOffering(any());
    }

    // AcademicTerm'de @Version yok; upsert sürümü yok saydığı için silme de saymaz.
    @Test
    void termDeleteResolvesTheNaturalKeySlugAndIgnoresTheVersion() {
        var service = mock(CalendarAdminService.class);
        var dao = mock(AcademicTermDao.class);
        var provider = new AcademicTermCollectionProvider(service, dao, MAPPER, validator());

        var term = AcademicTerm.builder().id(UUID.randomUUID()).academicYear("2026-2027").semester(Semester.FALL)
                .startDate(LocalDate.of(2026, 9, 21)).endDate(LocalDate.of(2027, 1, 16)).build();
        when(dao.findAllByOrderByStartDateDesc()).thenReturn(List.of(term));

        provider.delete("2026-2027-fall", 42);

        verify(service).deleteTerm(term.getId());
    }

    @Test
    void termDeleteRefusesAnUnknownSlug() {
        var service = mock(CalendarAdminService.class);
        var dao = mock(AcademicTermDao.class);
        var provider = new AcademicTermCollectionProvider(service, dao, MAPPER, validator());

        when(dao.findAllByOrderByStartDateDesc()).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> provider.delete("2026-2027-fall", 0));
        verify(service, never()).deleteTerm(any());
    }
}
