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
            const accessToken = localStorage.getItem('access_token');
            const tokenType = localStorage.getItem('access_token_type');

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

                  isRefreshing = true;

                  try {
                        const r = await axios.post<JwtResponseDto>(`${API_BASE}${REFRESH_PATH}`, null, {
                              withCredentials: true,
                              headers: { 'Content-Type': 'application/json' },
                        });

                        localStorage.setItem('access_token', r.data.access_token);
                        localStorage.setItem('access_token_type', r.data.access_token_type);

                        notifySubscribers();
                        originalConfig.__isRetryRequest = true;
                        return instance(originalConfig);
                  } catch (refreshErr) {
                        notifySubscribers();

                        //TODO: IMPLEMENT
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
