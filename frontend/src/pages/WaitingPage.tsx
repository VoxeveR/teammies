import { faCopy } from '@fortawesome/free-regular-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import axios from 'axios';
import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuizSessionData, type QuizPlayer } from '../hooks/useQuizSessionData';
import toast from 'react-hot-toast';

import * as StompJs from '@stomp/stompjs';

function WaitingPage() {
      const params = useParams();
      const navigate = useNavigate();
      const { quizData, updateTeamMembers } = useQuizSessionData();
      const [localTeamMembers, setLocalTeamMembers] = useState<QuizPlayer[]>([]);
      const [error, setError] = useState<string | null>(null);
      const lastCountRef = useRef<number>(-1);
      const updateTeamMembersRef = useRef(updateTeamMembers);

      const stompClientRef = useRef<StompJs.Client | null>(null);

      useEffect(() => {
            if (!params.sessionCode || !params.teamCode) return;

            const stompClient = new StompJs.Client({
                  brokerURL: 'ws://localhost:8080/ws-quiz',
                  onConnect: () => {
                        console.log('Connected to WebSocket');

                        // Subscribe to team member updates
                        stompClient.subscribe(`/topic/quiz-session/${params.sessionCode}/team/${params.teamCode}`, (message) => {
                              try {
                                    const updatedMembers = JSON.parse(message.body);
                                    console.log('Team members received:', updatedMembers);
                                    setLocalTeamMembers(updatedMembers);
                                    updateTeamMembersRef.current(updatedMembers);
                              } catch (error) {
                                    console.error('Error parsing message:', error);
                              }
                        });

                        stompClient.subscribe(`/topic/quiz-session/${params.sessionCode}/events`, (message) => {
                              try {
                                    const firstQuestion = JSON.parse(message.body);
                                    console.log('Quiz started with first question:', firstQuestion);

                                    if (firstQuestion.eventType === 'SESSION_CLOSED') {
                                          console.log('Session closed event');
                                          toast.error('Quiz session has been closed by admin!');
                                          navigate('/join');
                                    } else if (firstQuestion.id || firstQuestion.questionId) {
                                          navigate(`/quiz/${params.sessionCode}/${params.teamCode}/${quizData.quizPlayerId}`, {
                                                state: { firstQuestion },
                                          });
                                    }
                              } catch (error) {
                                    console.error('Error parsing question message:', error);
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
      }, [params.sessionCode, params.teamCode, navigate]);

      function copyToClipboard(text: string) {
            navigator.clipboard.writeText(text);
            toast.success('Copied to clipboard!');
      }

      useEffect(() => {
            updateTeamMembersRef.current = updateTeamMembers;
      }, [updateTeamMembers]);

      useEffect(() => {
            if (!params.sessionCode || !params.teamCode) return;

            const fetchMembers = async () => {
                  try {
                        const response = await axios.get(`http://localhost:8080/api/quiz-sessions/${params.sessionCode}/teams/${params.teamCode}/members`);
                        const members = response.data.members || [];
                        const memberCount = members.length;

                        if (memberCount !== lastCountRef.current) {
                              setLocalTeamMembers(members);
                              updateTeamMembersRef.current(members);
                              lastCountRef.current = memberCount;
                        }

                        if (error !== null) {
                              setError(null);
                        }
                  } catch (err: any) {
                        const errorMsg = err.response?.data?.message || 'Failed to fetch members';

                        if (error !== errorMsg) {
                              setError(errorMsg);
                        }
                        console.error(err);
                  }
            };

            fetchMembers();

            const interval = setInterval(fetchMembers, 3000);

            return () => clearInterval(interval);
      }, [params.sessionCode, params.teamCode]);

      return (
            <div className='flex h-screen w-full flex-col items-center justify-center-safe lg:h-screen'>
                  <div className='bg-quiz-white flex h-screen w-screen flex-col items-center gap-8 rounded-lg p-10 shadow-md lg:h-156 lg:w-fit'>
                        {/* Mobile: Logo on top, codes in row below. Desktop: Original layout */}
                        <div className='flex w-full flex-col items-center gap-4 lg:flex-col lg:justify-evenly'>
                              {/* Logo - top on mobile, middle on desktop */}
                              <img src='/src/assets/logo.svg' className='order-first h-32 w-32 animate-spin [animation-duration:1.5s] lg:order-0 lg:h-32 lg:w-32' />

                              {/* Quiz code and Team code in same row on mobile */}
                              <div className='flex flex-row gap-4'>
                                    <div className='flex flex-col justify-center'>
                                          <div className='text-quiz-green text-center text-xl'>Quiz code</div>
                                          <div className='bg-quiz-dark-green text-quiz-white flex gap-2 rounded-xl p-4'>
                                                <div className='flex flex-col justify-center text-xl'>{params.sessionCode}</div>
                                                <div className='w-fit cursor-pointer'>
                                                      <FontAwesomeIcon icon={faCopy} className='text-3xl' onClick={() => copyToClipboard(params.sessionCode!)} />
                                                </div>
                                          </div>
                                    </div>

                                    <div className='flex flex-col justify-center'>
                                          <div className='text-quiz-green text-center text-xl'>Team code</div>
                                          <div className='bg-quiz-dark-green text-quiz-white flex gap-2 rounded-xl p-4'>
                                                <div className='flex flex-col justify-center text-xl'>{params.teamCode}</div>
                                                <div className='w-fit cursor-pointer'>
                                                      <FontAwesomeIcon icon={faCopy} className='text-3xl' onClick={() => copyToClipboard(params.teamCode!)} />
                                                </div>
                                          </div>
                                    </div>
                              </div>
                        </div>
                        <div className='text-quiz-dark-green text-3xl lg:text-6xl'>Waiting for start...</div>
                        <div className='text-quiz-light-green text-3xl'>Participants ({localTeamMembers.length})</div>
                        <div className='scrollbar w-full overflow-y-auto lg:max-h-64'>
                              {error && <div className='text-sm text-red-500'>{error}</div>}
                              {localTeamMembers.length === 0 ? (
                                    <p className='text-center text-gray-400'>Waiting for team members...</p>
                              ) : (
                                    <div className='grid h-full grid-cols-1 gap-4 px-4 lg:grid-cols-2'>
                                          {localTeamMembers
                                                .sort((a, b) => {
                                                      if (a.captain === b.captain) return 0;
                                                      return a.captain ? -1 : 1;
                                                })
                                                .map((member: QuizPlayer) => {
                                                      return (
                                                            <div key={member.playerId} className='text-quiz-white bg-quiz-green flex flex-row rounded-lg p-3 text-center'>
                                                                  {member.captain ? <div>👑</div> : <div className='opacity-0'>'👑'</div>}
                                                                  <div className='flex-1 text-sm font-semibold'>{member.nickname}</div>
                                                            </div>
                                                      );
                                                })}
                                    </div>
                              )}
                        </div>
                  </div>
            </div>
      );
}

export default WaitingPage;
