import { createContext, useState, useContext } from 'react';
import axios from 'axios';

interface AuthContextType {
      accessToken: string;
      setAccessToken: (token: string) => void;
      login: (username: string, password: string) => Promise<{ success: boolean; message?: string }>;
      isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
      const [accessToken, setAccessToken] = useState('');
      let isAuthenticated = false;

      async function login(username: string, password: string): Promise<{ success: boolean; message?: string }> {
            return axios
                  .post(
                        'http://localhost:8080/api/auth/login',
                        {
                              email: username,
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
                              setAccessToken(accessToken);
                              isAuthenticated = true;
                              return { success: true };
                        }
                        return { success: false, message: 'Login failed' };
                  })
                  .catch((error) => {
                        console.log(error + ' cos sie zesralo');
                        return { success: false, message: 'Invalid username or password' };
                  });
      }

      return <AuthContext.Provider value={{ accessToken, setAccessToken, login, isAuthenticated }}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
      const context = useContext(AuthContext);
      if (!context) {
            throw new Error('useAuth must be used within an AuthProvider');
      }
      return context;
};
