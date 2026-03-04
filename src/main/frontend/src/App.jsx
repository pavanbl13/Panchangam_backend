import SankalpamForm from './components/SankalpamForm';
import gayatriDeviImg from './assets/Gayatri-Dev-pic.jpg';
import samskritQuotes from './data/SamskritQuotes.json';
import './styles/global.css';

// Computed once at module load time — no recalculation on re-renders
const currentMonth = new Date().toLocaleString('en-US', { month: 'long' });
const footerQuote = samskritQuotes[currentMonth] || 'ॐ';

export default function App() {
  return (
    <div className="page-wrapper">
      <header className="site-header">
        <div className="header-inner">
          <div className="header-emblem" aria-hidden="true">ॐ</div>
          <div className="header-text">
            <h1 className="header-title">Telugu Sankalpam</h1>
            <span className="header-tagline">Sacred Intention Declaration</span>
          </div>
          <div className="gayatri-devi-img" aria-hidden="true">
            <img 
              src={gayatriDeviImg}
              alt="Gayatri Devi" 
              title="Gayatri Devi"
            />
          </div>
        </div>
        <div className="header-mantra">ॐ श्रीमात्रे नमः</div>
        <div className="header-divider" aria-hidden="true">
          ❖ ❖ ❖
        </div>
      </header>

      <main className="main-content" id="main">
        <div className="form-intro">
          <p>
            A Sankalpam is a sacred declaration of intent made before any Hindu ritual or prayer.
            It specifies <em>who</em> is performing the act, <em>when</em> and <em>where</em>,
            and <em>for what purpose</em> — anchoring the action in auspicious time and space.
          </p>
          <p className="telugu-text">
            "సంకల్పం అంటే ఏదైనా హిందూ ఆచారం లేదా ప్రార్థనకు ముందు చేసే ఒక పవిత్రమైన నిశ్చయం. 
            ఇది ఆ క్రతువును ఎవరు చేస్తున్నారు, ఎప్పుడు, ఎక్కడ మరియు ఏ ఉద్దేశ్యంతో చేస్తున్నారు 
            అనే విషయాలను స్పష్టం చేస్తుంది — తద్వారా ఆ కార్యాన్ని ఒక శుభప్రదమైన కాలంలో మరియు ప్రదేశంలో సుస్థిరం చేస్తుంది."
          </p>
        </div>
        <SankalpamForm />
      </main>

      <footer className="site-footer">
        <p className="footer-verse">
          "{footerQuote}"
        </p>
        <p className="footer-copyright">
          ® Pavan&amp;I Vedic Services Private Limited 2026. All rights reserved.
        </p>
      </footer>
    </div>
  );
}
