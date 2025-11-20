import { useState } from 'react';
import Navbar from '../components/general/Navbar.tsx';
import { useAuth } from '../hooks/useAuth.tsx';
import { useNavigate } from 'react-router-dom';

function RegisterPage() {
      const [username, setUsername] = useState('');
      const [email, setEmail] = useState('');
      const [password, setPassword] = useState('');
      const [confirmPassword, setConfirmPassword] = useState('');
      const [error, setError] = useState('');
      const [isLoading, setIsLoading] = useState(false);

      const auth = useAuth();
      const navigate = useNavigate();

      async function register(e: React.FormEvent<HTMLButtonElement>) {
            e.preventDefault();
            setError('');

            if (!username.trim() || !email.trim() || !password.trim() || !confirmPassword.trim()) {
                  setError('All fields must be filled');
                  return;
            }

            if (password !== confirmPassword) {
                  setError('Passwords do not match');
                  return;
            }

            // Możesz dodać walidację emaila:
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(email)) {
                  setError('Invalid email address');
                  return;
            }

            setIsLoading(true);

            try {
                  const result = await auth.register(email, username, password);
                  if (result.success) {
                        navigate('/login', { state: { registered: true } });
                  } else {
                        setError(result.message || 'Registration failed');
                  }
            } catch (err) {
                  setError('An unexpected error occurred!');
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
                              <div className='text-[40px]'>Registration</div>
                              <div className='text-quiz-light-green pb-5 text-[16px]'>Please, enter your details to sign up.</div>
                        </div>
                        <form className='mx-auto mt-2 flex w-4/5 flex-col gap-4 lg:w-3/5'>
                              <div className='flex flex-col gap-1'>
                                    <label className='text-quiz-green text-[16px]'>Username</label>
                                    <input type='text' placeholder='Enter your username' className='input mb-2' onChange={(e) => setUsername(e.target.value)}></input>
                              </div>
                              <div className='flex flex-col gap-1'>
                                    <label className='text-quiz-green text-[16px]'>Email address</label>
                                    <input type='text' placeholder='Enter your email' className='input mb-2' onChange={(e) => setEmail(e.target.value)}></input>
                              </div>
                              <div className='flex flex-col gap-1'>
                                    <label className='text-quiz-green text-[16px]'>Password</label>
                                    <input type='password' placeholder='Enter your password' className='input mb-2' onChange={(e) => setPassword(e.target.value)}></input>
                              </div>
                              <div className='flex flex-col gap-1'>
                                    <label className='text-quiz-green text-[16px]'>Confirm password</label>
                                    <input type='password' placeholder='Confirm your password' className='input mb-2' onChange={(e) => setConfirmPassword(e.target.value)}></input>
                              </div>
                              <button type='submit' className='button pt-5' disabled={isLoading} onClick={register}>
                                    {isLoading ? 'Loading...' : 'Register'}
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
