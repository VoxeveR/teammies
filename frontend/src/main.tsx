import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.tsx';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import QuizPage from './pages/QuizPage.tsx';
//import QuizJoin from './pages/QuizJoin.tsx';
import ErrorPage from './pages/ErrorPage.tsx';
import Navbar from './components/general/Navbar.tsx';
import { Suspense } from 'react';
import React from 'react';
import WaitingPage from './pages/WaitingPage.tsx';
import LoginPage from './pages/LoginPage.tsx';
import RegisterPage from './pages/RegisterPage.tsx';

const QuizJoinPage = React.lazy(() => import('./pages/QuizJoinPage.tsx'));

const exampleNavbar = [
      { label: 'Main', linkTo: '/' },
      { label: 'Quiz', linkTo: '/quiz' },
];

const router = createBrowserRouter([
      {
            errorElement: <ErrorPage />,
            children: [
                  {
                        path: '/',
                        element: <App />,
                  },
                  {
                        path: 'error-throw',
                        loader: async () => {
                              throw new Response('Forbidden', { status: 403, statusText: 'Forbidden' });
                        },
                  },
                  {
                        path: 'quiz',
                        element: <QuizPage />,
                  },
                  {
                        path: 'join',
                        element: <QuizJoinPage />,
                  },
                  {
                        path: 'navbar',
                        element: <Navbar elements={exampleNavbar} />,
                  },
                  {
                        path: 'test-suspense',
                        element: (
                              <Suspense fallback={<div>Loading...</div>}>
                                    <QuizJoinPage />
                              </Suspense>
                        ),
                  },
                  {
                        path: 'waiting',
                        element: <WaitingPage></WaitingPage>,
                  },
                  {
                        path: 'login',
                        element: <LoginPage></LoginPage>,
                  },
                  {
                        path: 'register',
                        element: <RegisterPage></RegisterPage>,
                  },
            ],
      },
]);

createRoot(document.getElementById('root')!).render(
      <StrictMode>
            <RouterProvider router={router} />
      </StrictMode>
);
