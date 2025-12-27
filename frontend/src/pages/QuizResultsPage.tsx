import { useLocation, useParams, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';

interface TeamResult {
      teamId: number;
      teamName: string;
      points: number;
      position: number;
}

function QuizResultsPage() {
      const location = useLocation();
      const params = useParams();
      const navigate = useNavigate();
      const [results, setResults] = useState<TeamResult[]>([]);

      useEffect(() => {
            const resultsData = location.state?.results;

            if (resultsData) {
                  // Sort by position to ensure correct order
                  const sortedResults = Array.isArray(resultsData) ? [...resultsData].sort((a, b) => a.position - b.position) : [];
                  setResults(sortedResults);
                  console.log('Quiz results:', sortedResults);
            }
      }, [location.state, navigate]);

      const getMedalEmoji = (position: number) => {
            switch (position) {
                  case 1:
                        return '🥇';
                  case 2:
                        return '🥈';
                  case 3:
                        return '🥉';
                  default:
                        return `${position}️⃣`;
            }
      };

      return (
            <div className='flex h-screen w-full flex-col items-center justify-center bg-gradient-to-b from-[#0a4a4d] to-[#1CABB0] p-4'>
                  <div className='w-full max-w-2xl'>
                        <h1 className='mb-2 text-center text-4xl font-bold text-white md:text-5xl'>Quiz Results</h1>
                        <p className='mb-8 text-center text-lg text-gray-100'>Session: {params.sessionCode}</p>

                        <div className='space-y-4'>
                              {results.length > 0 ? (
                                    results.map((team) => (
                                          <div
                                                key={team.teamId}
                                                className={`rounded-lg p-6 text-white shadow-lg transition-all ${
                                                      team.position === 1 ? 'scale-105 bg-yellow-500' : team.position === 2 ? 'bg-gray-400' : team.position === 3 ? 'bg-orange-500' : 'bg-gray-700'
                                                }`}
                                          >
                                                <div className='flex items-center justify-between'>
                                                      <div className='flex items-center gap-4'>
                                                            <span className='text-3xl'>{getMedalEmoji(team.position)}</span>
                                                            <div>
                                                                  <p className='text-2xl font-bold'>{team.teamName}</p>
                                                                  <p className='text-sm opacity-90'>Position: #{team.position}</p>
                                                            </div>
                                                      </div>
                                                      <div className='text-right'>
                                                            <p className='text-3xl font-bold'>{team.points}</p>
                                                            <p className='text-sm opacity-90'>points</p>
                                                      </div>
                                                </div>
                                          </div>
                                    ))
                              ) : (
                                    <div className='rounded-lg bg-gray-700 p-8 text-center text-white'>
                                          <p className='text-lg'>Loading results...</p>
                                    </div>
                              )}
                        </div>

                        <div className='mt-8 text-center'>
                              <p className='text-gray-100'>Quiz session: {params.sessionCode}</p>
                        </div>
                  </div>
            </div>
      );
}

export default QuizResultsPage;
