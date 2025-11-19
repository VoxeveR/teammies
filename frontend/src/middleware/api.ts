import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios';

const API_BASE = 'http://localhost:8080/api';
const REFRESH_PATH = '/auth/refreshToken';

const instance = axios.create({
      baseURL: API_BASE,
      headers: {
            'Content-Type': 'application/json',
      },
      withCredentials: true,
});

instance.interceptors.request.use(
      (config) => {
            const accessToken = sessionStorage.getItem('access_token');
            const tokenType = sessionStorage.getItem('access_token_type');

            if (accessToken && tokenType) {
                  config.headers['Authorization'] = `${tokenType} ${accessToken}`;
            }
            console.log(config);
            return config;
      },
      (error) => {
            return Promise.reject(error);
      }
);

interface AxiosRequestConfigWithRetry extends AxiosRequestConfig {
      __isRetryRequest?: boolean;
}

let isRefreshing = false;
type Subscriber = () => void;
let subscribers: Subscriber[] = [];

export let logoutHandler: (() => void) | null = null;
export function setLogoutHandler(fn: () => void) {
      logoutHandler = fn;
}

function addSubscriber(cb: Subscriber) {
      subscribers.push(cb);
}

function notifySubscribers() {
      subscribers.forEach((cb) => cb());
      subscribers = [];
}

interface JwtResponseDto {
      access_token: string;
      access_token_type: string;
      access_token_expires_in: number;
}

// response interceptor obsługujący 401 i retry po refresh
instance.interceptors.response.use(
      (resp) => resp,
      async (error) => {
            const originalConfig = (error.config ?? {}) as AxiosRequestConfigWithRetry;
            const response = error.response;

            // brak configu lub brak response -> reject (network error itp.)
            if (!originalConfig || !response) return Promise.reject(error);

            // tylko 401 i jeśli nie retryowaliśmy już tego requestu
            if (response.status === 401 && !originalConfig.__isRetryRequest) {
                  // jeśli już ktoś robi refresh -> dołącz do kolejki i poczekaj
                  if (isRefreshing) {
                        return new Promise((resolve, reject) => {
                              addSubscriber(() => {
                                    originalConfig.__isRetryRequest = true;
                                    // retry - instancja ma withCredentials: true więc cookie zostanie dołączone
                                    resolve(instance(originalConfig));
                              });
                        });
                  }

                  // zaczynamy refresh
                  isRefreshing = true;

                  try {
                        // POST refresh — serwer powinien odczytać refresh cookie i ustawić nowe cookie
                        const r = await axios.post<JwtResponseDto>(`${API_BASE}${REFRESH_PATH}`, null, {
                              withCredentials: true,
                              headers: { 'Content-Type': 'application/json' },
                        });

                        sessionStorage.setItem('access_token', r.data.access_token);
                        sessionStorage.setItem('access_token_type', r.data.access_token_type);

                        // opcjonalnie możesz sprawdzić r.status / r.data jeśli backend zwraca info
                        // zakładamy, że jeśli nie rzuci erroru, refresh powiódł się i cookie zostały zaktualizowane

                        // powiadamiamy wszystkich oczekujących, żeby retry-owali swoje requesty
                        notifySubscribers();

                        // retry oryginalnego requestu
                        originalConfig.__isRetryRequest = true;
                        return instance(originalConfig);
                  } catch (refreshErr) {
                        // refresh nie powiódł się - czyścimy queue i wykonujemy logout if provided
                        notifySubscribers(); // powiadamiamy żeby requesty się zakończyły (one powinny złapać, że cookie dalej nie działa)
                        if (logoutHandler) logoutHandler();

                        return Promise.reject(refreshErr);
                  } finally {
                        isRefreshing = false;
                  }
            }

            return Promise.reject(error);
      }
);

export default instance;
