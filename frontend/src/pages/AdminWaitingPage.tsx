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
      const [selectedTeam, setSelectedTeam] = useState<Team | null>(null);
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
                                    } else if (event.eventType === 'SESSION_CLOSED') {
                                          console.log('Session closed event');
                                          toast.success('Quiz session has been closed!');
                                          navigate('/join');
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
                                    // Redirect to results page with results data
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

      async function handleCloseQuiz() {
            if (!params.sessionCode) return;

            try {
                  const response = await api.post(`/quiz-sessions/${params.sessionCode}/close`);
                  console.log('Quiz closed:', response.data);
                  toast.success('Quiz session closed!');
                  // Navigate back to join page
                  navigate('/leagues');
            } catch (error) {
                  console.error('Failed to close quiz:', error);
                  toast.error('Failed to close quiz');
            }
      }

      return (
            <div className='flex h-screen w-full flex-col items-center justify-center-safe'>
                  <div className='bg-quiz-white flex h-screen w-full flex-col items-center gap-8 rounded-lg p-10 shadow-md lg:h-168 lg:w-fit'>
                        <div className='flex w-full flex-row justify-evenly'>
                              <img src='/src/assets/logo.svg' className='h-32 w-32 animate-spin [animation-duration:1.5s] lg:h-32 lg:w-32' />
                        </div>

                        <div className='text-quiz-dark-green text-3xl lg:text-6xl'>{isQuizRunning ? 'Quiz in progress!' : 'Waiting for players...'}</div>
                        <div className='text-quiz-green -mb-8 text-center text-xl'>Quiz code</div>
                        {isQuizRunning ? (
                              <div className='text-quiz-green text-2xl font-bold'>Quiz is now running...</div>
                        ) : (
                              <div className='flex flex-row items-center justify-center gap-4'>
                                    <button onClick={handleStartQuiz} className='button lg:w-40!'>
                                          Start Quiz
                                    </button>
                                    <div className='flex flex-col justify-between'>
                                          <div className='bg-quiz-dark-green text-quiz-white flex gap-2 rounded-xl p-4'>
                                                <div className='flex flex-col justify-center text-xl'>{params.sessionCode}</div>
                                                <div className='w-fit cursor-pointer'>
                                                      <FontAwesomeIcon icon={faCopy} className='text-3xl' onClick={() => copyToClipboard(params.sessionCode!)} />
                                                </div>
                                          </div>
                                    </div>
                                    <button onClick={handleCloseQuiz} className='button border-red-600! bg-red-600! text-white hover:bg-red-700! lg:w-40!'>
                                          Close Quiz
                                    </button>
                              </div>
                        )}
                        <div className='text-quiz-light-green text-3xl'>Team count ({sessionTeams.length})</div>

                        <div className='scrollbar w-full overflow-y-auto lg:max-h-64'>
                              <div className='grid grid-cols-1 gap-4 px-4 lg:grid-cols-2'>
                                    {sessionTeams.length === 0 ? (
                                          <p className='text-gray-400'>Waiting for teams to join...</p>
                                    ) : (
                                          sessionTeams.map((team: Team) => (
                                                <div
                                                      key={team.teamId}
                                                      className='text-quiz-white bg-quiz-green hover:bg-quiz-dark-green cursor-pointer rounded-lg p-3 transition-colors'
                                                      onClick={() => setSelectedTeam(team)}
                                                >
                                                      <div className='font-bold'>{team.teamName}</div>
                                                      <div className='text-sm'>Players: {team.players.length}</div>
                                                </div>
                                          ))
                                    )}
                              </div>
                        </div>

                        {/* Team Members Modal */}
                        {selectedTeam && (
                              <div className='fixed inset-0 z-50 flex items-center justify-center' onClick={() => setSelectedTeam(null)}>
                                    <div className='bg-quiz-white border-quiz-dark-green mx-4 w-full max-w-md rounded-lg border-2 p-6 shadow-lg' onClick={(e) => e.stopPropagation()}>
                                          <div className='mb-4 flex items-center justify-between'>
                                                <h2 className='text-quiz-dark-green text-2xl font-bold'>{selectedTeam.teamName}</h2>
                                                <button onClick={() => setSelectedTeam(null)} className='text-2xl text-gray-400 hover:text-gray-600'>
                                                      ×
                                                </button>
                                          </div>
                                          <div className='flex flex-col gap-2'>
                                                {selectedTeam.players.length === 0 ? (
                                                      <p className='text-gray-400'>No players yet...</p>
                                                ) : (
                                                      selectedTeam.players
                                                            .sort((a, b) => {
                                                                  if (a.captain === b.captain) return 0;
                                                                  return a.captain ? -1 : 1;
                                                            })
                                                            .map((player) => (
                                                                  <div key={player.playerId} className='bg-quiz-green text-quiz-white flex items-center gap-2 rounded p-2'>
                                                                        {player.captain && <span>👑</span>}
                                                                        <span className='font-semibold'>{player.playerUsername}</span>
                                                                  </div>
                                                            ))
                                                )}
                                          </div>
                                    </div>
                              </div>
                        )}
                  </div>
            </div>
      );
}

export default AdminWaitingPage;
