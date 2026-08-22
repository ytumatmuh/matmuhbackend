package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tek seferlik takvim kaydı: sınav, akademik takvim maddesi, tatil, etkinlik.
 *
 * Sınav tarihi neden burada, `ExamStatistic`'te değil: istatistik kaydı katılım sayılarını
 * zorunlu tutuyor ve sınav tarihi girilirken o sayılar henüz yok. Burası "ne zaman ve
 * nerede", `ExamStatistic` "nasıl geçti" — ikisi (offering, examType) çiftiyle eşleşiyor.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "calendar_events",
        indexes = {
                @Index(name = "ix_calendar_event_starts", columnList = "starts_at"),
                @Index(name = "ix_calendar_event_offering", columnList = "lecture_offering_id")
        }
)
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private CalendarEventType type;

    // Sınavlarda dolu; mevcut ExamType yeniden kullanılıyor ki sınav sözlüğü tek kalsın.
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", length = 30)
    private ExamType examType;

    // Sınavda dolu, tatilde boş.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_offering_id")
    private LectureOffering lectureOffering;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    // 23 Nisan gibi kayıtlarda saat anlamsız; istemci saat göstermesin diye ayrı bayrak.
    @Column(name = "all_day", nullable = false)
    @Builder.Default
    private boolean allDay = false;

    @Column(name = "classroom", length = 60)
    private String classroom;
}
