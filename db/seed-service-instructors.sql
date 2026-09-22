-- Servis dersi öğretim elemanları (Kaan'ın OBS taramasından, 10 kişi).
-- "Atanmamış Eğitmen" bilerek yok: gerçek kişi değil, OBS'de boş gelen kayıtların yer tutucusu.
--
-- raw_name OBS'den gelen ham dizedir. first_name/last_name bölmesi son boşluğa göre
-- yapıldı; iki satırda (Beril Eker Gümüş, Damla Tezel Yalkut) çift soyadı ihtimali var
-- ve bölme yanlış olabilir. Ham ad saklandığı için ileride tek UPDATE ile düzeltilebilir.
--
-- Tekrar çalıştırılabilir: aynı slug varsa hiçbir şey yapmaz.

INSERT INTO instructors (id, first_name, last_name, academic_title, raw_name, slug, version)
VALUES
  (gen_random_uuid(), 'Ayben',       'KARASU',      'Prof.Dr.',     'Prof.Dr. Ayben KARASU',            'ayben-karasu',       0),
  (gen_random_uuid(), 'Sırrı Emrah', 'ÜÇER',        'Doç.Dr.',      'Doç.Dr. Sırrı Emrah ÜÇER',         'sirri-emrah-ucer',   0),
  (gen_random_uuid(), 'Şebnem',      'GELMEDİ',     'Öğr.Gör.Dr.',  'Öğr.Gör.Dr. Şebnem GELMEDİ',       'sebnem-gelmedi',     0),
  (gen_random_uuid(), 'GÜLSEMA',     'LÜYER',       'Öğr.Gör.',     'Öğr.Gör. GÜLSEMA LÜYER',           'gulsema-luyer',      0),
  (gen_random_uuid(), 'BERİL EKER',  'GÜMÜŞ',       'Öğr.Gör.',     'Öğr.Gör. BERİL EKER GÜMÜŞ',        'beril-eker-gumus',   0),
  (gen_random_uuid(), 'Hilal',       'TUFAN',       'Öğr.Gör.',     'Öğr.Gör. Hilal TUFAN',             'hilal-tufan',        0),
  (gen_random_uuid(), 'Arzuhan',     'KOCABAŞ',     'Öğr.Gör.',     'Öğr.Gör. Arzuhan KOCABAŞ',         'arzuhan-kocabas',    0),
  (gen_random_uuid(), 'DAMLA TEZEL', 'YALKUT',      'Öğr.Gör.',     'Öğr.Gör. DAMLA TEZEL YALKUT',      'damla-tezel-yalkut', 0),
  (gen_random_uuid(), 'Selçuk',      'GÜL',         'Öğr.Gör.',     'Öğr.Gör. Selçuk GÜL',              'selcuk-gul',         0),
  (gen_random_uuid(), 'Mahmut Zeki', 'ERBAY',       'Öğr.Gör.',     'Öğr.Gör. Mahmut Zeki ERBAY',       'mahmut-zeki-erbay',  0)
ON CONFLICT DO NOTHING;

-- Eklenenleri uuid'leriyle görmek için:
-- SELECT id, slug, raw_name FROM instructors WHERE raw_name IS NOT NULL ORDER BY last_name;
