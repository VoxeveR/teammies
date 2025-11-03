import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.tsx';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import Quiz from './pages/Quiz.tsx';
import QuizJoin from './pages/QuizJoin.tsx';

const router = createBrowserRouter([
      {
            path: '/',
            element: <App />,
      },
      {
            path: '/quiz',
            element: <Quiz />,
      },
      {
            path: '/join',
            element: <QuizJoin />,
      },
]);

createRoot(document.getElementById('root')!).render(
      <StrictMode>
            <RouterProvider router={router} />
      </StrictMode>
);
