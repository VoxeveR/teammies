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

                                    if (firstQuestion.id || firstQuestion.questionId) {
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

      // useEffect(() => {
      //       if (!params.sessionCode || !params.teamCode) return;

      //       const fetchMembers = async () => {
      //             try {
      //                   const response = await axios.get(`http://localhost:8080/api/quiz-sessions/${params.sessionCode}/teams/${params.teamCode}/members`);
      //                   const members = response.data.members || [];
      //                   const memberCount = members.length;

      //                   // Only update if count actually changed
      //                   if (memberCount !== lastCountRef.current) {
      //                         setLocalTeamMembers(members);
      //                         updateTeamMembersRef.current(members);
      //                         lastCountRef.current = memberCount;
      //                   }
      //                   // Only set error to null if there was an error before
      //                   if (error !== null) {
      //                         setError(null);
      //                   }
      //             } catch (err: any) {
      //                   const errorMsg = err.response?.data?.message || 'Failed to fetch members';
      //                   // Only update error if it changed
      //                   if (error !== errorMsg) {
      //                         setError(errorMsg);
      //                   }
      //                   console.error(err);
      //             }
      //       };

      //       fetchMembers();

      //       const interval = setInterval(fetchMembers, 3000);

      //       return () => clearInterval(interval);
      // }, [params.sessionCode, params.teamCode]);

      return (
            <div className='flex h-screen w-full flex-col items-center justify-center-safe'>
                  <div className='bg-quiz-white flex h-128 w-fit flex-col items-center gap-8 rounded-lg p-10 shadow-md'>
                        <div className='flex w-full flex-row justify-evenly'>
                              <div className='flex flex-col justify-center'>
                                    <div className='text-quiz-green text-center text-xl'>Quiz code</div>
                                    <div className='bg-quiz-dark-green text-quiz-white flex gap-2 rounded-xl p-4'>
                                          <div className='flex flex-col justify-center text-xl'>{params.sessionCode}</div>
                                          <div className='w-fit cursor-pointer'>
                                                <FontAwesomeIcon icon={faCopy} className='text-3xl' onClick={() => copyToClipboard(params.sessionCode!)} />
                                          </div>
                                    </div>
                              </div>
                              <img src='/src/assets/logo.svg' className='h-32 w-32 animate-spin [animation-duration:1.5s] lg:h-32 lg:w-32' />
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
                        <div className='text-quiz-dark-green text-6xl'>WAITING FOR START :D</div>
                        <div className='text-quiz-light-green text-3xl'>Participants ({localTeamMembers.length})</div>
                        <div className='flex flex-col gap-2'>
                              {error && <div className='text-sm text-red-500'>{error}</div>}
                              {localTeamMembers.length === 0 ? (
                                    <p className='text-gray-400'>Waiting for team members...</p>
                              ) : (
                                    localTeamMembers.map((member: QuizPlayer) => (
                                          <div key={member.quizPlayerId} className='text-quiz-dark-green'>
                                                ✓ {member.nickname}
                                          </div>
                                    ))
                              )}
                        </div>
                  </div>
            </div>
      );
}

export default WaitingPage;
