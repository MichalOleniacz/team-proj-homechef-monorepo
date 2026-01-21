import { useState, type FormEvent } from "react";
import { useTranslation } from "react-i18next";
import Container from "../components/Container";

export default function ContactSection() {
    const { t } = useTranslation();
    const [email, setEmail] = useState("");
    const [msg, setMsg] = useState("");

    const onSubmit = (e: FormEvent) => {
        e.preventDefault();
        alert("Demo: formularz nie wysyła. Podepniesz API później 🙂");
        setEmail("");
        setMsg("");
    };

    return (
        <section id="contact" className="scroll-mt-24 py-16">
            <Container>
                <div className="grid gap-8 md:grid-cols-2">
                    <div>
                        <h2 className="text-3xl font-extrabold">{t("contact.title")}</h2>
                        <p className="mt-3 text-sm opacity-80">{t("contact.text")}</p>
                        <p className="mt-6 text-sm opacity-70">{t("contact.note")}</p>
                    </div>

                    <form
                        onSubmit={onSubmit}
                        className="rounded-2xl border p-6"
                        style={{
                            background: "var(--color-surface)",
                            borderColor: "var(--color-border)",
                            boxShadow: "var(--shadow-card)",
                            borderRadius: "var(--radius-card)"
                        }}
                    >
                        <label className="block text-sm font-semibold">
                            {t("contact.emailLabel")}
                        </label>
                        <input
                            className="mt-2 w-full rounded-2xl border px-4 py-3 text-sm outline-none focus:ring-2"
                            style={{
                                background: "var(--color-bg)",
                                borderColor: "var(--color-border)"
                            }}
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            type="email"
                            required
                        />

                        <label className="mt-5 block text-sm font-semibold">
                            {t("contact.messageLabel")}
                        </label>
                        <textarea
                            className="mt-2 w-full rounded-2xl border px-4 py-3 text-sm outline-none focus:ring-2"
                            style={{
                                background: "var(--color-bg)",
                                borderColor: "var(--color-border)"
                            }}
                            value={msg}
                            onChange={(e) => setMsg(e.target.value)}
                            rows={5}
                            required
                        />

                        <button
                            type="submit"
                            className="mt-5 w-full rounded-2xl px-5 py-3 text-sm font-semibold transition hover:opacity-95"
                            style={{
                                background: "var(--color-primary)",
                                color: "#001018"
                            }}
                        >
                            {t("contact.send")}
                        </button>
                    </form>
                </div>
            </Container>
        </section>
    );
}
