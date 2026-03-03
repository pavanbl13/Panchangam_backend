export default function SuccessPanel({ submission, formData, onReset }) {
  const { data, message } = submission;

  // Extract panchanga data from the flat structure returned by the backend
  const panchanga = {
    samvatsaram: data.samvatsaram,
    ayanam: data.ayanam,
    ruthu: data.ruthuvu,
    masam: data.maasam,
    paksham: data.paksham,
    tithi: data.tithi,
    vaasaram: data.vaaram,
    nakshatram: data.nakshatram,
  };

  // Format date as ordinal (e.g., "24th Feb 2026")
  const formatDateOrdinal = (dateString) => {
    const date = new Date(dateString);
    const day = date.getDate();
    const month = date.toLocaleDateString('en-US', { month: 'long' });
    const year = date.getFullYear();
    
    const ordinal = (n) => {
      if (n > 3 && n < 21) return 'th';
      switch (n % 10) {
        case 1: return 'st';
        case 2: return 'nd';
        case 3: return 'rd';
        default: return 'th';
      }
    };
    
    return `${day}${ordinal(day)} ${month} ${year}`;
  };

  // Format time from HH:MM:SS to 12-hour format
  const formatValidUntilTime = (timeString) => {
    if (!timeString) return 'N/A';
    
    // If already in 12-hour format with AM/PM, return as is
    if (timeString.includes('AM') || timeString.includes('PM')) {
      return timeString;
    }
    
    // Otherwise parse HH:MM or HH:MM:SS format
    const timeParts = timeString.split(':');
    const hour = parseInt(timeParts[0]);
    const minutes = timeParts[1];
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const displayHour = hour % 12 || 12;
    return `${displayHour.toString().padStart(2, '0')}:${minutes} ${ampm}`;
  };

  // Format time from HH:MM to 12-hour format with timezone
  const formatTime12Hour = (timeString) => {
    const [hours, minutes] = timeString.split(':');
    const hour = parseInt(hours);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const displayHour = hour % 12 || 12;
    const tz = Intl.DateTimeFormat().resolvedOptions().timeZone;
    return `${displayHour.toString().padStart(2, '0')}:${minutes} ${ampm} ${tz}`;
  };

  return (
    <div className="success-panel" role="status" aria-live="polite">
      <div className="success-glyph" aria-hidden="true">🕉️</div>
      
      {/* Main Sankalpam Content */}
      {panchanga && (
        <div className="sankalpam-container">
          <div className="sankalpam-verse-box">
            <p className="sankalpam-main-text">
              Sankalpam for <strong>{formData.city}</strong> on <strong>{formatDateOrdinal(formData.date)}</strong> 
              <br />
              At <strong>{formatTime12Hour(formData.time)}</strong> and valid through <strong>{formatValidUntilTime(data.validUntil)}</strong>
            </p>
            
            <div className="panchanga-verse">
              <p className="panchanga-line">
                <strong>{panchanga.samvatsaram}</strong> nAma samvathsarE, <strong>{panchanga.ayanam}</strong>,
              </p>
              <p className="panchanga-line">
                <strong>{panchanga.ruthu}</strong> rithou, <strong>{panchanga.masam}</strong> mAsE,
              </p>
              <p className="panchanga-line">
                <strong>{panchanga.paksham}</strong> pakshE, <strong>{panchanga.tithi}</strong> shubha thithou,
              </p>
              <p className="panchanga-line">
                <strong>{panchanga.vaasaram}</strong> vAsara, <strong>{panchanga.nakshatram}</strong> nakshatra yukthAyAm.
              </p>
            </div>
          </div>

          {/* Celestial Times */}
          <div className="celestial-times">
            {data.sunrise && (
              <div className="celestial-item">
                <span className="celestial-icon">🌅</span>
                <div className="celestial-content">
                  <span className="celestial-label">Sunrise</span>
                  <span className="celestial-time">{data.sunrise}</span>
                </div>
              </div>
            )}
            {data.sunset && (
              <div className="celestial-item">
                <span className="celestial-icon">🌇</span>
                <div className="celestial-content">
                  <span className="celestial-label">Sunset</span>
                  <span className="celestial-time">{data.sunset}</span>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      <button className="btn-primary" onClick={onReset}>
        Find Another Sankalpam
      </button>
    </div>
  );
}
