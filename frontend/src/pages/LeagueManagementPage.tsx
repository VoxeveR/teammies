import { useEffect, useMemo, useState } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faMagnifyingGlass } from '@fortawesome/free-solid-svg-icons';
import Navbar from '../components/general/Navbar';
import api from '../middleware/api';

interface League {
      league_id: number;
      league_name: string;
      description: string;
      team_size: number;
      max_eams: number;
      is_public: boolean;
      owner_id: number;
}
function LeagueManagementPage() {
      const [query, setQuery] = useState('');
      const [leaguesData, setLeaguesData] = useState<League[]>([]);

      useEffect(() => {
            api.get('/leagues/')
                  .then((response) => setLeaguesData(response.data))
                  .catch((error) => {
                        console.log(error);
                  });
      }, []);

      const filtered = useMemo(() => {
            const q = query.trim().toLowerCase();
            if (!q) return leaguesData;
            return leaguesData.filter((l) => l.league_name.toLowerCase().includes(q));
      }, [leaguesData, query]);

      return (
            <div className='flex h-screen flex-col'>
                  <Navbar />
                  <div className='flex h-full w-full items-center justify-center'>
                        <div className='bg-quiz-white lg:bg-quiz-white/75 flex h-full w-full flex-col gap-6 p-10 lg:h-[90%] lg:w-4/5 lg:rounded-xl'>
                              <h1 className='text-center text-3xl font-bold lg:text-start lg:text-5xl'>League Management</h1>
                              <div className='flex flex-1 flex-col gap-6 lg:flex-row'>
                                    {/* Lewy panel */}
                                    <div className='bg-quiz-white w-3/10 rounded-xl p-4'></div>

                                    {/* Prawy panel */}
                                    <div className='x flex flex-1 flex-col p-2'>
                                          {/* Header z napisem i searchbarem */}
                                          <div className='mb-4 flex shrink-0 items-center justify-between'>
                                                <h2 className='text-2xl font-bold'>Your Leagues</h2>
                                                <div className='relative w-full max-w-sm items-center'>
                                                      <label htmlFor='league-search' className='sr-only'>
                                                            Search leagues
                                                      </label>
                                                      <FontAwesomeIcon icon={faMagnifyingGlass} className={'text-quiz-white absolute top-1/2 left-3 -translate-y-1/2'} />
                                                      <input
                                                            id='league-search'
                                                            type='text'
                                                            value={query}
                                                            onChange={(e) => setQuery(e.target.value)}
                                                            placeholder='Search leagues...'
                                                            className='border-box input placeholder:text-quiz-white w-full !pl-10'
                                                      />
                                                </div>
                                          </div>

                                          {/* Lista lig z przewijaniem */}
                                          <div className='relative h-full w-full flex-1'>
                                                <div className='scrollbar absolute inset-0 overflow-auto pe-2'>
                                                      {filtered.map((league) => (
                                                            <div key={league.league_id} className='bg-quiz-white mt-2 flex w-full items-center justify-between rounded-lg p-4'>
                                                                  <div>
                                                                        <div className='text-2xl'>{league.league_name}</div>
                                                                        <div className='text-quiz-light-green text-sm'>{league.description}</div>
                                                                  </div>

                                                                  <div className='flex items-center gap-2'>
                                                                        <button className='button'>EDIT</button>
                                                                  </div>
                                                            </div>
                                                      ))}
                                                      {filtered.length === 0 && <p className='text-quiz-dark-green mt-4 text-center text-5xl'>No leagues found.</p>}
                                                </div>
                                          </div>
                                    </div>
                              </div>
                        </div>
                  </div>
            </div>
      );
}

export default LeagueManagementPage;
