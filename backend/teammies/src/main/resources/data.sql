-- ===== Users =====
INSERT INTO users (user_id, email, username, password, is_enabled, created_at, updated_at)
VALUES (10000, 'pz@pz.pl', 'piotrz', '$2a$12$if6C1YPYsbiom536vKemDOlUbJJwhj3c7GUjLJdEbLQkiKy5ddJg6', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (user_id, email, username, password, is_enabled, created_at, updated_at)
VALUES (20000, 'p@p.pl', 'anna99', '$2a$12$if6C1YPYsbiom536vKemDOlUbJJwhj3c7GUjLJdEbLQkiKy5ddJg6', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== Leagues =====
INSERT INTO leagues (id, league_name, description, team_size, start_date, end_date, max_teams, is_public, created_by, created_at, updated_at)
VALUES (10000, 'PK Quiz League', 'Mocked league for testing', 5, '2025-12-01', '2025-12-31', 10, true, 10000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Leagues for user 10000 (piotrz)
INSERT INTO leagues (id, league_name, description, team_size, start_date, end_date, max_teams, is_public, created_by, created_at, updated_at)
VALUES (10001, 'Tech Champions League', 'Private league for tech enthusiasts', 4, '2025-12-15', '2026-01-15', 8, false, 10000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO leagues (id, league_name, description, team_size, start_date, end_date, max_teams, is_public, created_by, created_at, updated_at)
VALUES (10002, 'Winter Knowledge Cup', 'Public winter quiz competition', 6, '2025-12-20', '2026-02-28', 12, true, 10000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO leagues (id, league_name, description, team_size, start_date, end_date, max_teams, is_public, created_by, created_at, updated_at)
VALUES (10003, 'Family Quiz Night', 'Private league for friends and family', 3, '2025-12-01', '2026-03-01', 6, false, 10000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO leagues (id, league_name, description, team_size, start_date, end_date, max_teams, is_public, created_by, created_at, updated_at)
VALUES (10004, 'Global Trivia Challenge', 'Open to everyone worldwide', 5, '2026-01-01', '2026-06-30', 20, true, 10000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Leagues for user 20000 (anna99)
INSERT INTO leagues (id, league_name, description, team_size, start_date, end_date, max_teams, is_public, created_by, created_at, updated_at)
VALUES (10005, 'History Masters', 'Private history quiz league', 4, '2025-12-10', '2026-01-31', 8, false, 20000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO leagues (id, league_name, description, team_size, start_date, end_date, max_teams, is_public, created_by, created_at, updated_at)
VALUES (10006, 'Science Explorers', 'Public science quiz competition', 5, '2026-01-01', '2026-04-30', 15, true, 20000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO leagues (id, league_name, description, team_size, start_date, end_date, max_teams, is_public, created_by, created_at, updated_at)
VALUES (10007, 'Movie Buffs League', 'Private cinema and film quiz', 3, '2025-12-05', '2026-02-15', 10, false, 20000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO leagues (id, league_name, description, team_size, start_date, end_date, max_teams, is_public, created_by, created_at, updated_at)
VALUES (10008, 'Sports & Games Arena', 'Public sports trivia league', 6, '2025-12-25', '2026-05-31', 16, true, 20000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== Teams =====
INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (10000, 10000, 'Team 1', 'GOL123', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (10001, 10000, 'Team 5', 'OCH456', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (10002, 10000, 'Team 4', 'KON789', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (10003, 10000, 'Team 3', 'KIW101', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (10004, 10000, 'Team 2', 'FRA202', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (10005, 10000, 'Team 0', 'NOO303', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== League Standings =====
INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (10000, 10000, 10000, 100, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (10001, 10000, 10001, 95, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (10002, 10000, 10002, 80, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (10003, 10000, 10003, 50, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (10004, 10000, 10004, 5, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (10005, 10000, 10005, 0, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== Quizzes =====
INSERT INTO quizzes (id, league_id, title, description, time_limit, created_by, published, created_at, updated_at)
VALUES (10000, 10000, 'PK QUIZ 1', 'Mocked quiz', 30, 10000, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO quizzes (id, league_id, title, description, time_limit, created_by, published, created_at, updated_at)
VALUES (10001, 10000, 'PK QUIZ 2', 'Mocked quiz', 5, 10000, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== Questions for Quiz 1 =====
-- Question 1
INSERT INTO questions (id, quiz_id, text, question_type, points, position, created_at, updated_at)
VALUES (1000, 10000, 'What is the capital of France?', 'MULTIPLE_CHOICE', 10, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10000, 1000, 'Paris', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10001, 1000, 'London', 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10002, 1000, 'Berlin', 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10003, 1000, 'Madrid', 4, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Question 2
INSERT INTO questions (id, quiz_id, text, question_type, points, position, created_at, updated_at)
VALUES (1001, 10000, 'Which planet is known as the Red Planet?', 'MULTIPLE_CHOICE', 10, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10004, 1001, 'Venus', 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10005, 1001, 'Mars', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10006, 1001, 'Jupiter', 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10007, 1001, 'Saturn', 4, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Question 3
INSERT INTO questions (id, quiz_id, text, question_type, points, position, created_at, updated_at)
VALUES (1002, 10000, 'What is 2 + 2?', 'MULTIPLE_CHOICE', 10, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10008, 1002, '3', 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10009, 1002, '4', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10010, 1002, '5', 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10011, 1002, '6', 4, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== Questions for Quiz 2 =====
-- Question 4
INSERT INTO questions (id, quiz_id, text, question_type, points, position, created_at, updated_at)
VALUES (1003, 10001, 'Who wrote "Romeo and Juliet"?', 'MULTIPLE_CHOICE', 10, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10012, 1003, 'William Shakespeare', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10013, 1003, 'Jane Austen', 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10014, 1003, 'Mark Twain', 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10015, 1003, 'Charles Dickens', 4, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Question 5
INSERT INTO questions (id, quiz_id, text, question_type, points, position, created_at, updated_at)
VALUES (1004, 10001, 'What is the largest ocean on Earth?', 'MULTIPLE_CHOICE', 10, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10016, 1004, 'Atlantic Ocean', 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10017, 1004, 'Indian Ocean', 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10018, 1004, 'Pacific Ocean', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10019, 1004, 'Arctic Ocean', 4, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Question 6
INSERT INTO questions (id, quiz_id, text, question_type, points, position, created_at, updated_at)
VALUES (1005, 10001, 'What is the chemical symbol for gold?', 'MULTIPLE_CHOICE', 10, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10020, 1005, 'Go', 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10021, 1005, 'Gd', 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10022, 1005, 'Au', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10023, 1005, 'Ag', 4, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
