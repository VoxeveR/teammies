import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.tsx';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import Quiz from './pages/Quiz.tsx';
import QuizJoin from './pages/QuizJoin.tsx';
import ErrorPage from './pages/ErrorPage.tsx';
import Navbar from './components/general/Navbar.tsx';

const exampleNavbar = [
      { label: 'dupa', linkTo: '/' },
      { label: 'dupa2', linkTo: '/quiz' },
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
            ],
      },
]);

createRoot(document.getElementById('root')!).render(
      <StrictMode>
            <RouterProvider router={router} />
      </StrictMode>
);
