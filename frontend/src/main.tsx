import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.tsx';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import Quiz from './pages/Quiz.tsx';
//import QuizJoin from './pages/QuizJoin.tsx';
import ErrorPage from './pages/ErrorPage.tsx';
import Navbar from './components/general/Navbar.tsx';
import { Suspense } from 'react';
import React from 'react';

const QuizJoin = React.lazy(() => import('./pages/QuizJoin.tsx'));

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
                        element: <Quiz />,
                  },
                  {
                        path: 'join',
                        element: <QuizJoin />,
                  },
                  {
                        path: 'navbar',
                        element: <Navbar elements={exampleNavbar} />,
                  },
                  {
                        path: 'test-suspense',
                        element: <Suspense fallback={<div>Loading...</div>}><QuizJoin /></Suspense>
                  }
            ],
      },
]);

createRoot(document.getElementById('root')!).render(
            <StrictMode>
                  <RouterProvider router={router} />
            </StrictMode>
);
