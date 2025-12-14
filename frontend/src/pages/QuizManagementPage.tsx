import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import Navbar from '../components/general/Navbar';
import ModalWithForm from '../components/management/ModalWithForm';
import NoContent from '../components/management/NoContent';
import QuizTable from '../components/management/QuizList';
import api from '../middleware/api';
import toast from 'react-hot-toast';
import type { Quiz } from '../components/management/QuizList';
import { useNavigate } from 'react-router-dom';

interface BackendQuiz {
      id: number;
      leagueId: number;
      title: string;
      description: string;
      published: boolean;
      createdByUsername: string;
      questions?: any[];
}

interface LeagueStanding {
      teamName: string;
      points: number;
      matchesPlayed: number;
}

const medals = ['🥇', '🥈', '🥉'];

function QuizManagementPage() {
      const { leagueId } = useParams();

      const [leagueName, setLeagueName] = useState<string>('');
      const [standings, setStandings] = useState<LeagueStanding[]>([]);
      const navigate = useNavigate();
      const [quizzes, setQuizzes] = useState<BackendQuiz[]>([]);
      const [loading, setLoading] = useState(true);
      const [editingQuiz, setEditingQuiz] = useState<BackendQuiz | null>(null);

      useEffect(() => {
            if (!leagueId) return;

            async function fetchLeagueAndQuizzes() {
                  setLoading(true);
                  try {
                        const leagueResp = await api.get(`/leagues/${leagueId}`);
                        setLeagueName(leagueResp.data.league_name);

                        const quizzesResp = await api.get<BackendQuiz[]>(`/leagues/${leagueId}/quizzes`);
                        setQuizzes(quizzesResp.data);

                        const rankingResp = await api.get<LeagueStanding[]>(`/leagues/${leagueId}/ranking`);
                        setStandings(rankingResp.data);
                  } catch (err: any) {
                        if (err.response?.status === 404) {
                              toast.error('League not found');
                        } else if (err.response?.status === 403) {
                              toast.error('You do not have access to this league');
                        } else {
                              toast.error('Failed to load league');
                        }

                        // Optionally redirect back to /leagues
                        navigate('/leagues');
                  } finally {
                        setLoading(false);
                  }
            }

            fetchLeagueAndQuizzes();
      }, [leagueId]);

      const handleStart = async (quizId: number) => {
            if (!leagueId) return;

            try {
                  const response = await api.post(`/leagues/${leagueId}/quizzes/${quizId}/generate-join-code`);
                  const sessionCode = response.data.joinCode;
                  toast.success('Quiz session started! Redirecting...');

                  navigate(`/waiting-for-start/${sessionCode}/`, { state: { generateResponse: response.data } });
            } catch (err) {
                  console.error(err);
                  toast.error('Failed to start quiz.');
            }
      };

      const handleAddOrUpdateQuiz = async (payload: any, quizId?: number) => {
            if (!leagueId) return;

            try {
                  // Mock timeLimit to 30 seconds for now
                  const quizPayload = {
                        ...payload,
                        timeLimit: 5,
                  };

                  if (quizId) {
                        await api.put(`/leagues/${leagueId}/quizzes/${quizId}`, quizPayload);
                        toast.success('Quiz updated successfully!');
                  } else {
                        await api.post(`/leagues/${leagueId}/quizzes`, quizPayload);
                        toast.success('Quiz created successfully!');
                  }

                  const resp = await api.get(`/leagues/${leagueId}/quizzes`);
                  setQuizzes(resp.data);
                  setEditingQuiz(null);
            } catch (err) {
                  console.error(err);
                  toast.error(quizId ? 'Failed to update quiz.' : 'Failed to create quiz.');
            }
      };

      const handleDeleteQuiz = async (quiz: Quiz & { id: number }) => {
            if (!leagueId) return;

            try {
                  await api.delete(`/leagues/${leagueId}/quizzes/${quiz.id}`);
                  toast.success('Quiz deleted successfully!');

                  // Remove the quiz from state
                  setQuizzes((prev) => prev.filter((q) => q.id !== quiz.id));
            } catch (err) {
                  console.error(err);
                  toast.error('Failed to delete quiz.');
            }
      };

      return (
            <div className='flex h-fit flex-col lg:h-screen'>
                  <Navbar />

                  <div className='flex h-fit w-full items-center justify-center lg:h-full'>
                        <div className='bg-quiz-white lg:bg-quiz-white/75 flex h-fit min-h-screen w-full flex-col gap-3 p-2 lg:h-[90%] lg:min-h-fit lg:w-4/5 lg:gap-6 lg:rounded-xl lg:p-10'>
                              <div className='text-center text-6xl lg:text-left'>{leagueName}</div>

                              <div className='flex flex-1 flex-col gap-6 lg:flex-row'>
                                    {/* Left panel: leaderboard */}
                                    <div className='bg-quiz-white flex h-fit flex-col gap-4 rounded-xl p-2 pt-0 lg:w-3/10 lg:p-6'>
                                          <div className='text-quiz-green text-center text-3xl lg:text-left'>Leaderboard</div>
                                          <div className='scrollbar max-h-140 min-h-0 flex-1 overflow-y-auto'>
                                                {standings.map((team, index) => (
                                                      <div key={index} className={`border-quiz-green flex justify-between border-b ${index < 3 ? 'ps-2 pe-2 pt-6 pb-6' : 'p-2'}`}>
                                                            <div className='w-1/10 text-center'>{index + 1}</div>
                                                            <div className='flex-1 text-center'>
                                                                  {medals[index] || ''}
                                                                  {team.teamName}
                                                                  {medals[index] || ''}
                                                            </div>
                                                            <div className='w-1/10 text-center'>{team.points}</div>
                                                      </div>
                                                ))}
                                          </div>
                                    </div>

                                    {/* Right panel: quizzes */}
                                    <div className='flex flex-1 flex-col lg:p-2'>
                                          <div className='flex shrink-0 flex-col items-center justify-between lg:mb-4 lg:flex-row'>
                                                <h2 className='pt-4 text-2xl font-bold lg:pt-0'>League Quizzes</h2>
                                                <ModalWithForm onSubmitData={handleAddOrUpdateQuiz} />
                                          </div>

                                          <div className='relative h-full w-full flex-1'>
                                                <div className='scrollbar inset-0 lg:absolute lg:overflow-auto'>
                                                      {loading ? (
                                                            <div className='p-4 text-center'>Loading...</div>
                                                      ) : quizzes.length > 0 ? (
                                                            <QuizTable
                                                                  mockedQuizzes={quizzes.map((q) => ({
                                                                        id: q.id,
                                                                        name: q.title,
                                                                        date: '—',
                                                                        status: q.published ? 'Live' : 'Upcoming',
                                                                  }))}
                                                                  onQuizClick={(quiz: Quiz & { id: number }) => {
                                                                        const backendQuiz = quizzes.find((q) => q.id === quiz.id);
                                                                        if (backendQuiz) setEditingQuiz(backendQuiz);
                                                                  }}
                                                                  onStartQuiz={(quiz: Quiz & { id: number }) => handleStart(quiz.id)}
                                                                  onDeleteQuiz={(quiz: Quiz & { id: number }) => handleDeleteQuiz(quiz)}
                                                            />
                                                      ) : (
                                                            <NoContent title='No quizzes yet' description='It looks like there are no quizzes in this league'>
                                                                  <ModalWithForm onSubmitData={handleAddOrUpdateQuiz} />
                                                            </NoContent>
                                                      )}
                                                </div>
                                          </div>
                                    </div>
                              </div>
                        </div>
                  </div>

                  {/* Edit modal */}
                  {editingQuiz && <ModalWithForm initialData={editingQuiz} quizId={editingQuiz.id} onSubmitData={handleAddOrUpdateQuiz} onClose={() => setEditingQuiz(null)} />}
            </div>
      );
}

export default QuizManagementPage;
