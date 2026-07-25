package com.matmuh.matmuhsite.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import lombok.*;


@SQLDelete(sql = "UPDATE media SET is_deleted = true WHERE id = ?")
@Entity
@Getter
@Setter
@DiscriminatorValue("FILE")
public class File extends Media{
}
