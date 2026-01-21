import { useTranslation } from "react-i18next";
import Container from "../components/Container";

function Step({ title, text }: { title: string; text: string }) {
    return (
        <div
            className="rounded-2xl border p-6 transition
             hover:-translate-y-1 hover:scale-[1.01]
             hover:shadow-[0_20px_60px_rgba(15,23,42,0.12)]"
            style={{
                background: "var(--color-surface-strong)",
                borderColor: "var(--color-border)",
                boxShadow: "var(--shadow-card)",
                borderRadius: "var(--radius-card)",
            }}
        >
            <div className="text-base font-semibold">{title}</div>
            <div className="mt-2 text-sm opacity-80">{text}</div>
        </div>
    );
}

export default function HowItWorksSection() {
    const { t } = useTranslation();

    return (
        <section id="how" className="scroll-mt-24 py-16">
            <Container>
                <h2 className="text-3xl font-extrabold">{t("how.title")}</h2>

                <div className="mt-8 grid gap-6 md:grid-cols-3">
                    <Step title={t("how.step1_title")} text={t("how.step1_text")} />
                    <Step title={t("how.step2_title")} text={t("how.step2_text")} />
                    <Step title={t("how.step3_title")} text={t("how.step3_text")} />
                </div>
            </Container>
        </section>
    );
}
