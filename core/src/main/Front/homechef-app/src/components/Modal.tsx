import { useEffect } from "react";

export default function Modal({
                                  open,
                                  onClose,
                                  title,
                                  children,
                              }: {
    open: boolean;
    onClose: () => void;
    title: string;
    children: React.ReactNode;
}) {
    useEffect(() => {
        if (!open) return;

        const onKey = (e: KeyboardEvent) => {
            if (e.key === "Escape") onClose();
        };
        window.addEventListener("keydown", onKey);
        return () => window.removeEventListener("keydown", onKey);
    }, [open, onClose]);

    if (!open) return null;

    return (
        <div
            className="fixed inset-0 z-[100] flex items-center justify-center p-4"
            role="dialog"
            aria-modal="true"
        >
            <button
                className="absolute inset-0"
                onClick={onClose}
                aria-label="Close modal"
                style={{ background: "rgba(0,0,0,0.35)" }}
            />

            <div
                className="relative w-full max-w-md rounded-2xl border p-6 pop-in"
                style={{
                    background: "var(--color-surface-strong)",
                    borderColor: "var(--color-border)",
                    boxShadow: "var(--shadow-card)",
                    borderRadius: "var(--radius-card)",
                }}
            >
                <div className="flex items-start justify-between gap-4">
                    <div>
                        <div className="text-lg font-semibold">{title}</div>
                    </div>
                    <button
                        onClick={onClose}
                        className="rounded-xl border px-3 py-1 text-sm transition hover:-translate-y-[1px]"
                        style={{
                            borderColor: "var(--color-border)",
                            background: "var(--color-surface)",
                        }}
                    >
                        ✕
                    </button>
                </div>

                <div className="mt-4">{children}</div>
            </div>
        </div>
    );
}
