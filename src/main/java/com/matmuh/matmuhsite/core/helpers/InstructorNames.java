package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.entities.LectureOffering;

public final class InstructorNames {

    private InstructorNames() {}

    public static String of(LectureOffering offering) {
        if (offering == null) {
            return null;
        }

        var staff = offering.getStaff();
        if (staff != null) {
            return (staff.getFirstName() + " " + staff.getLastName()).trim();
        }

        return offering.getInstructorRawName();
    }
}
