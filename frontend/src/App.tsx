import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Navbar from './components/general/Navbar';
import FeatureCard from './components/general/FeatureCard';
import { faCog, faMedal, faPeopleGroup } from '@fortawesome/free-solid-svg-icons';
import { NavLink } from 'react-router-dom';

function App() {
      return (
            <div className='flex min-h-screen flex-col'>
                  <Navbar></Navbar>
                  <div className='bg-quiz-white flex h-full w-full flex-col gap-3 p-6 lg:m-10 lg:h-full lg:w-9/10 lg:flex-1 lg:self-center lg:rounded-3xl lg:p-20'>
                        <div className='text-quiz-dark-green text-center font-[Bungee] text-3xl lg:text-6xl'>THE ULTIMATE TEAM QUIZ EXPERIENCE</div>
                        <div className='text-quiz-green text-center text-lg lg:text-3xl'>Engage your team with fun, interactive and live-hosted quizzes.</div>
                        <NavLink to='/join' className='button text-3xl lg:w-1/3! lg:self-center lg:text-4xl'>
                              JOIN GAME NOW!
                        </NavLink>
                        <div className='flex flex-row gap-2 lg:hidden'>
                              <NavLink to='/register' className='button'>
                                    Become a host
                              </NavLink>
                              <NavLink to='/login' className='secondaryButton'>
                                    Host Login
                              </NavLink>
                        </div>
                        <div className='relative h-64 lg:flex lg:flex-1 lg:items-center lg:justify-center lg:gap-0 lg:rounded-2xl'>
                              <img src='/images/mobile.svg' className='absolute bottom-10 -left-2 h-3/4 lg:static lg:h-auto lg:w-[20%] lg:object-contain'></img>
                              <img src='/images/laptop.svg' className='absolute bottom-0 left-1/4 h-full object-contain lg:static lg:h-auto lg:w-[35%] lg:object-contain'></img>
                        </div>
                        <div className='-mt-8 flex w-full flex-col justify-around gap-2 lg:flex-row'>
                              <FeatureCard icon={faMedal} title='Live leaderboards' description='Watch the competition heat-up in real-time.' />
                              <FeatureCard icon={faPeopleGroup} title='Collaborate & Compete' description='Work together with your team to find the right answer.' />
                              <FeatureCard icon={faCog} title='Easy to host' description='Launch a game in seconds from any device.' />
                        </div>
                  </div>
            </div>
      );
}

export default App;
