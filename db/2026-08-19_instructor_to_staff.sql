-- Instructor -> Staff rename. Yeni imaj deploy EDILMEDEN ONCE calistirilmali.
-- ddl-auto=update tablo/kolon rename yapmaz: bu SQL calismazsa Hibernate bos bir "staff"
-- tablosu olusturur ve mevcut akademisyen kayitlari eski "instructors" tablosunda oksuz kalir.

BEGIN;

ALTER TABLE instructors RENAME TO staff;
ALTER TABLE lecture_offerings RENAME COLUMN instructor_id TO staff_id;

COMMIT;

-- Opsiyonel: LectureInstructor entity'si kaldirildi (hicbir yerden kullanilmiyordu).
-- Tabloyu da temizlemek istersen, icindeki veriyi kontrol ettikten sonra:
-- DROP TABLE IF EXISTS lecture_instructors;
