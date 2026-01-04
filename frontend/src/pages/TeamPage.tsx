import axios from 'axios';
import { useState, useEffect, useRef } from 'react';
import useQuizSessionData from '../hooks/useQuizSessionData';
import { useNavigate, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import * as StompJs from '@stomp/stompjs';

const MAX_JOIN_ATTEMPTS = 5;
const COOLDOWN_TIME = 60000; // 1 minute
const STORAGE_KEY = 'team_join_attempts';
const BLOCK_TIME_KEY = 'team_join_block_time';

function TeamPage() {
      const [teamName, setTeamName] = useState('');
      const [teamCode, setTeamCode] = useState('');
      const [joinAttempts, setJoinAttempts] = useState(() => {
            const stored = localStorage.getItem(STORAGE_KEY);
            return stored ? parseInt(stored) : 0;
      });
      const [isBlocked, setIsBlocked] = useState(() => {
            const blockTime = localStorage.getItem(BLOCK_TIME_KEY);
            if (!blockTime) return false;
            const timeLeft = parseInt(blockTime) - Date.now();
            if (timeLeft > 0) return true;
            localStorage.removeItem(BLOCK_TIME_KEY);
            localStorage.removeItem(STORAGE_KEY);
            return false;
      });
      const { quizData, updateTeam } = useQuizSessionData();
      const params = useParams();

      const navigate = useNavigate();
      const stompClientRef = useRef<StompJs.Client | null>(null);

      useEffect(() => {
            if (!params.sessionCode) return;

            const stompClient = new StompJs.Client({
                  brokerURL: 'ws://localhost:8080/ws-quiz',
                  onConnect: () => {
                        console.log('Connected to WebSocket');

                        stompClient.subscribe(`/topic/quiz-session/${params.sessionCode}/events`, (message) => {
                              try {
                                    const event = JSON.parse(message.body);
                                    console.log('Received event:', event);

                                    if (event.eventType === 'SESSION_CLOSED') {
                                          console.log('Session closed event');
                                          toast.error('Quiz session has been closed by admin!');
                                          navigate('/join');
                                    }
                              } catch (error) {
                                    console.error('Error parsing event message:', error);
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
      }, [params.sessionCode, navigate]);
      function handleCreateTeam() {
            if (!teamName.trim()) {
                  toast.error('Please fill in all fields!');
                  return;
            }

            axios.post(`http://localhost:8080/api/quiz-sessions/${params.sessionCode}/teams`, {
                  teamName: teamName,
                  quizPlayerId: quizData.quizPlayerId,
            })
                  .then((response) => {
                        if (response.status === 200) {
                              updateTeam(response.data);
                              navigate(`/quiz/${params.sessionCode}/${response.data.teamJoinCode}/waiting-for-start`);
                              console.log('Success!');
                        }
                  })
                  .catch((error) => {
                        console.log(error);
                        toast.error("Can't create team!. Try again later.");
                  });
      }

      function handleJoinTeam() {
            if (!teamCode.trim()) {
                  toast.error('Please fill in all fields!');
                  return;
            }

            if (isBlocked) {
                  toast.error('Too many join attempts. Please try again later.');
                  return;
            }

            const newAttempts = joinAttempts + 1;
            setJoinAttempts(newAttempts);
            localStorage.setItem(STORAGE_KEY, newAttempts.toString());

            if (newAttempts > MAX_JOIN_ATTEMPTS) {
                  const blockUntil = Date.now() + COOLDOWN_TIME;
                  localStorage.setItem(BLOCK_TIME_KEY, blockUntil.toString());
                  setIsBlocked(true);
                  toast.error(`Too many join attempts. Please try again in ${COOLDOWN_TIME / 1000} seconds.`);
                  setTimeout(() => {
                        setIsBlocked(false);
                        setJoinAttempts(0);
                        localStorage.removeItem(STORAGE_KEY);
                        localStorage.removeItem(BLOCK_TIME_KEY);
                  }, COOLDOWN_TIME);
                  return;
            }

            axios.post(`http://localhost:8080/api/quiz-sessions/${params.sessionCode}/teams/join`, {
                  teamJoinCode: teamCode,
                  quizPlayerId: quizData.quizPlayerId,
            })
                  .then((response) => {
                        if (response.status === 200) {
                              setJoinAttempts(0);
                              localStorage.removeItem(STORAGE_KEY);
                              localStorage.removeItem(BLOCK_TIME_KEY);
                              updateTeam(response.data);
                              navigate(`/quiz/${params.sessionCode}/${response.data.teamJoinCode}/waiting-for-start`);
                              console.log('Success!');
                        } else return { success: false, message: 'Login failed!' };
                  })
                  .catch((error) => {
                        console.log(error);
                        if (error.response?.status === 404) {
                              toast.error('Invalid Team Code!');
                        } else if (error.response?.status === 429) {
                              toast.error('Too many join requests. Please try again later.');
                        } else {
                              toast.error("Can't join team!. Try again later.");
                        }
                  });
      }
      return (
            <div className='flex min-h-screen flex-col lg:h-screen lg:items-center'>
                  <div className='bg-quiz-white flex flex-1 flex-col justify-center lg:items-center lg:justify-center lg:bg-transparent'>
                        <div className='bg-quiz-white/75 flex h-fit w-full flex-col gap-8 lg:flex-row lg:rounded-xl lg:p-6'>
                              <div className='h-fit w-full ps-4 pe-4'>
                                    <div className='bg-quiz-white flex flex-col justify-center gap-3 rounded-2xl border p-5 pt-2'>
                                          <div className='flex flex-row items-center'>
                                                <img src='/src/assets/team.svg'></img>
                                                <div className='grow text-[40px]'>Join team</div>
                                          </div>
                                          <div className='text-quiz-light-green -mt-3 text-center'>Use the invite code from the team captain.</div>
                                          <input className='input' placeholder='Enter invite code' onChange={(e) => setTeamCode(e.target.value)}></input>
                                          <button className='button' onClick={handleJoinTeam}>
                                                JOIN
                                          </button>
                                    </div>
                              </div>
                              <div className='h-fit w-full ps-4 pe-4'>
                                    <div className='bg-quiz-white flex flex-col justify-center gap-3 rounded-2xl border p-5 pt-2'>
                                          <div className='flex grow flex-row items-center'>
                                                <img src='/src/assets/guy.svg'></img>
                                                <div className='grow text-[40px]'>Create team</div>
                                          </div>
                                          <div className='text-quiz-light-green -mt-3 text-center'>Become the captain!</div>
                                          <input className='input' placeholder='Enter team name' onChange={(e) => setTeamName(e.target.value)}></input>
                                          <button className='button' onClick={handleCreateTeam}>
                                                CREATE
                                          </button>
                                    </div>
                              </div>
                        </div>
                  </div>
            </div>
      );
}

export default TeamPage;
