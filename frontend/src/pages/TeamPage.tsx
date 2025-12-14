import axios from 'axios';
import { useState } from 'react';
import useQuizSessionData from '../hooks/useQuizSessionData';
import { useNavigate, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';

function TeamPage() {
      const [teamName, setTeamName] = useState('');
      const [teamCode, setTeamCode] = useState('');
      const { quizData, updateTeam } = useQuizSessionData();
      const params = useParams();

      const navigate = useNavigate();
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

            axios.post(`http://localhost:8080/api/quiz-sessions/${params.sessionCode}/teams/join`, {
                  teamJoinCode: teamCode,
                  quizPlayerId: quizData.quizPlayerId,
            })
                  .then((response) => {
                        if (response.status === 200) {
                              updateTeam(response.data);
                              navigate(`/quiz/${params.sessionCode}/${response.data.teamJoinCode}/waiting-for-start`);
                              console.log('Success!');
                        } else return { success: false, message: 'Login failed!' };
                  })
                  .catch((error) => {
                        console.log(error);
                        if (error.response.status === 404) {
                              toast.error('Invalid Team Code!');
                        } else {
                              toast.error("Can't create team!. Try again later.");
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
