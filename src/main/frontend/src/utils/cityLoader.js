/**
 * City Loader
 * Fetches city suggestions from the backend (same-origin)
 */

import { sankalpamApi } from '../services/api';

export const getCitySuggestions = async (input) => {
  if (!input || input.trim().length === 0) {
    return [];
  }

  try {
    const response = await sankalpamApi.getCitySuggestions(input);
    // Handle response based on what the backend returns
    const cities = Array.isArray(response) ? response : response.cities || [];
    return cities;
  } catch (error) {
    return [];
  }
};
