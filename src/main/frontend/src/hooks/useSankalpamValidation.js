import { useState, useCallback } from 'react';

const REQUIRED_FIELDS = ['date', 'time', 'city'];

export function useSankalpamValidation() {
  const [errors, setErrors] = useState({});

  const validate = useCallback((form) => {
    const newErrors = {};

    // Required fields
    REQUIRED_FIELDS.forEach((field) => {
      if (!form[field]?.trim()) {
        newErrors[field] = 'This field is required';
      }
    });

    // Validate date format
    if (form.date && !/^\d{4}-\d{2}-\d{2}$/.test(form.date)) {
      newErrors.date = 'Please select a valid date';
    }

    // Validate time format
    if (form.time && !/^\d{2}:\d{2}$/.test(form.time)) {
      newErrors.time = 'Please select a valid time';
    }

    // Validate city (at least 2 characters)
    if (form.city && form.city.trim().length < 2) {
      newErrors.city = 'Please enter a valid city name';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }, []);

  // Merge server-side validation errors into local errors state
  const setServerErrors = useCallback((serverErrors) => {
    if (!serverErrors) return;
    const merged = {};
    Object.entries(serverErrors).forEach(([field, messages]) => {
      merged[field] = Array.isArray(messages) ? messages[0] : messages;
    });
    setErrors((prev) => ({ ...prev, ...merged }));
  }, []);

  const clearError = useCallback((field) => {
    setErrors((prev) => {
      const next = { ...prev };
      delete next[field];
      return next;
    });
  }, []);

  const clearAllErrors = useCallback(() => setErrors({}), []);

  return { errors, validate, setServerErrors, clearError, clearAllErrors };
}
