import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import Modal from "./Modal";
import { useAuth } from "../auth/AuthProvider";

type Mode = "login" | "register";

export default function AuthModal({
                                      open,
                                      onClose,
                                      defaultMode = "login",
                                  }: {
    open: boolean;
    onClose: () => void;
    defaultMode?: Mode;
}) {
    const { t } = useTranslation();
    const { login, register, error } = useAuth();

    const [mode, setMode] = useState<Mode>(defaultMode);

    const [email, setEmail] = useState("");
    const [pass, setPass] = useState("");
    const [pass2, setPass2] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [localError, setLocalError] = useState<string | null>(null);

    const title = useMemo(
        () => (mode === "login" ? t("auth.titleLogin") : t("auth.titleRegister")),
        [mode, t]
    );

    const reset = () => {
        setEmail("");
        setPass("");
        setPass2("");
        setLocalError(null);
        setSubmitting(false);
    };

    const close = () => {
        reset();
        onClose();
    };

    const onSubmit = async () => {
        setLocalError(null);

        if (!email.trim()) return setLocalError(t("auth.errors.emailRequired"));
        if (!pass) return setLocalError(t("auth.errors.passwordRequired"));

        if (mode === "register") {
            if (pass.length < 8) return setLocalError(t("auth.errors.passwordMin"));
            if (pass !== pass2) return setLocalError(t("auth.errors.passwordsMismatch"));
        }

        setSubmitting(true);
        try {
            if (mode === "login") {
                await login({ email, password: pass });
            } else {
                await register({ email, password: pass });
            }
            close();
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Modal open={open} onClose={close} title={title}>
            <div className="flex gap-2">
                <button
                    className={`flex-1 rounded-xl border px-3 py-2 text-sm transition ${
                        mode === "login" ? "font-semibold" : "opacity-70 hover:opacity-100"
                    }`}
                    style={{
                        borderColor: "var(--color-border)",
                        background: mode === "login" ? "var(--color-surface)" : "transparent",
                    }}
                    onClick={() => setMode("login")}
                    type="button"
                >
                    {t("auth.loginTab")}
                </button>

                <button
                    className={`flex-1 rounded-xl border px-3 py-2 text-sm transition ${
                        mode === "register" ? "font-semibold" : "opacity-70 hover:opacity-100"
                    }`}
                    style={{
                        borderColor: "var(--color-border)",
                        background: mode === "register" ? "var(--color-surface)" : "transparent",
                    }}
                    onClick={() => setMode("register")}
                    type="button"
                >
                    {t("auth.registerTab")}
                </button>
            </div>

            <div className="mt-4 space-y-3">
                <div>
                    <label className="block text-sm font-semibold">{t("auth.email")}</label>
                    <input
                        className="mt-2 w-full rounded-xl border px-4 py-3 text-sm outline-none focus:ring-2 transition"
                        style={{ background: "var(--color-bg)", borderColor: "var(--color-border)" }}
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        type="email"
                        autoComplete="email"
                    />
                </div>

                <div>
                    <label className="block text-sm font-semibold">{t("auth.password")}</label>
                    <input
                        className="mt-2 w-full rounded-xl border px-4 py-3 text-sm outline-none focus:ring-2 transition"
                        style={{ background: "var(--color-bg)", borderColor: "var(--color-border)" }}
                        value={pass}
                        onChange={(e) => setPass(e.target.value)}
                        type="password"
                        autoComplete={mode === "login" ? "current-password" : "new-password"}
                    />
                </div>

                {mode === "register" && (
                    <div>
                        <label className="block text-sm font-semibold">{t("auth.confirmPassword")}</label>
                        <input
                            className="mt-2 w-full rounded-xl border px-4 py-3 text-sm outline-none focus:ring-2 transition"
                            style={{ background: "var(--color-bg)", borderColor: "var(--color-border)" }}
                            value={pass2}
                            onChange={(e) => setPass2(e.target.value)}
                            type="password"
                            autoComplete="new-password"
                        />
                    </div>
                )}

                {(localError || error) && (
                    <div className="text-sm text-red-600">{localError || error}</div>
                )}

                <button
                    onClick={onSubmit}
                    disabled={submitting}
                    className="mt-2 w-full rounded-xl px-5 py-3 text-sm font-semibold transition
                     hover:-translate-y-[1px] active:translate-y-0 disabled:opacity-60"
                    style={{ background: "var(--color-primary)", color: "#0b1220", boxShadow: "var(--shadow-soft)" }}
                >
                    {submitting
                        ? t("auth.submitting")
                        : mode === "login"
                            ? t("auth.submitLogin")
                            : t("auth.submitRegister")}
                </button>
            </div>
        </Modal>
    );
}
