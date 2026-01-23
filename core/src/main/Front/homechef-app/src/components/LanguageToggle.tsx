import { useTranslation } from "react-i18next";
import { disableTransitionsTemporarily } from "../utils/disableTransitions";

export default function LanguageToggle() {
    const { i18n } = useTranslation();
    const lang = i18n.language === "en" ? "en" : "pl";

    const setLang = (next: "pl" | "en") => {
        if (next === lang) return;

        disableTransitionsTemporarily();

        i18n.changeLanguage(next);
        localStorage.setItem("lang", next);
    };

    return (
        <div className="flex items-center gap-2 whitespace-nowrap">
            <div
                className="toggle-wrap inline-flex rounded-full border px-1 py-1"
                style={{
                    borderColor: "var(--color-border)",
                    background: "var(--color-surface-strong)",
                    boxShadow: "var(--shadow-soft)",
                }}
            >
                <span
                    className={`toggle-indicator ${lang === "en" ? "is-right" : ""}`}
                    aria-hidden="true"
                />

                <button
                    onClick={() => setLang("pl")}
                    className={`toggle-btn px-3 py-1 text-sm rounded-full transition
            ${lang === "pl" ? "font-semibold" : "opacity-70 hover:opacity-100"}`}
                >
                    PL
                </button>

                <button
                    onClick={() => setLang("en")}
                    className={`toggle-btn px-3 py-1 text-sm rounded-full transition
            ${lang === "en" ? "font-semibold" : "opacity-70 hover:opacity-100"}`}
                >
                    EN
                </button>
            </div>
        </div>
    );
}
