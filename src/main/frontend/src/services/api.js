import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1/sankalpam',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'X-Requested-With': 'XMLHttpRequest',
  },
});

// Request interceptor — attach request ID
api.interceptors.request.use((config) => {
  config.headers['X-Request-ID'] = crypto.randomUUID();
  return config;
});

// Response interceptor — normalize errors
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message =
      error.response?.data?.message ||
      (error.response?.status === 422
        ? 'Validation failed. Please check the form.'
        : 'Server error. Please try again later.');

    const validationErrors = error.response?.data?.errors || null;
    return Promise.reject({ message, validationErrors, status: error.response?.status });
  }
);

export const sankalpamApi = {
  getMetadata: () => api.get('/metadata'),
  submit: (payload) => api.post('/find', payload),
  healthCheck: () => api.get('/health'),
  getCitySuggestions: (input) => api.get('/cities', { params: { q: input } }),
};
