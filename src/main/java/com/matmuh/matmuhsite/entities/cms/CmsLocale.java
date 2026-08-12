package com.matmuh.matmuhsite.entities.cms;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cms_locales")
public class CmsLocale {

    @Id
    @Column(name = "code", length = 16, nullable = false)
    private String code;

    @Column(name = "position", nullable = false)
    private int position;
}
