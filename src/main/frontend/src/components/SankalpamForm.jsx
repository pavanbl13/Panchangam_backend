import { useState, useCallback, useRef } from 'react';
import { sankalpamApi } from '../services/api';
import { useSankalpamValidation } from '../hooks/useSankalpamValidation';
import { getCitySuggestions } from '../utils/cityLoader';
import SuccessPanel from './SuccessPanel';

const INITIAL_FORM = {
  date: '',
  time: '',
  city: '',
};

// Helper to format current date and time
const getCurrentDateTime = () => {
  const now = new Date();
  const date = now.toISOString().split('T')[0]; // YYYY-MM-DD
  const time = now.toTimeString().slice(0, 5); // HH:MM
  return { date, time };
};

export default function SankalpamForm() {
  const { date: currentDate, time: currentTime } = getCurrentDateTime();
  const [form, setForm] = useState({ ...INITIAL_FORM, date: currentDate, time: currentTime });
  const [submitting, setSubmitting] = useState(false);
  const [submission, setSubmission] = useState(null);
  const [apiError, setApiError] = useState(null);
  const [cityMatches, setCityMatches] = useState([]);
  const [isLoadingCities, setIsLoadingCities] = useState(false);
  const citiesTimeoutRef = useRef(null);

  const { errors, validate, setServerErrors, clearError, clearAllErrors } = useSankalpamValidation();

  // Handle city input with debounced search
  const handleCityChange = useCallback((e) => {
    const { value } = e.target;
    setForm((prev) => ({ ...prev, city: value }));
    clearError('city');
    setApiError(null);

    // Clear previous timeout
    if (citiesTimeoutRef.current) {
      clearTimeout(citiesTimeoutRef.current);
    }

    // Only search if input has 4 or more characters
    if (value.trim().length >= 4) {
      setIsLoadingCities(true);
      citiesTimeoutRef.current = setTimeout(async () => {
        try {
          const suggestions = await getCitySuggestions(value);
          setCityMatches(suggestions);
        } catch (error) {
          setCityMatches([]);
        } finally {
          setIsLoadingCities(false);
        }
      }, 300); // 300ms debounce delay
    } else {
      setCityMatches([]);
      setIsLoadingCities(false);
    }
  }, [clearError]);

  const handleChange = useCallback((e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    clearError(name);
    setApiError(null);
  }, [clearError]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    clearAllErrors();
    setApiError(null);

    if (!validate(form)) {
      document.querySelector('[aria-invalid="true"]')?.focus();
      return;
    }

    setSubmitting(true);
    try {
      const result = await sankalpamApi.submit(form);
      setSubmission(result);
    } catch (err) {
      if (err.validationErrors) {
        setServerErrors(err.validationErrors);
      } else {
        setApiError(err.message || 'Submission failed. Please try again.');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleReset = () => {
    const { date, time } = getCurrentDateTime();
    setForm({ ...INITIAL_FORM, date, time });
    clearAllErrors();
    setApiError(null);
    setSubmission(null);
    setCityMatches([]);
    if (citiesTimeoutRef.current) {
      clearTimeout(citiesTimeoutRef.current);
    }
  };

  if (submission) {
    return <SuccessPanel submission={submission} formData={form} onReset={handleReset} />;
  }

  return (
    <form onSubmit={handleSubmit} noValidate aria-label="Sankalpam Submission Form" className="sankalpam-form-container">
      <div className="form-header">
        <h1 className="form-title">
          <span className="title-icon">🙏</span>
          Sankalpam Finder
        </h1>
        <p className="form-subtitle">Find the perfect Sankalpam date, time and location</p>
      </div>

      {/* ── API error banner ── */}
      {apiError && (
        <div className="api-error-banner" role="alert">
          <span className="error-icon">⚠️</span>
          {apiError}
        </div>
      )}

      {/* ── Main Form Section ── */}
      <section className="form-section main-form">
        <div className="form-fields-container">
          {/* Date Field */}
          <div className="form-field">
            <label htmlFor="date" className="field-label">
              Date <span className="required-mark">*</span>
            </label>
            <input
              type="date"
              id="date"
              name="date"
              value={form.date}
              onChange={handleChange}
              className={`field-input${errors.date ? ' field-error' : ''}`}
              required
              aria-invalid={!!errors.date}
              aria-describedby={errors.date ? 'date-error' : undefined}
            />
            {errors.date && (
              <span id="date-error" className="error-msg" role="alert">{errors.date}</span>
            )}
          </div>

          {/* Time Field */}
          <div className="form-field">
            <label htmlFor="time" className="field-label">
              Time <span className="required-mark">*</span>
            </label>
            <input
              type="time"
              id="time"
              name="time"
              value={form.time}
              onChange={handleChange}
              className={`field-input${errors.time ? ' field-error' : ''}`}
              required
              aria-invalid={!!errors.time}
              aria-describedby={errors.time ? 'time-error' : undefined}
            />
            {errors.time && (
              <span id="time-error" className="error-msg" role="alert">{errors.time}</span>
            )}
          </div>

          {/* City Field with Backend Autocomplete */}
          <div className="form-field city-field">
            <label htmlFor="city" className="field-label">
              City <span className="required-mark">*</span>
            </label>
            <div className="city-input-wrapper">
              <input
                type="text"
                id="city"
                name="city"
                value={form.city}
                onChange={handleCityChange}
                placeholder="Search or type city name"
                className={`field-input${errors.city ? ' field-error' : ''}`}
                required
                aria-invalid={!!errors.city}
                aria-describedby={errors.city ? 'city-error' : undefined}
                autoComplete="off"
              />
              {isLoadingCities && (
                <div className="city-loading">
                  <span className="spinner-mini" aria-hidden="true" />
                  Searching...
                </div>
              )}
              {cityMatches.length > 0 && !isLoadingCities && (
                <div className="city-suggestions">
                  {cityMatches.map((city, index) => (
                    <div
                      key={`${city}-${index}`}
                      className="suggestion-item"
                      onClick={() => {
                        setForm((prev) => ({ ...prev, city }));
                        setCityMatches([]);
                      }}
                    >
                      📍 {city}
                    </div>
                  ))}
                </div>
              )}
            </div>
            {errors.city && (
              <span id="city-error" className="error-msg" role="alert">{errors.city}</span>
            )}
          </div>
        </div>
      </section>

      {/* ── Submit Button ── */}
      <div className="form-submit-row">
        <button
          type="submit"
          className="btn-primary btn-find-sankalpam"
          disabled={submitting}
          aria-busy={submitting}
        >
          {submitting ? (
            <><span className="spinner" aria-hidden="true" /> Finding Sankalpam…</>
          ) : (
            'Find Sankalpam 🔍'
          )}
        </button>
      </div>
    </form>
  );
}
