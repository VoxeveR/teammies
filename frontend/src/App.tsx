import { Link, Outlet } from 'react-router-dom';

function App() {
      return (
            <div className='min-h-screen bg-gray-50'>
                  <header className='border-b bg-white'>
                        <div className='mx-auto flex max-w-4xl items-center justify-between px-4 py-4'>
                              <h1 className='text-xl font-bold'>Teammies</h1>
                              <nav className='flex gap-3'>
                                    <Link to='/' className='text-sm text-gray-700'>
                                          Home
                                    </Link>
                                    <Link to='/quiz' className='text-sm text-gray-700'>
                                          Quiz
                                    </Link>
                                    <Link to='/join' className='text-sm text-gray-700'>
                                          Join
                                    </Link>
                                    <Link to='/error-test' className='text-sm text-gray-700'>
                                          Error test
                                    </Link>
                              </nav>
                        </div>
                  </header>

                  <main className='mx-auto max-w-4xl px-4 py-8'>
                        <div className='h-64 w-full rounded border bg-white p-6'>
                              <h2 className='text-lg font-semibold'>Welcome to hell</h2>
                              <p className='text-sm text-gray-600'>Use the navigation to open the quiz or other pages.</p>
                        </div>

                        {/* This is important: nested route children (like /quiz) will render here */}
                        <div className='mt-6'>
                              <Outlet />
                        </div>
                  </main>
            </div>
      );
}

export default App;
