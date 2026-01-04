import { useEffect, useMemo, useState } from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faMagnifyingGlass } from '@fortawesome/free-solid-svg-icons';
import Navbar from '../components/general/Navbar';
import api from '../middleware/api';
import toast from 'react-hot-toast';
import NoContent from '../components/management/NoContent';
import { NavLink } from 'react-router-dom';
import DatepickerWrapper from '../components/management/DatepickerWrapper';

interface League {
      league_id: number;
      league_name: string;
      description: string;
      team_size: number;
      max_teams: number;
      is_public: boolean;
      owner_id: number;
      start_date: string; // ISO date string, e.g., "2025-12-04"
      end_date: string; // ISO date string, e.g., "2025-12-10"
}

interface AllLeaguesResponse {
      my_leagues: League[];
      public_leagues: League[];
}

function LeagueManagementPage() {
      const [myQuery, setMyQuery] = useState('');
      const [publicQuery, setPublicQuery] = useState('');
      const [myLeaguesData, setMyLeaguesData] = useState<League[]>([]);
      const [publicLeaguesData, setPublicLeaguesData] = useState<League[]>([]);
      const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);

      //Form data
      const [leagueName, setLeagueName] = useState('');
      const [description, setDescription] = useState('');
      const [teamSize, setTeamSize] = useState('');
      const [maxTeams, setMaxTeams] = useState('');
      const [isPublic, setIsPublic] = useState('private');
      const [startDate, setStartDate] = useState<Date | null>();
      const [endDate, setEndDate] = useState<Date | null>();

      // View data
      const [isLoading, setIsLoading] = useState(false);
      const [showForMobile, setShowFormMobile] = useState(false);
      const [editLeague, setEditLeague] = useState<League | null>(null);

      const fetchLeagues = () => {
            const token = localStorage.getItem('access_token');
            setIsAuthenticated(!!token);

            api.get<AllLeaguesResponse>('/leagues/')
                  .then((res) => {
                        setMyLeaguesData(res.data.my_leagues);
                        setPublicLeaguesData(res.data.public_leagues);
                        console.log(res.data);
                  })
                  .catch((err) => console.error(err));
      };

      useEffect(() => {
            fetchLeagues();
      }, []);

      const handleCreateLeague = async () => {
            if (!leagueName.trim() || !description.trim() || !teamSize || !maxTeams || !startDate || !endDate) {
                  toast.error('Please fill in all required fields');
                  return;
            }

            if (endDate <= startDate) {
                  toast.error('End date must be after start date');
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
                  await api.post('/leagues/', {
                        league_name: leagueName,
                        description,
                        team_size: teamSizeNum,
                        max_teams: maxTeamsNum,
                        is_public: isPublic === 'public',
                        start_date: startDate.toISOString().split('T')[0], // send as yyyy-mm-dd
                        end_date: endDate.toISOString().split('T')[0],
                  });

                  toast.success('League created successfully!');
                  clearLeague();
                  fetchLeagues();
            } catch (error: any) {
                  const errorMessage = error.response?.data?.message || 'Failed to create league';
                  toast.error(errorMessage);
                  console.error(error);
            } finally {
                  setIsLoading(false);
            }
      };

      const handleSaveEdit = async () => {
            if (!editLeague) return;

            if (!leagueName.trim() || !description.trim() || !teamSize || !maxTeams || !startDate || !endDate) {
                  toast.error('Please fill in all required fields');
                  return;
            }

            if (endDate <= startDate) {
                  toast.error('End date must be after start date');
                  return;
            }

            try {
                  await api.put(`/leagues/${editLeague.league_id}`, {
                        league_name: leagueName,
                        description,
                        team_size: parseInt(teamSize),
                        max_teams: parseInt(maxTeams),
                        is_public: isPublic === 'public',
                        start_date: startDate.toISOString().split('T')[0],
                        end_date: endDate.toISOString().split('T')[0],
                  });

                  toast.success('League updated successfully!');
                  clearLeague();
                  fetchLeagues();
            } catch (error) {
                  toast.error('Failed to update league');
                  console.error(error);
            }
      };

      async function handleDeleteLeague(id: number) {
            const confirmed = confirm('Are you sure you want to delete this league?');
            if (!confirmed) return;

            try {
                  await api.delete(`/leagues/${id}`);
                  toast.success('League deleted');
                  fetchLeagues();
            } catch {
                  toast.error('Failed to delete league');
            }
      }

      const filteredMyLeagues = useMemo(() => {
            const q = myQuery.trim().toLowerCase();
            if (!q) return myLeaguesData;
            return myLeaguesData.filter((l) => l.league_name.toLowerCase().includes(q));
      }, [myLeaguesData, myQuery]);

      const filteredPublicLeagues = useMemo(() => {
            const q = publicQuery.trim().toLowerCase();
            if (!q) return publicLeaguesData;
            return publicLeaguesData.filter((l) => l.league_name.toLowerCase().includes(q));
      }, [publicLeaguesData, publicQuery]);

      function clearLeague() {
            setEditLeague(null);
            setLeagueName('');
            setDescription('');
            setTeamSize('');
            setMaxTeams('');
            setIsPublic('private');
            setStartDate(null);
            setEndDate(null);
      }

      function startLeagueEditing(league: League) {
            setEditLeague(league);
            setLeagueName(league.league_name);
            setDescription(league.description);
            setTeamSize(String(league.team_size));
            setMaxTeams(String(league.max_teams));
            setIsPublic(league.is_public ? 'public' : 'private');
            setStartDate(league.start_date ? new Date(league.start_date) : null);
            setEndDate(league.end_date ? new Date(league.end_date) : null);
      }

      return (
            <div className='flex h-fit flex-col lg:h-screen'>
                  <Navbar />

                  <div className='flex h-fit w-full items-center justify-center lg:h-full'>
                        <div className='bg-quiz-white lg:bg-quiz-white/75 flex h-fit min-h-screen w-full flex-col gap-3 p-4 lg:h-[90%] lg:min-h-fit lg:w-4/5 lg:gap-6 lg:rounded-xl lg:p-10'>
                              <div className='text-center text-4xl lg:text-left lg:text-6xl'>LEAGUE MANAGEMENT</div>
                              {isAuthenticated && (
                                    <button className='button lg:hidden' onClick={() => setShowFormMobile((prev) => !prev)}>
                                          {showForMobile ? 'HIDE FORM' : 'CREATE NEW LEAGUE'}
                                    </button>
                              )}

                              <div className='flex flex-1 flex-col gap-6 lg:flex-row'>
                                    {/* Lewy panel - tylko dla zalogowanych */}
                                    {isAuthenticated && (
                                          <div className={`bg-quiz-white flex flex-col gap-4 rounded-xl p-4 lg:w-3/10 ${showForMobile ? 'flex' : 'hidden'} lg:flex`}>
                                                <div className='text-quiz-green text-3xl'>{editLeague ? `Update League` : 'Create a new league'} </div>
                                                <div>
                                                      <div className='text-quiz-light-green text-xl'>League name</div>
                                                      <input className='input' placeholder='League name' value={leagueName} onChange={(e) => setLeagueName(e.target.value)}></input>
                                                </div>

                                                <div className='flex flex-1 flex-col'>
                                                      <div className='text-quiz-light-green text-xl'>League description</div>
                                                      <textarea
                                                            className='input h-full resize-none align-text-top'
                                                            placeholder='Provide a brief description of your league'
                                                            value={description}
                                                            onChange={(e) => setDescription(e.target.value)}
                                                      ></textarea>
                                                </div>
                                                <div className='flex w-full flex-row gap-2'>
                                                      <div className='w-full'>
                                                            <div className='text-quiz-light-green text-xl'>Start date</div>
                                                            <DatepickerWrapper
                                                                  key={startDate?.toISOString() ?? 'start'}
                                                                  value={startDate}
                                                                  onChange={(date) => setStartDate(date)}
                                                                  placeholder='Select start date'
                                                            />
                                                      </div>
                                                      <div className='w-full'>
                                                            <div className='text-quiz-light-green text-xl'>End date</div>
                                                            <DatepickerWrapper
                                                                  key={startDate?.toISOString() ?? 'start'}
                                                                  value={endDate}
                                                                  onChange={(date) => setEndDate(date)}
                                                                  placeholder='Select end date'
                                                            />
                                                      </div>
                                                </div>
                                                <div className='flex w-full flex-row gap-2'>
                                                      <div className='w-full'>
                                                            <div className='text-quiz-light-green text-xl'>Team size</div>
                                                            <input
                                                                  className='input w-full!'
                                                                  placeholder='Team size'
                                                                  type='number'
                                                                  value={teamSize}
                                                                  onChange={(e) => setTeamSize(e.target.value)}
                                                            ></input>
                                                      </div>
                                                      <div className='w-full'>
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

                                                <div className='flex w-full items-center justify-center gap-4'>
                                                      {editLeague && (
                                                            <button className='secondaryButton w-[44%]!' onClick={() => clearLeague()}>
                                                                  Cancel
                                                            </button>
                                                      )}

                                                      <button className='button disabled:opacity-50' onClick={editLeague ? handleSaveEdit : handleCreateLeague} disabled={isLoading}>
                                                            {editLeague ? 'Update' : isLoading ? 'Creating...' : 'Create League'}
                                                      </button>
                                                </div>
                                          </div>
                                    )}

                                    {/* Prawy panel */}
                                    <div className='flex flex-1 flex-col gap-6 lg:p-2'>
                                          {/* Sekcja 1: Twoje ligi - tylko dla zalogowanych */}
                                          {isAuthenticated && (
                                                <div className='flex flex-1 flex-col'>
                                                      {/* Header z napisem i searchbarem */}
                                                      <div className='flex shrink-0 flex-col items-center justify-between lg:mb-4 lg:flex-row'>
                                                            <h2 className='order-1 pt-4 text-2xl font-bold lg:order-first lg:pt-0'>Your Leagues</h2>
                                                            <div className='relative w-full max-w-sm items-center'>
                                                                  <label htmlFor='my-league-search' className='sr-only'>
                                                                        Search your leagues
                                                                  </label>
                                                                  <FontAwesomeIcon icon={faMagnifyingGlass} className={'text-quiz-white absolute top-1/2 left-3 -translate-y-1/2'} />
                                                                  <input
                                                                        id='my-league-search'
                                                                        type='text'
                                                                        value={myQuery}
                                                                        onChange={(e) => setMyQuery(e.target.value)}
                                                                        placeholder='Search your leagues...'
                                                                        className='border-box input placeholder:text-quiz-white w-full pl-10!'
                                                                  />
                                                            </div>
                                                      </div>

                                                      {/* Lista lig z przewijaniem */}
                                                      <div className='relative h-full w-full flex-1'>
                                                            <div className='lg:scrollbar inset-0 pe-2 lg:absolute lg:overflow-auto'>
                                                                  {filteredMyLeagues.map((league) => (
                                                                        <div
                                                                              key={league.league_id}
                                                                              className='bg-quiz-white border-s-quiz-dark-green mt-2 flex w-full items-center justify-between rounded-lg border border-s-16 p-4 lg:border-none'
                                                                        >
                                                                              <NavLink to={`${league.league_id}/quizzes`}>
                                                                                    <div className='text-2xl'>{league.league_name}</div>
                                                                                    <div className='text-quiz-light-green text-sm'>{league.description}</div>
                                                                              </NavLink>

                                                                              <div className='flex flex-col items-center gap-2 lg:flex-row'>
                                                                                    <button className='button lg:w-30!' onClick={() => startLeagueEditing(league)}>
                                                                                          EDIT
                                                                                    </button>
                                                                                    <button className='secondaryButton lg:w-30!' onClick={() => handleDeleteLeague(league.league_id)}>
                                                                                          DELETE
                                                                                    </button>
                                                                              </div>
                                                                        </div>
                                                                  ))}
                                                                  {filteredMyLeagues.length === 0 && (
                                                                        <NoContent
                                                                              title={myLeaguesData.length === 0 ? 'No leagues yet' : 'No leagues found'}
                                                                              description={myLeaguesData.length === 0 ? 'It looks like there are no leagues.' : 'Change your search criteria'}
                                                                        ></NoContent>
                                                                  )}
                                                            </div>
                                                      </div>
                                                </div>
                                          )}

                                          {/* Sekcja 2: Ligi publiczne */}
                                          <div className='flex flex-1 flex-col'>
                                                {/* Header z napisem i searchbarem */}
                                                <div className='flex shrink-0 flex-col items-center justify-between lg:mb-4 lg:flex-row'>
                                                      <h2 className='order-1 pt-4 text-2xl font-bold lg:order-first lg:pt-0'>Public Leagues</h2>
                                                      <div className='relative w-full max-w-sm items-center'>
                                                            <label htmlFor='public-league-search' className='sr-only'>
                                                                  Search public leagues
                                                            </label>
                                                            <FontAwesomeIcon icon={faMagnifyingGlass} className={'text-quiz-white absolute top-1/2 left-3 -translate-y-1/2'} />
                                                            <input
                                                                  id='public-league-search'
                                                                  type='text'
                                                                  value={publicQuery}
                                                                  onChange={(e) => setPublicQuery(e.target.value)}
                                                                  placeholder='Search public leagues...'
                                                                  className='border-box input placeholder:text-quiz-white w-full pl-10!'
                                                            />
                                                      </div>
                                                </div>

                                                {/* Lista lig z przewijaniem */}
                                                <div className='relative h-full w-full flex-1'>
                                                      <div className='lg:scrollbar inset-0 pe-2 lg:absolute lg:overflow-auto'>
                                                            {filteredPublicLeagues.map((league) => (
                                                                  <div
                                                                        key={league.league_id}
                                                                        className='bg-quiz-white border-s-quiz-dark-green mt-2 flex w-full items-center justify-between rounded-lg border border-s-16 p-4 lg:border-none'
                                                                  >
                                                                        <NavLink to={`${league.league_id}/quizzes`}>
                                                                              <div className='text-2xl'>{league.league_name}</div>
                                                                              <div className='text-quiz-light-green text-sm'>{league.description}</div>
                                                                        </NavLink>

                                                                        <div className='flex flex-col items-center gap-2 lg:flex-row'>
                                                                              <span className='text-quiz-green text-sm font-semibold'>Public</span>
                                                                        </div>
                                                                  </div>
                                                            ))}
                                                            {filteredPublicLeagues.length === 0 && (
                                                                  <NoContent
                                                                        title={publicLeaguesData.length === 0 ? 'No public leagues' : 'No leagues found'}
                                                                        description={publicLeaguesData.length === 0 ? 'There are no public leagues available.' : 'Change your search criteria'}
                                                                  ></NoContent>
                                                            )}
                                                      </div>
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
