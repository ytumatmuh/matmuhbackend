package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@DiscriminatorValue("FILE")
public class File extends Media{
}
