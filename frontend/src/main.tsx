import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './index.css';
import App from './App.tsx';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import QuizPage from './pages/QuizPage.tsx';
import QuizResultsPage from './pages/QuizResultsPage.tsx';
import ErrorPage from './pages/ErrorPage.tsx';
import { Suspense } from 'react';
import React from 'react';
import WaitingPage from './pages/WaitingPage.tsx';
import LoginPage from './pages/LoginPage.tsx';
import RegisterPage from './pages/RegisterPage.tsx';
import TeamPage from './pages/TeamPage.tsx';
import LeagueManagementPage from './pages/LeagueManagementPage.tsx';
import { AuthProvider } from './hooks/useAuth.tsx';
import { Toaster } from 'react-hot-toast';
import AuthenticatedRoute from './middleware/AuthenticatedRoute.tsx';
import QuizManagementPage from './pages/QuizManagementPage.tsx';
import AdminWaitingPage from './pages/AdminWaitingPage.tsx';

const QuizJoinPage = React.lazy(() => import('./pages/QuizJoinPage.tsx'));

const router = createBrowserRouter([
      {
            errorElement: <ErrorPage />,
            children: [
                  {
                        element: <AuthenticatedRoute />,
                        children: [
                              {
                                    path: 'leagues',
                                    element: (
                                          <>
                                                <LeagueManagementPage></LeagueManagementPage>
                                          </>
                                    ),
                              },
                              {
                                    path: 'leagues/:leagueId/quizzes',
                                    element: <QuizManagementPage />,
                              },
                        ],
                  },

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
                        path: 'quiz/:sessionCode/:teamCode/:playerId',
                        element: <QuizPage />,
                  },
                  {
                        path: 'quiz-results/:sessionCode',
                        element: <QuizResultsPage />,
                  },
                  {
                        path: 'waiting-for-start/:sessionCode',
                        element: <AdminWaitingPage />,
                  },
                  {
                        path: 'join',
                        element: <QuizJoinPage />,
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
                        path: 'quiz/:sessionCode/:teamCode/waiting-for-start',
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
                  {
                        path: 'quiz/:sessionCode/team-join',
                        element: <TeamPage></TeamPage>,
                  },
            ],
      },
]);

createRoot(document.getElementById('root')!).render(
      <StrictMode>
            <AuthProvider>
                  <Toaster position='top-center' />
                  <RouterProvider router={router} />
            </AuthProvider>
      </StrictMode>
);
