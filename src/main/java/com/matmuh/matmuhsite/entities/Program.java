package com.matmuh.matmuhsite.entities;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public enum Program {
    UNDERGRADUATE,
    MASTERS_THESIS,
    MASTERS_NON_THESIS,
    DOCTORATE;

    // Öğrenim düzeyi tezli ile tezsizi ayırmaz; yüksek lisans tezli varsayılır ve tezsiz programın
    // dersleri Bologna verisiyle ayrıca işaretlenir (Kaan'ın planı, 25 Eylül).
    public static Set<Program> fromDegreeLevels(Collection<DegreeLevel> levels) {
        var programs = new LinkedHashSet<Program>();
        if (levels == null) {
            return programs;
        }
        for (var level : levels) {
            programs.add(switch (level) {
                case UNDERGRADUATE -> UNDERGRADUATE;
                case MASTERS -> MASTERS_THESIS;
                case DOCTORATE -> DOCTORATE;
            });
        }
        return programs;
    }
}
