export default function FormField({
  label, name, value, onChange, error,
  type = 'text', placeholder, required, autoComplete,
}) {
  const id = `field-${name}`;
  const errorId = `${id}-error`;
  return (
    <div className="form-field">
      <label htmlFor={id} className="field-label">
        {label}
        {required && <span className="required-mark"> *</span>}
      </label>
      <input
        id={id} name={name} type={type} value={value}
        onChange={onChange} placeholder={placeholder}
        autoComplete={autoComplete}
        className={`field-input${error ? ' field-error' : ''}`}
        required={required}
        aria-invalid={!!error}
        aria-describedby={error ? errorId : undefined}
      />
      {error && (
        <span id={errorId} className="error-msg" role="alert">{error}</span>
      )}
    </div>
  );
}
