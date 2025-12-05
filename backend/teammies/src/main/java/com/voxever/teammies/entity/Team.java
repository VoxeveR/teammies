package com.voxever.teammies.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Set;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String name;

    @Column(nullable=false, unique=true)
    private String joinCode;

    @ManyToOne
    @JoinColumn(name="league_id", nullable=false)
    private League league;

    @OneToMany(mappedBy = "team")
    private Set<LeagueStanding> leagueStandings;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;



//    @OneToMany(mappedBy = "team")
//    private Set<SessionTeam> sessionTeams;
}
