package com.voxever.teammies.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "leagues")
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leagueId;

    @Column(name = "description")
    private String description;

    @Column(name = "team_size", nullable = false)
    private Integer teamSize;

    @Column(name = "max_teams", nullable = false)
    private Integer maxTeams;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy="league")
    private List<Team> teams;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

/*

@startuml
skinparam classAttributeIconSize 0

' ====================
' Services
' ====================
class UserService {
    +registerUser(userDTO)
    +login(credentials)
    +getUserProfile(userId)
    +validateUserCredentials(credentials)
}

class LeagueService {
    +createLeague(userId, leagueDTO)
    +getLeaguesForUser(userId)
    +generateLeagueJoinCode(leagueId)
    +updateLeagueStandings(sessionId)
}

class TeamService {
    +createTeam(userId, sessionId, teamDTO)
    +joinTeam(sessionId, joinCode, playerName)
    +getTeamStatus(teamId)
    +generateTeamJoinCode(teamId)
}

class QuizService {
    +createQuiz(leagueId, quizDTO)
    +addQuestion(quizId, questionDTO)
    +addAnswerOption(questionId, optionDTO)
    +getQuiz(quizId)
    +validateQuiz(quizId)
}

class SessionService {
    +startSession(quizId)
    +joinSession(sessionJoinCode, playerName)
    +nextRound(sessionId)
    +endSession(sessionId)
    +getSessionStatus(sessionId)
}

class ScoringService {
    +calculatePoints(sessionId, teamId, answers)
    +updateTeamScore(sessionTeamId, points)
    +finalizeSessionScores(sessionId)
}

' ====================
' Relationships
' ====================
UserService --> User
LeagueService --> League
TeamService --> Team
TeamService --> SessionService
QuizService --> Quiz
QuizService --> Question
QuizService --> AnswerOption
SessionService --> QuizSession
SessionService --> SessionTeam
ScoringService --> SessionTeam
LeagueService --> Team
LeagueService --> Quiz

' ====================
' Notes for structure
' ====================
note right of UserService
Responsible for:
- user registration and login
- user profile and credentials
end note

note right of LeagueService
Responsible for:
- league creation and management
- generating join codes for leagues
- updating league standings
end note

note right of TeamService
Responsible for:
- creating/joining teams
- generating team join codes
- delegating session logic to SessionService
end note

note right of QuizService
Responsible for:
- creating quizzes and questions
- adding answer options
- validating quiz correctness
end note

note right of SessionService
Responsible for:
- managing quiz sessions
- tracking session state
- player joining session
- advancing rounds and ending session
end note

note right of ScoringService
Responsible for:
- calculating points per team
- updating scores
- finalizing session results
end note

@enduml

 */



/*
@startuml
' ERD dla systemu quizów drużynowych z trwałym kodem drużyny
' i tymczasowymi graczami w sesjach

!define TABLE(name,desc) class name as "desc\n--" << (T,#FFEECC) >>

' ===== Tables =====
TABLE(users, "users") {
  * id : BIGSERIAL
  username : VARCHAR(50) <<NOT NULL>>
  email : VARCHAR(255) <<NOT NULL>>
  password_hash : VARCHAR(255) <<NOT NULL>>
  role : VARCHAR(50) <<NOT NULL>>
  created_at : TIMESTAMPTZ <<NOT NULL>>
}

TABLE(leagues, "leagues") {
  * id : BIGSERIAL
  name : VARCHAR(255) <<NOT NULL>>
  description : TEXT
  created_by : BIGINT <<NOT NULL>>
  created_at : TIMESTAMPTZ <<NOT NULL>>
}

TABLE(teams, "teams") {
  * id : BIGSERIAL
  league_id : BIGINT <<NOT NULL>>
  name : VARCHAR(255) <<NOT NULL>>
  join_code : VARCHAR(20) <<NOT NULL>> ' trwały kod drużyny
  created_at : TIMESTAMPTZ <<NOT NULL>>
}

TABLE(quizzes, "quizzes") {
  * id : BIGSERIAL
  league_id : BIGINT <<NOT NULL>>
  title : VARCHAR(255) <<NOT NULL>>
  description : TEXT
  created_by : BIGINT <<NOT NULL>>
  published : BOOLEAN <<NOT NULL>>
  created_at : TIMESTAMPTZ <<NOT NULL>>
}

TABLE(questions, "questions") {
  * id : BIGSERIAL
  quiz_id : BIGINT <<NOT NULL>>
  text : TEXT <<NOT NULL>>
  question_type : VARCHAR(20) <<NOT NULL>>
  points : INT <<NOT NULL>>
  position : INT <<NOT NULL>>
  created_at : TIMESTAMPTZ <<NOT NULL>>
}

TABLE(answer_options, "answer_options") {
  * id : BIGSERIAL
  question_id : BIGINT <<NOT NULL>>
  text : TEXT <<NOT NULL>>
  is_correct : BOOLEAN <<NOT NULL>>
  position : INT <<NOT NULL>>
}

TABLE(quiz_sessions, "quiz_sessions") {
  * id : BIGSERIAL
  quiz_id : BIGINT <<NOT NULL>>
  start_time : TIMESTAMPTZ
  state : VARCHAR(20) <<NOT NULL>>
  current_round : INT <<NOT NULL>>
  join_code : VARCHAR(20) <<NOT NULL>> ' kod dołączenia do sesji
  join_code_expires_at : TIMESTAMPTZ
  created_at : TIMESTAMPTZ <<NOT NULL>>
}

TABLE(session_teams, "session_teams") {
  * id : BIGSERIAL
  session_id : BIGINT <<NOT NULL>>
  team_id : BIGINT ' powiązanie z trwałą drużyną
  display_name : VARCHAR(255) <<NOT NULL>>
  score : INT <<NOT NULL>>
  connected : BOOLEAN <<NOT NULL>>
  members : TEXT <<NOT NULL>> ' tymczasowi gracze w JSON array
  created_at : TIMESTAMPTZ <<NOT NULL>>
}

TABLE(rounds, "rounds") {
  * id : BIGSERIAL
  session_id : BIGINT <<NOT NULL>>
  number : INT <<NOT NULL>>
  question_id : BIGINT
  time_limit_seconds : INT <<NOT NULL>>
  status : VARCHAR(20) <<NOT NULL>>
  ends_at : TIMESTAMPTZ
  created_at : TIMESTAMPTZ <<NOT NULL>>
}

TABLE(player_answers, "player_answers") {
  * id : BIGSERIAL
  round_id : BIGINT <<NOT NULL>>
  session_team_id : BIGINT <<NOT NULL>>
  free_text : TEXT
  selected_option_ids : INT[]
  awarded_points : INT <<NOT NULL>>
  answered_at : TIMESTAMPTZ <<NOT NULL>>
}

TABLE(league_standings, "league_standings") {
  * id : BIGSERIAL
  league_id : BIGINT <<NOT NULL>>
  team_id : BIGINT <<NOT NULL>>
  points : INT <<NOT NULL>>
  matches_played : INT <<NOT NULL>>
  last_updated : TIMESTAMPTZ <<NOT NULL>>
}

' ===== Relations =====
users "1" -- "0..*" leagues : creates
leagues "1" -- "0..*" teams : contains
leagues "1" -- "0..*" quizzes : contains
quizzes "1" -- "0..*" questions : contains
questions "1" -- "0..*" answer_options : has
quiz_sessions "1" -- "0..*" session_teams : includes
quiz_sessions "1" -- "0..*" rounds : contains
rounds "1" -- "0..*" player_answers : collects
session_teams "1" -- "0..*" player_answers : submits
leagues "1" -- "0..*" league_standings : tracks
teams "1" -- "0..*" league_standings : participates

' Konkretne powiązania encji
quiz_sessions --> quizzes
session_teams --> teams
rounds --> questions
player_answers --> rounds
player_answers --> session_teams
leagues --> users

@enduml

 */