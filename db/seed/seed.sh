#!/bin/bash
# Örnek veri yükleyici: akademisyen -> ders -> dönem kaydı -> sınav istatistiği -> harf sonucu
#
# Kullanım:
#   TOKEN=<admin access token> bash db/seed/seed.sh
#   JWT=<jwt cookie degeri>    bash db/seed/seed.sh
#   BASE=http://localhost:9001/api TOKEN=... bash db/seed/seed.sh
#
# UYARI: Tekrar çalıştırırsan dersler/akademisyenler yeniden eklenmeye çalışılır.
#        Ders kodu benzersiz olduğu için ikinci çalıştırma 409 verir, bu beklenen davranış.

set -u
BASE=${BASE:-https://matmuh.yusufacmaci.com/api}

if [ -n "${TOKEN:-}" ]; then AUTH="Authorization: Bearer $TOKEN"
elif [ -n "${JWT:-}" ]; then AUTH="Cookie: jwt=$JWT"
else echo "HATA: TOKEN veya JWT ver."; exit 1; fi

ok=0; fail=0

# post <yol> <json> -> olusturulan kaydin id'sini stdout'a yazar
post() {
  local path="$1" body="$2" label="$3"
  local code
  code=$(curl -s -o /tmp/seed.json -w '%{http_code}' -X POST \
         -H "$AUTH" -H 'Content-Type: application/json' -d "$body" "$BASE$path")
  if [ "$code" = "200" ] || [ "$code" = "201" ]; then
    ok=$((ok+1))
    printf '  ok   %-46s %s\n' "$label" "$code" >&2
    python3 -c "import json;print(json.load(open('/tmp/seed.json'))['data']['id'])" 2>/dev/null
  else
    fail=$((fail+1))
    printf '  FAIL %-46s %s  %s\n' "$label" "$code" "$(head -c 160 /tmp/seed.json)" >&2
    echo ""
  fi
}

put() {
  local path="$1" body="$2" label="$3"
  local code
  code=$(curl -s -o /tmp/seed.json -w '%{http_code}' -X PUT \
         -H "$AUTH" -H 'Content-Type: application/json' -d "$body" "$BASE$path")
  if [ "$code" = "200" ] || [ "$code" = "201" ]; then
    ok=$((ok+1)); printf '  ok   %-46s %s\n' "$label" "$code"
  else
    fail=$((fail+1)); printf '  FAIL %-46s %s  %s\n' "$label" "$code" "$(head -c 160 /tmp/seed.json)"
  fi
}

echo "Hedef: $BASE"
echo
echo "=========== 1. AKADEMİSYENLER ==========="

ins() { post /instructors "$1" "$2"; }

I1=$(ins '{"firstName":"Ahmet","lastName":"Yılmaz","academicTitle":"Prof. Dr.","email":"ahmet.yilmaz@yildiz.edu.tr","office":"A-301","avesisLink":"https://avesis.yildiz.edu.tr/ahmet.yilmaz"}' "Prof. Dr. Ahmet Yılmaz")
I2=$(ins '{"firstName":"Elif","lastName":"Demir","academicTitle":"Prof. Dr.","email":"elif.demir@yildiz.edu.tr","office":"A-302","avesisLink":"https://avesis.yildiz.edu.tr/elif.demir"}' "Prof. Dr. Elif Demir")
I3=$(ins '{"firstName":"Mehmet","lastName":"Kaya","academicTitle":"Doç. Dr.","email":"mehmet.kaya@yildiz.edu.tr","office":"A-305","avesisLink":"https://avesis.yildiz.edu.tr/mehmet.kaya"}' "Doç. Dr. Mehmet Kaya")
I4=$(ins '{"firstName":"Zeynep","lastName":"Arslan","academicTitle":"Doç. Dr.","email":"zeynep.arslan@yildiz.edu.tr","office":"A-307","avesisLink":"https://avesis.yildiz.edu.tr/zeynep.arslan"}' "Doç. Dr. Zeynep Arslan")
I5=$(ins '{"firstName":"Burak","lastName":"Şahin","academicTitle":"Dr. Öğr. Üyesi","email":"burak.sahin@yildiz.edu.tr","office":"B-104","avesisLink":"https://avesis.yildiz.edu.tr/burak.sahin"}' "Dr. Öğr. Üyesi Burak Şahin")
I6=$(ins '{"firstName":"Selin","lastName":"Öztürk","academicTitle":"Dr. Öğr. Üyesi","email":"selin.ozturk@yildiz.edu.tr","office":"B-106","avesisLink":"https://avesis.yildiz.edu.tr/selin.ozturk"}' "Dr. Öğr. Üyesi Selin Öztürk")
I7=$(ins '{"firstName":"Emre","lastName":"Çelik","academicTitle":"Arş. Gör. Dr.","email":"emre.celik@yildiz.edu.tr","office":"B-210","avesisLink":"https://avesis.yildiz.edu.tr/emre.celik"}' "Arş. Gör. Dr. Emre Çelik")
I8=$(ins '{"firstName":"Merve","lastName":"Aydın","academicTitle":"Arş. Gör.","email":"merve.aydin@yildiz.edu.tr","office":"B-212","avesisLink":"https://avesis.yildiz.edu.tr/merve.aydin"}' "Arş. Gör. Merve Aydın")

echo
echo "=========== 2. DERSLER ==========="

lec() { post /lectures "$1" "$2"; }

L1=$(lec '{"name":"Analiz 1","code":"MTM1501","languages":["TURKISH"],"term":1,"semester":"FALL","weeklyHours":5,"localCredit":5,"ects":7,"about":"Reel sayılar, diziler, limit ve süreklilik, türev ve uygulamaları, belirsiz integral.","gradingPolicy":"2 vize %30, final %40. Bağıl değerlendirme uygulanır.","resources":"Thomas Calculus; Balcı, Genel Matematik","bolognaLink":"https://bologna.yildiz.edu.tr/index.php?r=course/view&id=1&aid=3"}' "MTM1501 Analiz 1")
L2=$(lec '{"name":"Analiz 2","code":"MTM1502","languages":["TURKISH"],"term":2,"semester":"SPRING","weeklyHours":5,"localCredit":5,"ects":7,"about":"Belirli integral, seriler, çok değişkenli fonksiyonlar, kısmi türev, katlı integraller.","gradingPolicy":"2 vize %30, final %40.","resources":"Thomas Calculus"}' "MTM1502 Analiz 2")
L3=$(lec '{"name":"Lineer Cebir","code":"MTM1511","languages":["TURKISH"],"term":1,"semester":"FALL","weeklyHours":4,"localCredit":4,"ects":6,"about":"Matrisler, determinantlar, lineer denklem sistemleri, vektör uzayları, özdeğer ve özvektörler.","gradingPolicy":"Vize %40, final %60.","resources":"Anton, Elementary Linear Algebra"}' "MTM1511 Lineer Cebir")
L4=$(lec '{"name":"Diferansiyel Denklemler","code":"MTM2501","languages":["TURKISH"],"term":3,"semester":"FALL","weeklyHours":4,"localCredit":4,"ects":6,"about":"Birinci mertebeden denklemler, yüksek mertebeden lineer denklemler, Laplace dönüşümü, seri çözümler.","gradingPolicy":"2 vize %40, final %60."}' "MTM2501 Diferansiyel Denklemler")
L5=$(lec '{"name":"Olasılık ve İstatistik","code":"MTM2502","languages":["TURKISH"],"term":4,"semester":"SPRING","weeklyHours":4,"localCredit":4,"ects":6,"about":"Olasılık uzayları, rastgele değişkenler, dağılımlar, hipotez testleri, regresyon.","gradingPolicy":"Vize %40, final %60."}' "MTM2502 Olasılık ve İstatistik")
L6=$(lec '{"name":"Nümerik Analiz","code":"MTM2511","languages":["TURKISH"],"term":3,"semester":"FALL","weeklyHours":3,"localCredit":3,"ects":5,"about":"Hata analizi, kök bulma, interpolasyon, sayısal türev ve integral, lineer sistemlerin sayısal çözümü.","gradingPolicy":"Vize %30, proje %20, final %50.","resources":"Burden & Faires, Numerical Analysis"}' "MTM2511 Nümerik Analiz")
L7=$(lec '{"name":"Kompleks Fonksiyonlar Teorisi","code":"MTM3501","languages":["TURKISH"],"term":5,"semester":"FALL","weeklyHours":4,"localCredit":4,"ects":6,"about":"Kompleks sayılar, analitik fonksiyonlar, Cauchy teoremi, Laurent serileri, rezidü teoremi.","gradingPolicy":"Vize %40, final %60."}' "MTM3501 Kompleks Fonksiyonlar")
L8=$(lec '{"name":"Kısmi Diferansiyel Denklemler","code":"MTM3502","languages":["TURKISH"],"term":6,"semester":"SPRING","weeklyHours":4,"localCredit":4,"ects":6,"about":"Birinci ve ikinci mertebeden KDD, sınır değer problemleri, Fourier serileri, ısı ve dalga denklemi.","gradingPolicy":"Vize %40, final %60."}' "MTM3502 Kısmi Diferansiyel Denklemler")
L9=$(lec '{"name":"Optimizasyon","code":"MTM3511","languages":["TURKISH"],"term":5,"semester":"FALL","weeklyHours":3,"localCredit":3,"ects":5,"about":"Lineer programlama, simpleks yöntemi, dualite, kısıtsız ve kısıtlı optimizasyon, KKT koşulları.","gradingPolicy":"Vize %35, ödev %15, final %50."}' "MTM3511 Optimizasyon")
L10=$(lec '{"name":"Matematiksel Modelleme","code":"MTM4501","languages":["TURKISH"],"term":7,"semester":"FALL","weeklyHours":3,"localCredit":3,"ects":5,"about":"Modelleme süreci, boyut analizi, sürekli ve ayrık modeller, dinamik sistemler, vaka çalışmaları.","gradingPolicy":"Proje %50, final %50."}' "MTM4501 Matematiksel Modelleme")
L11=$(lec '{"name":"Finans Matematiği","code":"MTM4502","languages":["TURKISH"],"term":8,"semester":"SPRING","weeklyHours":3,"localCredit":3,"ects":5,"about":"Faiz teorisi, tahvil değerleme, portföy optimizasyonu, opsiyon fiyatlama, Black-Scholes modeli.","gradingPolicy":"Vize %40, final %60."}' "MTM4502 Finans Matematiği")
L12=$(lec '{"name":"Algoritmalar ve Programlama","code":"MTM2521","languages":["TURKISH"],"term":4,"semester":"SPRING","weeklyHours":4,"localCredit":3,"ects":6,"about":"Algoritma tasarımı, karmaşıklık analizi, veri yapıları, sıralama ve arama, özyineleme.","gradingPolicy":"Vize %30, laboratuvar %20, final %50."}' "MTM2521 Algoritmalar")

echo
echo "=========== 3. DÖNEM KAYITLARI ==========="

off() {
  # off <lectureId> <instructorId> <yil> <donem> <grup> <etiket>
  [ -z "$1" ] || [ -z "$2" ] && { echo ""; return; }
  post "/lectures/$1/offerings" \
       "{\"instructorId\":\"$2\",\"academicYear\":\"$3\",\"semester\":\"$4\",\"groupNumber\":$5}" "$6"
}

O1=$(off "$L1" "$I1" 2025-2026 FALL 1 "Analiz 1 / 2025-2026 Güz / Grup 1")
O2=$(off "$L1" "$I2" 2025-2026 FALL 2 "Analiz 1 / 2025-2026 Güz / Grup 2")
O3=$(off "$L1" "$I1" 2024-2025 FALL 1 "Analiz 1 / 2024-2025 Güz / Grup 1")
O4=$(off "$L2" "$I2" 2025-2026 SPRING 1 "Analiz 2 / 2025-2026 Bahar")
O5=$(off "$L3" "$I3" 2025-2026 FALL 1 "Lineer Cebir / 2025-2026 Güz")
O6=$(off "$L3" "$I4" 2024-2025 FALL 1 "Lineer Cebir / 2024-2025 Güz")
O7=$(off "$L4" "$I4" 2025-2026 FALL 1 "Diferansiyel Denklemler / 2025-2026 Güz")
O8=$(off "$L5" "$I5" 2025-2026 SPRING 1 "Olasılık ve İstatistik / 2025-2026 Bahar")
O9=$(off "$L6" "$I6" 2025-2026 FALL 1 "Nümerik Analiz / 2025-2026 Güz")
O10=$(off "$L7" "$I1" 2025-2026 FALL 1 "Kompleks Fonksiyonlar / 2025-2026 Güz")
O11=$(off "$L8" "$I3" 2025-2026 SPRING 1 "Kısmi Diferansiyel Denklemler / 2025-2026 Bahar")
O12=$(off "$L9" "$I5" 2025-2026 FALL 1 "Optimizasyon / 2025-2026 Güz")
O13=$(off "$L10" "$I7" 2025-2026 FALL 1 "Matematiksel Modelleme / 2025-2026 Güz")
O14=$(off "$L11" "$I8" 2025-2026 SPRING 1 "Finans Matematiği / 2025-2026 Bahar")
O15=$(off "$L12" "$I6" 2025-2026 SPRING 1 "Algoritmalar / 2025-2026 Bahar")

echo
echo "=========== 4. SINAV İSTATİSTİKLERİ ==========="

stat() {
  # stat <offeringId> <examType> <agirlik> <toplam> <katilan> <devamsiz> <ortalama> <tarih>
  [ -z "$1" ] && return
  put "/lecture-offerings/$1/exam-statistics/$2" \
      "{\"weightPercent\":$3,\"totalStudentCount\":$4,\"attendedStudentCount\":$5,\"failedByAbsenceCount\":$6,\"averageScore\":$7,\"announcedAt\":\"$8\"}" \
      "$2 -> $1"
}

stat "$O1" MIDTERM_1 30 84 79 3 52.40 "2025-11-14T16:00:00"
stat "$O1" MIDTERM_2 30 84 76 3 48.75 "2025-12-19T16:00:00"
stat "$O1" FINAL     40 84 74 5 55.10 "2026-01-16T16:00:00"
stat "$O1" RESIT     40 21 18 0 41.30 "2026-02-06T16:00:00"

stat "$O2" MIDTERM_1 30 78 74 2 57.80 "2025-11-14T16:00:00"
stat "$O2" MIDTERM_2 30 78 71 2 51.20 "2025-12-19T16:00:00"
stat "$O2" FINAL     40 78 70 4 58.60 "2026-01-16T16:00:00"

stat "$O5" MIDTERM_1 40 92 88 2 61.30 "2025-11-12T16:00:00"
stat "$O5" FINAL     60 92 85 5 63.90 "2026-01-14T16:00:00"

stat "$O9" MIDTERM_1 30 46 44 1 66.20 "2025-11-18T16:00:00"
stat "$O9" FINAL     50 46 43 2 68.40 "2026-01-20T16:00:00"

echo
echo "=========== 5. HARF SONUÇLARI ==========="

grades() {
  # grades <offeringId> <examPeriod> <json>
  [ -z "$1" ] && return
  put "/lecture-offerings/$1/grade-results/$2" "$3" "$2 harf sonucu -> $1"
}

grades "$O1" NORMAL '{
  "evaluationMethod":"RELATIVE","resultStatus":"Açıklandı","resultDate":"2026-01-23",
  "examCurriculumName":"Matematik Mühendisliği (Türkçe)","participantCount":84,
  "classAverage":52.65,"classAverageParticipantCount":79,"standardDeviation":14.20,
  "classLevel":"Orta","rangesChanged":false,
  "grades":[
    {"letterGrade":"AA","minScore":78.00,"maxScore":100.00,"studentCount":7},
    {"letterGrade":"BA","minScore":71.00,"maxScore":77.99,"studentCount":9},
    {"letterGrade":"BB","minScore":64.00,"maxScore":70.99,"studentCount":12},
    {"letterGrade":"CB","minScore":57.00,"maxScore":63.99,"studentCount":14},
    {"letterGrade":"CC","minScore":50.00,"maxScore":56.99,"studentCount":16},
    {"letterGrade":"DC","minScore":43.00,"maxScore":49.99,"studentCount":11},
    {"letterGrade":"DD","minScore":36.00,"maxScore":42.99,"studentCount":8},
    {"letterGrade":"FD","minScore":30.00,"maxScore":35.99,"studentCount":4},
    {"letterGrade":"FF","minScore":0.00,"maxScore":29.99,"studentCount":3}
  ]}'

grades "$O1" BUT '{
  "evaluationMethod":"ABSOLUTE","resultStatus":"Açıklandı","resultDate":"2026-02-13",
  "examCurriculumName":"Matematik Mühendisliği (Türkçe)","participantCount":21,
  "classAverage":41.30,"classAverageParticipantCount":18,"standardDeviation":11.75,
  "classLevel":"Düşük","rangesChanged":true,
  "grades":[
    {"letterGrade":"CC","minScore":60.00,"maxScore":100.00,"studentCount":3},
    {"letterGrade":"DC","minScore":50.00,"maxScore":59.99,"studentCount":5},
    {"letterGrade":"DD","minScore":40.00,"maxScore":49.99,"studentCount":6},
    {"letterGrade":"FF","minScore":0.00,"maxScore":39.99,"studentCount":7}
  ]}'

grades "$O5" NORMAL '{
  "evaluationMethod":"RELATIVE","resultStatus":"Açıklandı","resultDate":"2026-01-21",
  "examCurriculumName":"Matematik Mühendisliği (Türkçe)","participantCount":92,
  "classAverage":62.80,"classAverageParticipantCount":88,"standardDeviation":12.40,
  "classLevel":"İyi","rangesChanged":false,
  "grades":[
    {"letterGrade":"AA","minScore":82.00,"maxScore":100.00,"studentCount":12},
    {"letterGrade":"BA","minScore":75.00,"maxScore":81.99,"studentCount":14},
    {"letterGrade":"BB","minScore":68.00,"maxScore":74.99,"studentCount":18},
    {"letterGrade":"CB","minScore":62.00,"maxScore":67.99,"studentCount":17},
    {"letterGrade":"CC","minScore":55.00,"maxScore":61.99,"studentCount":13},
    {"letterGrade":"DC","minScore":48.00,"maxScore":54.99,"studentCount":8},
    {"letterGrade":"DD","minScore":40.00,"maxScore":47.99,"studentCount":6},
    {"letterGrade":"FF","minScore":0.00,"maxScore":39.99,"studentCount":4}
  ]}'

grades "$O9" NORMAL '{
  "evaluationMethod":"RELATIVE","resultStatus":"Açıklandı","resultDate":"2026-01-27",
  "examCurriculumName":"Matematik Mühendisliği (Türkçe)","participantCount":46,
  "classAverage":67.10,"classAverageParticipantCount":44,"standardDeviation":10.90,
  "classLevel":"İyi","rangesChanged":false,
  "grades":[
    {"letterGrade":"AA","minScore":85.00,"maxScore":100.00,"studentCount":8},
    {"letterGrade":"BA","minScore":78.00,"maxScore":84.99,"studentCount":7},
    {"letterGrade":"BB","minScore":71.00,"maxScore":77.99,"studentCount":9},
    {"letterGrade":"CB","minScore":64.00,"maxScore":70.99,"studentCount":8},
    {"letterGrade":"CC","minScore":57.00,"maxScore":63.99,"studentCount":6},
    {"letterGrade":"DC","minScore":50.00,"maxScore":56.99,"studentCount":4},
    {"letterGrade":"FF","minScore":0.00,"maxScore":49.99,"studentCount":4}
  ]}'

echo
echo "=========== SONUÇ ==========="
echo "  başarılı: $ok    başarısız: $fail"
echo
echo "Kontrol:"
echo "  curl $BASE/instructors?size=50"
echo "  curl $BASE/lectures?size=50"
echo "  curl $BASE/cms/collections/lectures"
echo "  curl $BASE/cms/collections/instructors"
