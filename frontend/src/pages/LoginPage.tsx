import { useEffect, useState } from 'react';
import Navbar from '../components/general/Navbar.tsx';
import { useAuth } from '../hooks/useAuth.tsx';
import { useLocation, useNavigate } from 'react-router';
import { toast } from 'react-hot-toast';

function LoginPage() {
      const [username, setUsername] = useState('');
      const [password, setPassword] = useState('');
      const [error, setError] = useState('');
      const [isLoading, setIsLoading] = useState(false);

      const auth = useAuth();
      const location = useLocation();
      const navigate = useNavigate();

      useEffect(() => {
            if (location.state?.registered) {
                  toast.success('Account created successfully! You can now log in.');
            }
            window.history.replaceState({}, '');
      }, [location]);

      async function login(e: React.FormEvent<HTMLButtonElement>) {
            e.preventDefault();
            setIsLoading(true);
            try {
                  const result = await auth.login(username, password);
                  if (result.success) {
                        navigate('/leagues');
                  } else {
                        setError(result.message || 'Login failed');
                  }
            } catch (err) {
                  setError('An unexpected error occured!');
            } finally {
                  setIsLoading(false);
            }
      }

      return (
            <div className='flex h-screen max-h-screen w-full flex-col overflow-auto'>
                  <Navbar showMobile={false}></Navbar>
                  <div className='bg-quiz-white flex h-full min-h-fit w-full flex-col lg:mx-auto lg:my-auto lg:h-fit lg:w-1/4 lg:min-w-lg lg:rounded-xl lg:bg-none lg:pb-8 xl:w-1/5'>
                        {error && (
                              <div className='flex h-fit w-full items-center justify-center bg-red-500 text-center text-xl lg:rounded-t-xl'>
                                    {' '}
                                    <div className='flex-1 text-center'>{error}</div>{' '}
                                    <div className='cursor-pointer pe-2' onClick={() => setError('')}>
                                          {' '}
                                          X{' '}
                                    </div>{' '}
                              </div>
                        )}

                        <div className='flex w-full flex-col items-center justify-center gap-1 pt-15 lg:pt-8'>
                              <div className='hidden items-center lg:flex lg:flex-col'>
                                    <img src='/src/assets/logo.svg' className='lg:h-48 lg:w-48' />
                                    <div className='font-[Bungee] text-xl'>TEAMMIES</div>
                              </div>
                              <div className='text-[40px]'>Welcome back!</div>
                              <div className='text-quiz-light-green pb-5 text-[16px]'>Please, enter your details to sign in.</div>
                        </div>

                        <form className='mx-auto mt-2 flex w-4/5 flex-col gap-2 lg:w-3/5'>
                              <label className='text-quiz-green text-[16px]'>Username</label>
                              <input type='text' placeholder='Enter your username' className='input mb-2' onChange={(e) => setUsername(e.target.value)}></input>
                              <label className='text-quiz-green text-[16px]'>Password</label>
                              <input type='password' placeholder='Enter your password' className='input mb-2' onChange={(e) => setPassword(e.target.value)}></input>
                              <div className='flex items-center justify-center pt-5'>Forgot your password?</div>
                              <button type='submit' className='button' onClick={login} disabled={isLoading}>
                                    {isLoading ? 'Logging in...' : 'LOGIN'}
                              </button>
                        </form>
                        <div>
                              <div className='mt-4 pb-5 text-center'>
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
