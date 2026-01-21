import { useEffect, useState } from "react";
import { disableTransitionsTemporarily } from "../utils/disableTransitions";

type Theme = "light" | "dark";

function getInitialTheme(): Theme {
    const saved = localStorage.getItem("theme");
    if (saved === "dark" || saved === "light") return saved;

    const prefersDark = window.matchMedia?.("(prefers-color-scheme: dark)")?.matches;
    return prefersDark ? "dark" : "light";
}

export function useTheme() {
    const [theme, setTheme] = useState<Theme>(() => getInitialTheme());

    useEffect(() => {
        const root = document.documentElement;

        disableTransitionsTemporarily();

        if (theme === "dark") root.classList.add("dark");
        else root.classList.remove("dark");

        localStorage.setItem("theme", theme);
    }, [theme]);

    const toggle = () => {
        setTheme((t) => (t === "dark" ? "light" : "dark"));
    };

    return { theme, setTheme, toggle };
}
