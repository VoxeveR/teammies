package com.voxever.teammies.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "questions",
        indexes = {@Index(columnList = "quiz_id, position")})
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // belongs to quiz
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(columnDefinition = "text", nullable = false)
    private String text;

    @Column(name = "question_type", length = 20, nullable = false)
    private String questionType;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false)
    private Integer position;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AnswerOption> answerOptions;

//    @OneToMany(mappedBy = "question")
//    private Set<Round> rounds;
}
