import { faCopy } from '@fortawesome/free-regular-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../middleware/api';
import toast from 'react-hot-toast';

import * as StompJs from '@stomp/stompjs';
import { useEffect, useRef, useState } from 'react';

interface Team {
      teamId: number;
      teamName: string;
      teamJoinCode: string;
      players: Array<Player>;
}

interface Player {
      captain: boolean;
      playerId: number;
      playerUsername: string;
}

function AdminWaitingPage() {
      const params = useParams();
      const navigate = useNavigate();
      const [sessionTeams, setSessionTeams] = useState<Team[]>([]);
      const [isQuizRunning, setIsQuizRunning] = useState(false);
      function copyToClipboard(text: string) {
            navigator.clipboard.writeText(text);
            toast.success('Copied to clipboard!');
      }

      const stompClientRef = useRef<StompJs.Client | null>(null);

      useEffect(() => {
            if (!params.sessionCode) return;

            const stompClient = new StompJs.Client({
                  brokerURL: 'ws://localhost:8080/ws-quiz',
                  onConnect: () => {
                        console.log('Connected to WebSocket');

                        stompClient.subscribe(`/topic/quiz-session/${params.sessionCode}/admin/events`, (message) => {
                              const event = JSON.parse(message.body);
                              console.log('Received event:', event);
                              try {
                                    if (event.eventType === 'TEAM_CREATED') {
                                          console.log('Team created event');

                                          setSessionTeams((prevTeams) => [
                                                ...prevTeams,
                                                {
                                                      teamId: event.teamId,
                                                      teamName: event.teamName,
                                                      teamJoinCode: event.teamJoinCode,
                                                      players: [],
                                                },
                                          ]);
                                    } else if (event.eventType === 'PLAYER_JOINED') {
                                          console.log('Player joined event');

                                          setSessionTeams((prevTeams) =>
                                                prevTeams.map((team) =>
                                                      team.teamId === event.teamId
                                                            ? {
                                                                    ...team,
                                                                    players: [
                                                                          ...team.players,
                                                                          {
                                                                                playerId: event.playerId,
                                                                                playerUsername: event.playerUsername,
                                                                                captain: event.isCaptain,
                                                                          },
                                                                    ],
                                                              }
                                                            : team
                                                )
                                          );
                                    }
                              } catch (error) {
                                    console.error('Error parsing message:', error);
                              }
                        });

                        // Subscribe to quiz results
                        stompClient.subscribe(`/topic/quiz-session/${params.sessionCode}/results`, (message) => {
                              try {
                                    const results = JSON.parse(message.body);
                                    console.log('Quiz results received:', results);
                                    // Redirect to results page
                                    navigate(`/quiz-results/${params.sessionCode}`, { state: { results } });
                              } catch (error) {
                                    console.error('Error parsing results message:', error);
                              }
                        });
                  },
                  onDisconnect: () => {
                        console.log('Disconnected from WebSocket');
                  },
                  onStompError: (frame) => {
                        console.error('STOMP Error:', frame.body);
                  },
            });

            stompClientRef.current = stompClient;
            stompClient.activate();

            return () => {
                  if (stompClientRef.current?.active) {
                        stompClientRef.current.deactivate();
                  }
            };
      }, [params.sessionCode]);

      // Load initial teams on mount
      useEffect(() => {
            loadInitialTeams();
      }, [params.sessionCode]);

      async function loadInitialTeams() {
            try {
                  const response = await api.get(`/quiz-sessions/${params.sessionCode}/teams/all`);
                  console.log('Loaded teams:', response.data);
                  setSessionTeams(response.data);
            } catch (error) {
                  console.error('Failed to load teams:', error);
                  toast.error('Failed to load teams');
            }
      }

      async function handleStartQuiz() {
            if (!params.sessionCode) return;

            try {
                  const response = await api.post(`/quiz-sessions/${params.sessionCode}/start`);
                  console.log('Quiz started:', response.data);
                  setIsQuizRunning(true);
                  toast.success('Quiz started!');
            } catch (error) {
                  console.error('Failed to start quiz:', error);
                  toast.error('Failed to start quiz');
            }
      }

      return (
            <div className='flex h-screen w-full flex-col items-center justify-center-safe'>
                  <div className='bg-quiz-white flex h-128 w-fit flex-col items-center gap-8 rounded-lg p-10 shadow-md'>
                        <div className='flex w-full flex-row justify-evenly'>
                              <img src='/src/assets/logo.svg' className='h-32 w-32 animate-spin [animation-duration:1.5s] lg:h-32 lg:w-32' />
                        </div>

                        <div className='text-quiz-dark-green text-6xl'>{isQuizRunning ? 'QUIZ IN PROGRESS' : 'WAITING FOR PLAYERS'}</div>
                        <div className='flex flex-col justify-center'>
                              <div className='text-quiz-green text-center text-xl'>Quiz code</div>
                              <div className='bg-quiz-dark-green text-quiz-white flex gap-2 rounded-xl p-4'>
                                    <div className='flex flex-col justify-center text-xl'>{params.sessionCode}</div>
                                    <div className='w-fit cursor-pointer'>
                                          <FontAwesomeIcon icon={faCopy} className='text-3xl' onClick={() => copyToClipboard(params.sessionCode!)} />
                                    </div>
                              </div>
                        </div>
                        <div className='text-quiz-light-green text-3xl'>Participants ({sessionTeams.length})</div>
                        {isQuizRunning ? (
                              <div className='text-quiz-green text-2xl font-bold'>Quiz is now running...</div>
                        ) : (
                              <button onClick={handleStartQuiz} className='button'>
                                    Start Quiz
                              </button>
                        )}
                        <div className='flex flex-col gap-2'>
                              {sessionTeams.length === 0 ? (
                                    <p className='text-gray-400'>Waiting for teams to join...</p>
                              ) : (
                                    sessionTeams.map((team: Team) => (
                                          <div key={team.teamId} className='text-quiz-dark-green'>
                                                <div className='font-bold'>{team.teamName}</div>
                                                <div className='text-sm text-gray-600'>Players: {team.players.length}</div>
                                          </div>
                                    ))
                              )}
                        </div>
                  </div>
            </div>
      );
}

export default AdminWaitingPage;
