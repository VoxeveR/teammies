-- ===== Users =====
INSERT INTO users (user_id, email, username, password, is_enabled, created_at, updated_at)
VALUES (10000, 'pz@pz.pl', 'piotrz', '$2a$12$if6C1YPYsbiom536vKemDOlUbJJwhj3c7GUjLJdEbLQkiKy5ddJg6', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (user_id, email, username, password, is_enabled, created_at, updated_at)
VALUES (20000, 'p@p.pl', 'anna99', '$2a$12$if6C1YPYsbiom536vKemDOlUbJJwhj3c7GUjLJdEbLQkiKy5ddJg6', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== Leagues =====
INSERT INTO leagues (id, league_name, description, team_size, start_date, end_date, max_teams, is_public, created_by, created_at, updated_at)
VALUES (1, 'PK Quiz League', 'Mocked league for testing', 5, '2025-12-01', '2025-12-31', 10, true, 10000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== Teams =====
INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (1, 1, 'Gołsony', 'GOL123', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (2, 1, 'Ochmany', 'OCH456', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (3, 1, 'Konsiantka', 'KON789', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (4, 1, 'Kiwiory', 'KIW101', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (5, 1, 'Frajerzy', 'FRA202', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (6, 1, 'Nooby', 'NOO303', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== League Standings =====
INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (1, 1, 1, 100, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (2, 1, 2, 95, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (3, 1, 3, 80, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (4, 1, 4, 50, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (5, 1, 5, 5, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (6, 1, 6, 0, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== Quizzes =====
INSERT INTO quizzes (id, league_id, title, description, time_limit, created_by, published, created_at, updated_at)
VALUES (1, 1, 'PK QUIZ 1', 'Mocked quiz', 5, 10000, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO quizzes (id, league_id, title, description, time_limit, created_by, published, created_at, updated_at)
VALUES (2, 1, 'PK QUIZ 2', 'Mocked quiz', 5, 10000, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
