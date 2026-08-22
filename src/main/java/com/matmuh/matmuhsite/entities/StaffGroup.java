package com.matmuh.matmuhsite.entities;

public enum StaffGroup {
    MANAGEMENT,
    ACADEMIC,
    TEACHING_AND_RESEARCH,
    ADMINISTRATIVE;

    public boolean canTeach() {
        return this != ADMINISTRATIVE;
    }
}
