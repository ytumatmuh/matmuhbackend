-- Silinmiş (is_deleted = true) açılışların geride bıraktığı ders saati, öğrenci kaydı ve
-- sınav tarihi satırları ile açılışa bağlı kalmış notlar. Açılış silme yolu
-- (LectureOfferingManager.deleteOffering, LectureManager.deleteLecture → OfferingDependents)
-- bunları artık açılışla birlikte düşürüyor; eski kayıtlar için aynı temizlik uygulamanın
-- açılışında kendiliğinden koşar (core/utilities/schema/OfferingOrphanCleanup, ApplicationRunner
-- @Order(4)). Bu dosya belge ve elle çalıştırma yedeği olarak duruyor; deploy tek başına yeterli.
-- Tekrar çalıştırılabilir.
--
-- Neden sert silme: saat, kayıt ve sınav tarihinin açılıştan bağımsız kimliği yok (CLAUDE.md §4);
-- saat ve kayıt join'li sorgular yüzünden zaten görünmüyordu, sınav tarihi ise genel takvimde
-- hayalet olarak kalıyordu (calendar_events.lecture_offering_id boş olabildiği için sol join).
-- Not silinmez (öğrencinin yüklemesi): yalnız açılış bağı çözülür ve not derse genel not olur;
-- bağ kalırsa silinmiş açılışa giden proxy GET /api/lecture-notes/{id} isteğini 500 yapıyordu.

DELETE FROM schedule_slots  WHERE lecture_offering_id IN (SELECT id FROM lecture_offerings WHERE is_deleted);
DELETE FROM enrollments     WHERE lecture_offering_id IN (SELECT id FROM lecture_offerings WHERE is_deleted);
DELETE FROM calendar_events WHERE lecture_offering_id IN (SELECT id FROM lecture_offerings WHERE is_deleted);
UPDATE lecture_notes SET lecture_offering_id = NULL
WHERE lecture_offering_id IN (SELECT id FROM lecture_offerings WHERE is_deleted);
