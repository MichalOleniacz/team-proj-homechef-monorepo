import Navbar from "./components/Navbar";
import HomeSection from "./sections/HomeSection";
import HowItWorksSection from "./sections/HowItWorksSection";
import AboutSection from "./sections/AboutSection";
import ContactSection from "./sections/ContactSection";
import Footer from "./sections/Footer";

export default function App() {
    return (
        <div
            style={{
                background: "var(--color-bg)",
                color: "var(--color-text)"
            }}
        >
            <Navbar />
            <HomeSection />
            <HowItWorksSection />
            <AboutSection />
            <ContactSection />
            <Footer />
        </div>
    );
}
