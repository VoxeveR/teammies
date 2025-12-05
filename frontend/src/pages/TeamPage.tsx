import Navbar from '../components/general/Navbar';

function TeamPage() {
      return (
            <div className='flex min-h-screen flex-col lg:h-screen lg:items-center'>
                  <Navbar></Navbar>
                  <div className='bg-quiz-white flex flex-1 flex-col justify-center lg:items-center lg:justify-center lg:bg-transparent'>
                        <div className='bg-quiz-white/75 flex h-fit w-full flex-col gap-8 lg:flex-row lg:rounded-xl lg:p-6'>
                              <div className='h-fit w-full ps-4 pe-4'>
                                    <div className='bg-quiz-white flex flex-col justify-center gap-3 rounded-2xl border p-5 pt-2'>
                                          <div className='flex flex-row items-center'>
                                                <img src='/src/assets/team.svg'></img>
                                                <div className='grow text-[40px]'>Join team</div>
                                          </div>
                                          <div className='text-quiz-light-green -mt-3 text-center'>Use the invite code from the team captain.</div>
                                          <input className='input' placeholder='Enter invite code'></input>
                                          <button className='button'>JOIN</button>
                                    </div>
                              </div>
                              <div className='h-fit w-full ps-4 pe-4'>
                                    <div className='bg-quiz-white flex flex-col justify-center gap-3 rounded-2xl border p-5 pt-2'>
                                          <div className='flex grow flex-row items-center'>
                                                <img src='/src/assets/guy.svg'></img>
                                                <div className='grow text-[40px]'>Create team</div>
                                          </div>
                                          <div className='text-quiz-light-green -mt-3 text-center'>Become the captain!</div>
                                          <input className='input' placeholder='Enter team name'></input>
                                          <button className='button'>CREATE</button>
                                    </div>
                              </div>
                        </div>
                  </div>
            </div>
      );
}

export default TeamPage;
