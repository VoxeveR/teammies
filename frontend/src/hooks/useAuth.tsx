import { createContext, useState, useContext } from 'react';
import axios from 'axios';

interface AuthContextType {
      accessToken: string;
      setAccessToken: (token: string) => void;
      login: (email: string, password: string) => Promise<{ success: boolean; message?: string }>;
      register: (email: string, username: string, password: string) => Promise<{ success: boolean; message?: string }>;
      isAuthenticated: boolean;
      logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
      const [accessToken, setAccessToken] = useState(() => {
            return sessionStorage.getItem('access_token') || '';
      });
      const [isAuthenticated, setIsAuthenticated] = useState(() => {
            return !!sessionStorage.getItem('access_token');
      });

      async function login(email: string, password: string): Promise<{ success: boolean; message?: string }> {
            return axios
                  .post(
                        'http://localhost:8080/api/auth/login',
                        {
                              email: email,
                              password: password,
                        },
                        {
                              withCredentials: true,
                        }
                  )
                  .then((response) => {
                        if (response.status === 200) {
                              const accessToken = response.data.access_token;
                              sessionStorage.setItem('access_token', accessToken);
                              sessionStorage.setItem('access_token_type', response.data.access_token_type);
                              sessionStorage.setItem('isAuthenticated', 'true');
                              setAccessToken(accessToken);
                              setIsAuthenticated(true);
                              return { success: true };
                        } else if (response.status === 401) {
                              return { success: false, message: 'Invalid username or password!' };
                        }
                        return { success: false, message: 'Login failed!' };
                  })
                  .catch((error) => {
                        console.log(error);
                        return { success: false, message: `Can't connect to server! Try again later.` };
                  });
      }

      async function register(email: string, username: string, password: string): Promise<{ success: boolean; message?: string }> {
            return axios
                  .post(
                        'http://localhost:8080/api/auth/register',
                        {
                              email: email,
                              username: username,
                              password: password,
                        },
                        {
                              withCredentials: true,
                        }
                  )
                  .then((response) => {
                        if (response.status === 200) {
                              return { success: true };
                        } else if (response.status === 422) {
                              return { success: false, message: response.data.message };
                        }
                        return { success: false, message: 'Registration failed!' };
                  })
                  .catch((error) => {
                        if (error.response) {
                              console.log(error.response.status);

                              if (error.response.status === 422 || error.response.status === 409) {
                                    return {
                                          success: false,
                                          message: error.response.data.message,
                                    };
                              }
                        }

                        console.log(error);
                        return {
                              success: false,
                              message: `Can't connect to server! Try again later.`,
                        };
                  });
      }

      async function logout(): Promise<void> {
            try {
                  await axios.post(
                        'http://localhost:8080/api/auth/logout',
                        {},
                        { withCredentials: true } // needed for refresh token cookie
                  );
            } catch (e) {
                  console.log('Server logout failed, clearing client anyway.');
            }

            sessionStorage.removeItem('access_token');
            sessionStorage.removeItem('access_token_type');
            sessionStorage.removeItem('isAuthenticated');

            setAccessToken('');
            setIsAuthenticated(false);
      }

      return <AuthContext.Provider value={{ accessToken, setAccessToken, login, register, isAuthenticated, logout }}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
      const context = useContext(AuthContext);
      if (!context) {
            throw new Error('useAuth must be used within an AuthProvider');
      }
      return context;
};
