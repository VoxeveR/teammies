import Navbar from '../components/general/Navbar.tsx';

const inputStyle = 'w-full p-2 border bg-quiz-green text-quiz-white rounded-md mb-2 rounded-xl';
const buttonStyle = 'xd';

function LoginPage() {
      return (
            <div className='flex h-full max-h-screen w-full flex-col overflow-hidden'>
                  <Navbar></Navbar>
                  <div className='bg-quiz-white flex h-full w-full flex-col pt-15 lg:mx-auto lg:my-auto lg:h-2/3 lg:w-1/2 lg:justify-center lg:rounded-xl lg:bg-none'>
                        <div className='flex w-full flex-col items-center justify-center gap-1'>
                              <div className='text-[40px]'>Welcome back!</div>
                              <div className='text-quiz-light-green pb-4 text-[16px]'>Please, enter your details to sign in.</div>
                        </div>
                        <form className='mx-auto mt-2 flex w-4/5 flex-col gap-2'>
                              <label className='text-quiz-green text-[16px]'>Email</label>
                              <input type='text' placeholder='Enter your email' className='bg-quiz-green text-quiz-white mb-2 w-full rounded-xl p-2'></input>
                              <label className='text-quiz-green text-[16px]'>Password</label>
                              <input type='password' placeholder='Enter your password' className={inputStyle}></input>
                              <div className='flex items-center justify-center'>Forgot your password?</div>
                              <button type='submit' className='bg-quiz-dark-green text-quiz-white w-full rounded-xl p-3'>
                                    LOGIN
                              </button>
                        </form>
                        <div>
                              <div className='mt-4 text-center'>
                                    Don't have an account?{' '}
                                    <a href='/register' className='text-quiz-light-green underline'>
                                          Register here
                                    </a>
                              </div>
                        </div>
                  </div>
            </div>
      );
}

export default LoginPage;
