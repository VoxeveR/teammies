import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import Navbar from '../components/general/Navbar';
import ModalWithForm from '../components/management/ModalWithForm';
import NoContent from '../components/management/NoContent';
import QuizList from '../components/management/QuizList';
import type { Quiz } from '../components/management/QuizList';
import api from '../middleware/api';

const mockedTeams = {
      teams: [
            { team_name: 'gołsony', points: 100 },
            { team_name: 'ochmany', points: 95 },
            { team_name: 'konsiantka', points: 80 },
            { team_name: 'kiwiory', points: 50 },
            { team_name: 'frajerzy', points: 5 },
            { team_name: 'nooby', points: 0 },
      ],
};

interface BackendQuiz {
      id: number;
      leagueId: number;
      title: string;
      description: string;
      published: boolean;
      createdByUsername: string;
}

const mockedQuizName = 'PK QUIZ';

function QuizManagementPage() {
      const { leagueId } = useParams(); // <- pobieramy ID z URL
      const [quizzes, setQuizzes] = useState<Quiz[]>([]);
      const [loading, setLoading] = useState(true);

      const medals = ['🥇', '🥈', '🥉'];

      // ---- FETCH QUIZÓW Z BACKENDU ----
      useEffect(() => {
            if (!leagueId) return;

            async function fetchQuizzes() {
                  try {
                        const resp = await api.get<BackendQuiz[]>(`/leagues/${leagueId}/quizzes`);
                        const data = resp.data;

                        // Mapowanie backend → frontend
                        const mapped: Quiz[] = data.map((q) => ({
                              name: q.title,
                              status: q.published ? 'Live' : 'Upcoming', // backend NIE ma statusu
                              date: '—', // backend NIE ma daty
                        }));

                        setQuizzes(mapped);
                  } catch (err) {
                        console.error('Error fetching quizzes:', err);
                  } finally {
                        setLoading(false);
                  }
            }

            fetchQuizzes();
      }, [leagueId]);

      return (
            <div className='flex h-fit flex-col lg:h-screen'>
                  <Navbar />

                  <div className='flex h-fit w-full items-center justify-center lg:h-full'>
                        <div className='bg-quiz-white lg:bg-quiz-white/75 flex h-fit min-h-screen w-full flex-col gap-3 p-2 lg:h-[90%] lg:min-h-fit lg:w-4/5 lg:gap-6 lg:rounded-xl lg:p-10'>
                              <div className='text-center text-6xl lg:text-left'>{mockedQuizName}</div>
                              <div className='flex flex-1 flex-col gap-6 lg:flex-row'>
                                    {/* Lewy panel */}
                                    <div className='bg-quiz-white flex h-fit flex-col gap-4 rounded-xl p-2 pt-0 lg:w-3/10 lg:p-6'>
                                          <div className='text-quiz-green text-center text-3xl lg:text-left'>Leaderboard</div>
                                          <div className='scrollbar max-h-140 min-h-0 flex-1 overflow-y-auto pe-6'>
                                                {mockedTeams.teams.map((team, index) => (
                                                      <div key={index} className={`flex justify-between border-b-2 ${index < 3 ? 'ps-2 pe-2 pt-6 pb-6' : 'p-2'}`}>
                                                            <div className='w-1/10 text-center'>{index + 1}</div>

                                                            <div className='flex-1 text-center'>
                                                                  {medals[index] || ''}
                                                                  {team.team_name}
                                                                  {medals[index] || ''}
                                                            </div>

                                                            <div className='w-1/10 text-center'>{team.points}</div>
                                                      </div>
                                                ))}
                                          </div>
                                    </div>

                                    {/* Prawy panel */}
                                    <div className='flex flex-1 flex-col lg:p-2'>
                                          <div className='flex shrink-0 flex-col items-center justify-between lg:mb-4 lg:flex-row'>
                                                <h2 className='pt-4 text-2xl font-bold lg:pt-0'>League Quizzes</h2>
                                                <ModalWithForm />
                                          </div>

                                          <div className='relative h-full w-full flex-1'>
                                                <div className='scrollbar inset-0 pe-2 lg:absolute lg:overflow-auto'>
                                                      {loading ? <div className='p-4 text-center'>Loading...</div> : <QuizList mockedQuizzes={quizzes} />}

                                                      {!loading && quizzes.length === 0 && (
                                                            <NoContent title='No quizzes yet' description='It looks like there are no quizzes in this league'>
                                                                  <ModalWithForm />
                                                            </NoContent>
                                                      )}
                                                </div>
                                          </div>
                                    </div>
                              </div>
                        </div>
                  </div>
            </div>
      );
}

export default QuizManagementPage;
