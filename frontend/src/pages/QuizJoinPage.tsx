import axios from 'axios';
import { useState } from 'react';
import useQuizSessionData from '../hooks/useQuizSessionData';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';

const MAX_JOIN_ATTEMPTS = 5;
const COOLDOWN_TIME = 60000; // 1 minute
const STORAGE_KEY = 'quiz_join_attempts';
const BLOCK_TIME_KEY = 'quiz_join_block_time';

function QuizJoinPage() {
      const [quizCode, setQuizCode] = useState('');
      const [username, setUsername] = useState('');
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
      const navigate = useNavigate();
      const { setQuizData } = useQuizSessionData();

      function handleSubmit() {
            if (!quizCode.trim() || !username.trim()) {
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

            axios.post('http://localhost:8080/api/quiz-sessions/join', {
                  joinCode: quizCode,
                  username: username,
            })
                  .then((response) => {
                        if (response.status === 200) {
                              setJoinAttempts(0);
                              localStorage.removeItem(STORAGE_KEY);
                              localStorage.removeItem(BLOCK_TIME_KEY);
                              setQuizData(response.data);
                              navigate(`/quiz/${response.data.sessionJoinCode}/team-join`);
                              console.log('Success!');
                        }
                  })
                  .catch((error) => {
                        console.log(error);
                        if (error.response?.status === 404) {
                              toast.error('Invalid Lobby Code!');
                        } else if (error.response?.status === 429) {
                              toast.error('Too many join requests. Please try again later.');
                        } else {
                              toast.error("Can't join quiz!. Try again later.");
                        }
                  });
      }
      return (
            <div className='flex h-screen w-full items-center justify-center overflow-hidden'>
                  <div className='flex h-full w-full items-center bg-[#CAF5F7] p-6 lg:h-fit lg:w-175 lg:rounded-[30px]'>
                        <div className='flex h-fit w-full flex-col items-center justify-center rounded-4xl border-2 ps-4 pe-4 pt-16 pb-16 lg:h-fit lg:border-0 lg:p-10'>
                              <div className='max-w-full pb-8 text-center text-3xl leading-tight wrap-break-word hyphens-auto sm:text-4xl md:text-5xl lg:pb-4 lg:text-6xl'>Join With Code</div>
                              <div className='w-full'>
                                    <input
                                          placeholder='Your Code'
                                          type='text'
                                          className='bg-quiz-green lg:h- w-full rounded-4xl p-4 text-white sm:text-4xl md:text-5xl lg:w-full lg:text-5xl'
                                          onChange={(e) => setQuizCode(e.target.value)}
                                    ></input>
                                    <input
                                          placeholder='Your Nickname'
                                          type='text'
                                          className='bg-quiz-green lg:h- mt-2 w-full rounded-4xl p-4 text-white sm:text-4xl md:text-5xl lg:w-full lg:text-5xl'
                                          onChange={(e) => setUsername(e.target.value)}
                                    ></input>
                                    <button className='button mt-4 rounded-4xl text-3xl' onClick={handleSubmit}>
                                          JOIN
                                    </button>
                              </div>
                        </div>
                  </div>
            </div>
      );
}

export default QuizJoinPage;
