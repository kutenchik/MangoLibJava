// src/main/java/com/example/demo/model/Comment.java

package com.example.crud.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    private String titleName;
    private Integer nomerGlavi;
    private Integer userId;

    @Column(columnDefinition = "TEXT")
    private String commentText;

    private String commentDate;
}
