import i18n from "i18next";
import { initReactI18next } from "react-i18next";

import pl from "./locales/pl/translation.json";
import en from "./locales/en/translation.json";

const savedLang = localStorage.getItem("lang");
const initialLang = savedLang === "en" || savedLang === "pl" ? savedLang : "pl";

i18n.use(initReactI18next).init({
    resources: {
        pl: { translation: pl },
        en: { translation: en }
    },
    lng: initialLang,
    fallbackLng: "en",
    supportedLngs: ["pl", "en"],
    interpolation: {
        escapeValue: false
    }
});

export default i18n;
