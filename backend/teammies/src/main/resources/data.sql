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
VALUES (10000, 1, 'Gołsony', 'GOL123', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (10001, 1, 'Ochmany', 'OCH456', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (10002, 1, 'Konsiantka', 'KON789', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (10003, 1, 'Kiwiory', 'KIW101', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (10004, 1, 'Frajerzy', 'FRA202', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO teams (id, league_id, name, join_code, created_at, updated_at)
VALUES (10005, 1, 'Nooby', 'NOO303', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== League Standings =====
INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (10000, 1, 10000, 100, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (10001, 1, 10001, 95, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (10002, 1, 10002, 80, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (10003, 1, 10003, 50, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (10004, 1, 10004, 5, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO league_standings (id, league_id, team_id, points, matches_played, created_at, updated_at)
VALUES (10005, 1, 10005, 0, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== Quizzes =====
INSERT INTO quizzes (id, league_id, title, description, time_limit, created_by, published, created_at, updated_at)
VALUES (1, 1, 'PK QUIZ 1', 'Mocked quiz', 5, 10000, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO quizzes (id, league_id, title, description, time_limit, created_by, published, created_at, updated_at)
VALUES (2, 1, 'PK QUIZ 2', 'Mocked quiz', 5, 10000, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ===== Questions for Quiz 1 =====
-- Question 1
INSERT INTO questions (id, quiz_id, text, question_type, points, position, created_at, updated_at)
VALUES (1000, 1, 'What is the capital of France?', 'MULTIPLE_CHOICE', 10, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

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
VALUES (1001, 1, 'Which planet is known as the Red Planet?', 'MULTIPLE_CHOICE', 10, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

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
VALUES (1002, 1, 'What is 2 + 2?', 'MULTIPLE_CHOICE', 10, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

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
VALUES (1003, 2, 'Who wrote "Romeo and Juliet"?', 'MULTIPLE_CHOICE', 10, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

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
VALUES (1004, 2, 'What is the largest ocean on Earth?', 'MULTIPLE_CHOICE', 10, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

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
VALUES (1005, 2, 'What is the chemical symbol for gold?', 'MULTIPLE_CHOICE', 10, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10020, 1005, 'Go', 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10021, 1005, 'Gd', 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10022, 1005, 'Au', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO answer_options (id, question_id, text, position, is_correct, created_at, updated_at)
VALUES (10023, 1005, 'Ag', 4, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
