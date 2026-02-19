package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@DiscriminatorValue("IMAGE")
public class Image extends Media{

}
