-- Mufredattaki secmeli slotlarini (MES2-3G, USS-2G gibi) ve her slotun ders havuzunu doldurur.
-- Tablolari Hibernate (ddl-auto=update) kendisi olusturur; bu script sadece veriyi yukler.
-- Deploy SONRASI bir kez calistirilmali. Dersler daha once yuklenmis olmali;
-- henuz kayitli olmayan ders kodlari sessizce atlanir, script tekrar calistirilabilir.
--
-- Slot kodlari ve ders havuzlari YTU Bologna lisans mufredatindan birebir alinmistir.
-- SEC-YL ve SEC-DR bizim kendi kodlarimiz: Bologna lisansustu programlarda ayni
-- SEC0001-SEC0007 kodlarini iki programda da kullandigi icin ayirt edilemiyorlar.
-- Bu iki grupta yariyil bos birakildi (secmeliler 1. ve 2. yariyila dagiliyor) ve
-- AKTS bos birakildi (Bologna 7.5 diyor, alan tam sayi oldugu icin yuvarlamak istemedik).

BEGIN;

CREATE TABLE IF NOT EXISTS elective_groups (
    id UUID PRIMARY KEY,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    code VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255),
    about VARCHAR(255),
    term INTEGER,
    semester VARCHAR(255),
    weekly_hours INTEGER,
    local_credit INTEGER,
    ects INTEGER,
    selection_count INTEGER NOT NULL DEFAULT 1,
    version INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT now(),
    updated_at TIMESTAMP(6)
);

CREATE TABLE IF NOT EXISTS elective_group_degree_levels (
    elective_group_id UUID NOT NULL REFERENCES elective_groups(id),
    degree_level VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS elective_group_options (
    elective_group_id UUID NOT NULL REFERENCES elective_groups(id),
    lecture_id UUID NOT NULL REFERENCES lectures(id)
);

INSERT INTO elective_groups (id, is_deleted, created_at, code, name, slug, term, semester, weekly_hours, local_credit, ects, selection_count, version)
SELECT gen_random_uuid(), FALSE, now(), slot.code, slot.name, slot.slug, slot.term, slot.semester, slot.weekly_hours, slot.local_credit, slot.ects, slot.selection_count, 0
FROM (VALUES
    ('USS-2G', 'Üniversite Sosyal Seçmeli', 'uss-2g', 3, 'FALL', 3, 3, 5, 1),
    ('MES1-2B', 'Mesleki Seçmeli 1', 'mes1-2b', 4, 'SPRING', 3, 3, 5, 1),
    ('MES2-3G', 'Mesleki Seçmeli 2', 'mes2-3g', 5, 'FALL', 3, 3, 5, 1),
    ('MES3-3G', 'Mesleki Seçmeli 3', 'mes3-3g', 5, 'FALL', 3, 3, 5, 1),
    ('MES4-3G', 'Mesleki Seçmeli 4', 'mes4-3g', 5, 'FALL', 3, 3, 5, 1),
    ('MES5-3B', 'Mesleki Seçmeli 5', 'mes5-3b', 6, 'SPRING', 3, 3, 5, 1),
    ('MES6-3B', 'Mesleki Seçmeli 6', 'mes6-3b', 6, 'SPRING', 3, 3, 5, 1),
    ('UMS-4G', 'Üniversite Mesleki Seçmeli', 'ums-4g', 7, 'FALL', 3, 3, 5, 1),
    ('MES7-4G', 'Mesleki Seçmeli 7', 'mes7-4g', 7, 'FALL', 3, 3, 5, 1),
    ('MES8-4G', 'Mesleki Seçmeli 8', 'mes8-4g', 7, 'FALL', 3, 3, 5, 1),
    ('MES9-4G', 'Mesleki Seçmeli 9', 'mes9-4g', 7, 'FALL', 3, 3, 5, 1),
    ('MES10-4B', 'Mesleki Seçmeli 10', 'mes10-4b', 8, 'SPRING', 3, 3, 5, 1),
    ('MES11-4B', 'Mesleki Seçmeli 11', 'mes11-4b', 8, 'SPRING', 3, 3, 5, 1),
    ('MES12-4B', 'Mesleki Seçmeli 12', 'mes12-4b', 8, 'SPRING', 3, 3, 5, 1),
    ('SEC-YL', 'Yüksek Lisans Seçmeli', 'sec-yl', NULL, NULL, 3, 3, NULL, 7),
    ('SEC-DR', 'Doktora Seçmeli', 'sec-dr', NULL, NULL, 3, 3, NULL, 7)
) AS slot(code, name, slug, term, semester, weekly_hours, local_credit, ects, selection_count)
WHERE NOT EXISTS (
    SELECT 1 FROM elective_groups existing WHERE LOWER(existing.code) = LOWER(slot.code)
);

INSERT INTO elective_group_options (elective_group_id, lecture_id)
SELECT g.id, l.id
FROM (VALUES
    ('USS-2G', ARRAY['ITB3390','ITB2030','ITB4100','ILT1621','SBP2082','SYP2192','SYP3241','MIM1422','MIM2421','MIM1412','HRT2941','ITB2020','INS2462','MDB4011','MDB4021','MAK2100','ITB3250','ITB3360','MTP4760','GIM4101','TDB4011','TDB4031','TDB4041','ITB1680','TDB4051','DNS1220','DNS1240','GIM4151','ITB4040','TDB4061','ISL1150','KIM1052','CEV3333','BED1013','MDB1016','MDB1004','MKT2201','GRA2024','EUT2022','MDB1001','MDB1003','MDB1007','MDB1009','MDB1011','MDB1013','MDB1015','MDB1017','MDB1019','SBP2020','INS4910','MDB1010','CEV3334','MAT4279','MDB1002','SBO1180','OKL2350','RPD2000','SBO1120','TRO2730','BTO1910','FBO2260','IMO2150','ING2350','MDB1008','SNF2210','SBO1190','SBO1230','SBO1240','MTM4595','TRO2281','EGT1022','EGT4041','EGT2031','MTM3611','BED3011','BED3041','BED4031','BED3051','BED3012','BED4022','BED3042','BED4032','TRO2261','SNF2112','ISL2560','ISL2710','ISL2630','ISL2901','ISL2760','SBP2031','ITB2040','ITB3330','ITB2090','ITB3150','ITB3020','ITB3040','ITB3270','ILT1611','ITB3260','ITB3420','ITB3210','ITB3220','ITB3130','ITB2080','ISL2170','ITB3010','ITB3550','ITB3560','ITB3570']),
    ('MES1-2B', ARRAY['MTM2552','MTM2562','MTM2572','MTM2632','MTM2622','MTM2602','MTM2612']),
    ('MES2-3G', ARRAY['MTM3521','MTM3531','MTM3541','MTM3551','MTM3661','MTM3721']),
    ('MES3-3G', ARRAY['MTM3561','MTM3571','MTM3581','MTM3591','MTM3671']),
    ('MES4-3G', ARRAY['MTM3601','MTM3621','MTM3631','MTM3641','MTM3681']),
    ('MES5-3B', ARRAY['MTM3542','MTM3552','MTM3562','MTM3572','MTM3632','MTM3662']),
    ('MES6-3B', ARRAY['MTM3582','MTM3592','MTM3602','MTM3612','MTM3642','MTM3652']),
    ('UMS-4G', ARRAY['YZM4015','IKT3610','EHM4370','EHM4220','EHM4270','GIM4322','GIM4392','KIM3557','KMM3561','ISL3660','CEV4501','MAK4482','CEV4111','HRT4332','MIM4341','ELM4010','SBP1300','SBP4310','KVK4412','BME4142','IKT3820','ISL3940','INS3841','BLM4400','BLM1012','BME4110','TDE3557','MTM4711','ELM4071','KOM4760','KOM4770','GMI3850','GMI3860','IST3557','MAT3557','FIZ3557','MBG3557','MEM4131','KVK4422','GDM4309','MKT4403','END4393','BYM4721','SBU3001']),
    ('MES7-4G', ARRAY['MTM4531','MTM4541','MTM4561','MTM4691','MTM4571']),
    ('MES8-4G', ARRAY['MTM4601','MTM4661','MTM4581','MTM4591','MTM4701']),
    ('MES9-4G', ARRAY['MTM4621','MTM4631','MTM4641','MTM4651','MTM4671','MTM4681']),
    ('MES10-4B', ARRAY['MTM4522','MTM4542','MTM4552','MTM4562','MTM4702']),
    ('MES11-4B', ARRAY['MTM4572','MTM4582','MTM4592','MTM4602','MTM4612','MTM4682']),
    ('MES12-4B', ARRAY['MTM4622','MTM4712','MTM4642','MTM4652','MTM4692']),
    ('SEC-YL', ARRAY['MTM5101','MTM5105','MTM5107','MTM5109','MTM5111','MTM5113','MTM5115','MTM5117','MTM5118','MTM5119','MTM5120','MTM5121','MTM5122','MTM5123','MTM5125','MTM5129','MTM5131','MTM5133','MTM5134','MTM5135','MTM5136','MTM5137','MTM5140','MTM5141','MTM5200','MTM5202','MTM5204','MTM6101','MTM6102','MTM6105','MTM6106','MTM6107','MTM6109','MTM6110','MTM6112','MTM6200','MTM6201','MTM6202','MTM6203']),
    ('SEC-DR', ARRAY['MTM5101','MTM5105','MTM5107','MTM5109','MTM5111','MTM5113','MTM5115','MTM5117','MTM5118','MTM5119','MTM5120','MTM5121','MTM5122','MTM5123','MTM5125','MTM5129','MTM5131','MTM5133','MTM5134','MTM5135','MTM5136','MTM5137','MTM5140','MTM5141','MTM5200','MTM5202','MTM5204','MTM6101','MTM6102','MTM6105','MTM6106','MTM6107','MTM6109','MTM6110','MTM6112','MTM6200','MTM6201','MTM6202','MTM6203'])
) AS pool(code, lecture_codes)
JOIN elective_groups g ON LOWER(g.code) = LOWER(pool.code)
JOIN lectures l ON UPPER(l.code) = ANY(ARRAY(SELECT UPPER(x) FROM unnest(pool.lecture_codes) AS x))
WHERE NOT EXISTS (
    SELECT 1 FROM elective_group_options existing
    WHERE existing.elective_group_id = g.id AND existing.lecture_id = l.id
);

INSERT INTO elective_group_degree_levels (elective_group_id, degree_level)
SELECT DISTINCT o.elective_group_id, d.degree_level
FROM elective_group_options o
JOIN lecture_degree_levels d ON d.lecture_id = o.lecture_id
WHERE NOT EXISTS (
    SELECT 1 FROM elective_group_degree_levels existing
    WHERE existing.elective_group_id = o.elective_group_id
      AND existing.degree_level = d.degree_level
);

COMMIT;

-- Kontrol: her slotta kac ders eslesti
--   SELECT g.code, g.name, g.term, count(o.lecture_id) AS ders_sayisi
--     FROM elective_groups g LEFT JOIN elective_group_options o ON o.elective_group_id = g.id
--     GROUP BY g.code, g.name, g.term ORDER BY g.term, g.code;
