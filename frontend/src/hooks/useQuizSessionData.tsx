// hooks/useQuizSessionData.ts
import { useState, useEffect } from 'react';

export interface TeamInfo {
      teamId: number;
      teamName: string;
      teamJoinCode: string;
      playerCount: number;
}

export interface TeamData {
      teamId: number | null;
      teamName: string;
      teamJoinCode: string;
}

export interface QuizPlayer {
      playerId: number;
      nickname: string;
      captain: boolean;
}

export interface QuizSessionData {
      quizSessionId: number | null;
      sessionJoinCode: string | null;
      quizId: number | null;
      quizTitle: string;
      quizDescription: string;
      quizPlayerId: number | null;
      username: string;
      availableTeams: TeamInfo[];
      team: TeamData | null;
      teamMembers: QuizPlayer[];
}

const STORAGE_KEY = 'QUIZ_SESSION_DATA';

export function useQuizSessionData() {
      const [quizData, setQuizData] = useState<QuizSessionData>(() => {
            const stored = sessionStorage.getItem(STORAGE_KEY);
            if (stored) {
                  const parsed = JSON.parse(stored);
                  return {
                        ...parsed,
                        teamMembers: parsed.teamMembers || [],
                  };
            }
            return {
                  quizSessionId: null,
                  sessionJoinCode: null,
                  quizId: null,
                  quizTitle: '',
                  quizDescription: '',
                  quizPlayerId: null,
                  username: '',
                  availableTeams: [],
                  team: null,
                  teamMembers: [],
            };
      });

      useEffect(() => {
            sessionStorage.setItem(STORAGE_KEY, JSON.stringify(quizData));
      }, [quizData]);

      const updateTeam = (data: { teamId: number; teamName: string; teamJoinCode: string }) => {
            setQuizData((prev) => ({
                  ...prev,
                  team: {
                        teamId: data.teamId,
                        teamName: data.teamName,
                        teamJoinCode: data.teamJoinCode,
                  },
            }));
      };

      const updateTeamMembers = (members: QuizPlayer[]) => {
            setQuizData((prev) => ({
                  ...prev,
                  teamMembers: members,
            }));
      };

      const addTeamMember = (member: QuizPlayer) => {
            setQuizData((prev) => ({
                  ...prev,
                  teamMembers: [...prev.teamMembers, member],
            }));
      };

      const resetQuiz = () => {
            sessionStorage.removeItem(STORAGE_KEY);
            setQuizData({
                  quizSessionId: null,
                  sessionJoinCode: null,
                  quizId: null,
                  quizTitle: '',
                  quizDescription: '',
                  quizPlayerId: null,
                  username: '',
                  availableTeams: [],
                  team: null,
                  teamMembers: [],
            });
      };

      return {
            quizData,
            setQuizData, // used after joining quiz
            updateTeam, // used after creating/joining team
            updateTeamMembers, // update all members (from WebSocket or polling)
            addTeamMember, // add single member
            resetQuiz, // logout
      };
}

export default useQuizSessionData;
