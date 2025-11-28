import { useEffect, useMemo, useState } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faMagnifyingGlass } from '@fortawesome/free-solid-svg-icons';
import Navbar from '../components/general/Navbar';
import api from '../middleware/api';
import { Datepicker, ThemeProvider } from 'flowbite-react';
import toast from 'react-hot-toast';
import ModalWithForm from '../components/management/ModalWithForm';
import NoContent from '../components/management/NoContent';
import { NavLink } from 'react-router-dom';

const customTheme = {
      datepicker: {
            root: {
                  base: 'relative',
            },
            popup: {
                  root: {
                        base: 'absolute bottom-full z-50 block pt-2',
                        inner: 'inline-block rounded-lg bg-white dark:bg-quiz-light-green p-4 shadow-lg border-2 border-quiz-dark-green',
                  },
                  header: {
                        title: 'px-2 py-3 text-center font-semibold text-quiz-dark-green',
                        selectors: {
                              base: 'mb-2 flex justify-between',
                              button: {
                                    base: 'rounded-lg bg-white px-5 py-2.5 text-sm font-semibold text-gray-900 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-200 dark:bg-quiz-dark-green dark:text-quiz-white dark:hover:bg-gray-600',
                                    prev: '',
                                    next: '',
                                    view: '',
                              },
                        },
                        footer: {
                              base: 'mt-2 flex space-x-2',
                              button: {
                                    base: 'w-full rounded-lg px-5 py-2 text-center text-sm font-medium focus:ring-4 focus:ring-primary-300',
                                    today: 'bg-primary-700 text-white hover:bg-primary-800 dark:bg-primary-600 dark:hover:bg-primary-700',
                                    clear: 'border border-gray-300 bg-white text-gray-900 hover:bg-gray-100 dark:border-gray-600 dark:bg-gray-700 dark:text-white dark:hover:bg-gray-600',
                              },
                        },
                  },
                  views: {
                        days: {
                              header: {
                                    base: 'mb-1 grid grid-cols-7',
                                    title: 'h-6 text-center text-sm font-medium leading-6  dark:text-gray-700',
                              },
                              items: {
                                    item: {
                                          selected: 'bg-quiz-light-green text-white ',
                                    },
                              },
                        },
                  },
            },
      },
};

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
      const [leagueName, setLeagueName] = useState('');
      const [description, setDescription] = useState('');
      const [teamSize, setTeamSize] = useState('');
      const [maxTeams, setMaxTeams] = useState('');
      const [isPublic, setIsPublic] = useState('private');
      const [startDate, setStartDate] = useState<Date | null>();
      const [endDate, setEndDate] = useState<Date | null>();
      const [isLoading, setIsLoading] = useState(false);
      const [showForMobile, setShowFormMobile] = useState(false);

      const fetchLeagues = () => {
            api.get('/leagues/')
                  .then((response) => setLeaguesData(response.data))
                  .catch((error) => {
                        console.log(error);
                        toast.error('Failed to load leagues');
                  });
      };

      useEffect(() => {
            fetchLeagues();
      }, []);

      const handleCreateLeague = async () => {
            if (!leagueName.trim() || !description.trim() || !teamSize || !maxTeams) {
                  toast.error('Please fill in all required fields');
                  return;
            }

            const teamSizeNum = parseInt(teamSize);
            const maxTeamsNum = parseInt(maxTeams);

            if (teamSizeNum <= 0 || maxTeamsNum <= 0) {
                  toast.error('Team size and max teams must be greater than 0');
                  return;
            }

            setIsLoading(true);

            try {
                  const response = await api.post('/leagues/', {
                        league_name: leagueName,
                        description: description,
                        team_size: teamSizeNum,
                        max_teams: maxTeamsNum,
                        is_public: isPublic === 'public',
                  });

                  toast.success('League created successfully!');
                  setLeagueName('');
                  setDescription('');
                  setTeamSize('');
                  setMaxTeams('');
                  setIsPublic('private');
                  setStartDate(null);
                  setEndDate(null);
                  fetchLeagues();
            } catch (error: any) {
                  const errorMessage = error.response?.data?.message || 'Failed to create league';
                  toast.error(errorMessage);
                  console.error(error);
            } finally {
                  setIsLoading(false);
            }
      };

      const filtered = useMemo(() => {
            const q = query.trim().toLowerCase();
            if (!q) return leaguesData;
            return leaguesData.filter((l) => l.league_name.toLowerCase().includes(q));
      }, [leaguesData, query]);

      return (
            <div className='flex h-fit flex-col lg:h-screen'>
                  <Navbar />

                  <div className='flex h-fit w-full items-center justify-center lg:h-full'>
                        <div className='bg-quiz-white lg:bg-quiz-white/75 flex h-fit min-h-screen w-full flex-col gap-3 p-4 lg:h-[90%] lg:min-h-fit lg:w-4/5 lg:gap-6 lg:rounded-xl lg:p-10'>
                              <div className='text-center text-4xl lg:text-left lg:text-6xl'>LEAGUE MANAGEMENT</div>
                              <button className='button lg:hidden' onClick={() => setShowFormMobile((prev) => !prev)}>
                                    {showForMobile ? 'HIDE FORM' : 'CREATE NEW LEAGUE'}
                              </button>

                              <div className='flex flex-1 flex-col gap-6 lg:flex-row'>
                                    {/* Lewy panel */}
                                    <div className={`bg-quiz-white flex flex-col gap-4 rounded-xl p-4 lg:w-3/10 ${showForMobile ? 'flex' : 'hidden'} lg:flex`}>
                                          <div className='text-quiz-green text-3xl'>Create a new league</div>
                                          <div>
                                                <div className='text-quiz-light-green text-xl'>League name</div>
                                                <input className='input' placeholder='League name' value={leagueName} onChange={(e) => setLeagueName(e.target.value)}></input>
                                          </div>

                                          <div>
                                                <div className='text-quiz-light-green text-xl'>League description</div>
                                                <textarea
                                                      className='input h-40 resize-none align-text-top'
                                                      placeholder='Provide a brief description of your league'
                                                      value={description}
                                                      onChange={(e) => setDescription(e.target.value)}
                                                ></textarea>
                                          </div>
                                          <div className='flex w-full flex-row gap-2'>
                                                <div>
                                                      <div className='text-quiz-light-green text-xl'>Start date</div>
                                                      <ThemeProvider theme={customTheme}>
                                                            <Datepicker onChange={(date: Date | null) => setStartDate(date)} />
                                                      </ThemeProvider>
                                                </div>
                                                <div>
                                                      <div className='text-quiz-light-green text-xl'>End date</div>
                                                      <ThemeProvider theme={customTheme}>
                                                            <Datepicker onChange={(date: Date | null) => setEndDate(date)} />
                                                      </ThemeProvider>
                                                </div>
                                          </div>
                                          <div className='flex w-full flex-row gap-2'>
                                                <div>
                                                      <div className='text-quiz-light-green text-xl'>Team size</div>
                                                      <input className='input' placeholder='Team size' type='number' value={teamSize} onChange={(e) => setTeamSize(e.target.value)}></input>
                                                </div>
                                                <div>
                                                      <div className='text-quiz-light-green text-xl'>Max teams</div>
                                                      <input className='input' placeholder='Max teams' type='number' value={maxTeams} onChange={(e) => setMaxTeams(e.target.value)}></input>
                                                </div>
                                          </div>
                                          <div>
                                                <div className='text-quiz-light-green text-xl'>Privacy</div>
                                                <select className='input' value={isPublic} onChange={(e) => setIsPublic(e.target.value)}>
                                                      <option value='private'>Private</option>
                                                      <option value='public'>Public</option>
                                                </select>
                                          </div>
                                          <div className='flex-1'></div>
                                          <div className='flex w-full items-center justify-center'>
                                                <button className='button w-[98%]! disabled:opacity-50' onClick={handleCreateLeague} disabled={isLoading}>
                                                      {isLoading ? 'CREATING...' : 'CREATE LEAGUE'}
                                                </button>
                                          </div>
                                    </div>

                                    {/* Prawy panel */}
                                    <div className='flex flex-1 flex-col lg:p-2'>
                                          {/* Header z napisem i searchbarem */}
                                          <div className='flex shrink-0 flex-col items-center justify-between lg:mb-4 lg:flex-row'>
                                                <h2 className='order-1 pt-4 text-2xl font-bold lg:order-first lg:pt-0'>Your Leagues</h2>
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
                                                            className='border-box input placeholder:text-quiz-white w-full pl-10!'
                                                      />
                                                </div>
                                          </div>

                                          {/* Lista lig z przewijaniem */}
                                          <div className='relative h-full w-full flex-1'>
                                                <div className='lg:scrollbar inset-0 pe-2 lg:absolute lg:overflow-auto'>
                                                      {filtered.map((league) => (
                                                            <div
                                                                  key={league.league_id}
                                                                  className='bg-quiz-white border-s-quiz-dark-green mt-2 flex w-full items-center justify-between rounded-lg border border-s-16 p-4 lg:border-none'
                                                            >
                                                                  <NavLink to={`${league.league_id}/quizzes`}>
                                                                        <div className='text-2xl'>{league.league_name}</div>
                                                                        <div className='text-quiz-light-green text-sm'>{league.description}</div>
                                                                  </NavLink>

                                                                  <div className='flex items-center gap-2'>
                                                                        <button className='button'>EDIT</button>
                                                                  </div>
                                                            </div>
                                                      ))}
                                                      {filtered.length === 0 && <NoContent title='No leagues yet' description='It looks like there are no leagues.'></NoContent>}
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
