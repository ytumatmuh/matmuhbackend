package com.matmuh.matmuhsite.core.dtos.enrollment.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EnrollRequestDto {

    @NotNull(message = "{enrollment.offering.not.null}")
    private UUID lectureOfferingId;
}
