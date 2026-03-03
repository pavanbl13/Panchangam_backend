import SankalpamForm from './components/SankalpamForm';
import gayatriDeviImg from './assets/Gayatri-Dev-pic.jpg';
import './styles/global.css';

export default function App() {
  return (
    <div className="page-wrapper">
      <header className="site-header">
        <div className="header-inner">
          <div className="header-emblem" aria-hidden="true">ॐ</div>
          <div className="header-text">
            <h1 className="header-title">Sankalpam</h1>
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
        <div className="header-mantra">Om Shri Mātre Namah</div>
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
        </div>
        <SankalpamForm />
      </main>

      <footer className="site-footer">
        <p className="footer-verse">
          "सर्वे भवन्तु सुखिनः सर्वे सन्तु निरामयाः"
        </p>
      </footer>
    </div>
  );
}
