import Navbar from '../components/general/Navbar.tsx';

function RegisterPage() {
      return (
            <div className='flex h-screen max-h-screen w-full flex-col overflow-auto'>
                  <Navbar showMobile={false}></Navbar>
                  <div className='bg-quiz-white flex h-full min-h-fit w-full flex-col pt-15 lg:mx-auto lg:my-auto lg:h-fit lg:w-1/4 lg:min-w-lg lg:rounded-xl lg:bg-none lg:pt-8 lg:pb-8 xl:w-1/5'>
                        <div className='flex w-full flex-col items-center justify-center gap-1'>
                              <div className='hidden items-center lg:flex lg:flex-col'>
                                    <img src='/src/assets/logo.svg' className='lg:h-48 lg:w-48' />
                                    <div className='font-[Bungee] text-xl'>TEAMMIES</div>
                              </div>
                              <div className='text-[40px]'>Registration</div>
                              <div className='text-quiz-light-green pb-5 text-[16px]'>Please, enter your details to sign up.</div>
                        </div>
                        <form className='mx-auto mt-2 flex w-4/5 flex-col gap-4 lg:w-3/5'>
                              <div className='flex flex-col gap-1'>
                                    <label className='text-quiz-green text-[16px]'>Username</label>
                                    <input type='text' placeholder='Enter your username' className='input mb-2'></input>
                              </div>
                              <div className='flex flex-col gap-1'>
                                    <label className='text-quiz-green text-[16px]'>Email address</label>
                                    <input type='text' placeholder='Enter your email' className='input mb-2'></input>
                              </div>
                              <div className='flex flex-col gap-1'>
                                    <label className='text-quiz-green text-[16px]'>Password</label>
                                    <input type='password' placeholder='Enter your password' className='input mb-2'></input>
                              </div>
                              <div className='flex flex-col gap-1'>
                                    <label className='text-quiz-green text-[16px]'>Confirm password</label>
                                    <input type='password' placeholder='Confirm your password' className='input mb-2'></input>
                              </div>
                              <button type='submit' className='button pt-5'>
                                    REGISTER
                              </button>
                        </form>
                        <div>
                              <div className='mt-4 pb-5 text-center'>
                                    Already have an account?{' '}
                                    <a href='/login' className='text-quiz-light-green underline'>
                                          Login here
                                    </a>
                              </div>
                        </div>
                  </div>
            </div>
      );
}

export default RegisterPage;
