export default function SelectField({
  label, name, value, onChange, error,
  options, loading, required,
}) {
  const id = `field-${name}`;
  const errorId = `${id}-error`;
  return (
    <div className="form-field">
      <label htmlFor={id} className="field-label">
        {label}
        {required && <span className="required-mark"> *</span>}
      </label>
      <select
        id={id} name={name} value={value} onChange={onChange}
        className={`field-select${error ? ' field-error' : ''}`}
        required={required} disabled={loading}
        aria-invalid={!!error}
        aria-describedby={error ? errorId : undefined}
        aria-busy={loading}
      >
        <option value="">
          {loading ? 'Loading…' : `— Select ${label} —`}
        </option>
        {options?.map((opt) => (
          <option key={opt} value={opt}>{opt}</option>
        ))}
      </select>
      {error && (
        <span id={errorId} className="error-msg" role="alert">{error}</span>
      )}
    </div>
  );
}
