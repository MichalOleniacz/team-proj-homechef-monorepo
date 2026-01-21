export default function RecipeSkeleton() {
    return (
        <div
            className="rounded-2xl border p-5"
            style={{
                background: "var(--color-surface-strong)",
                borderColor: "var(--color-border)",
                boxShadow: "var(--shadow-card)",
                borderRadius: "var(--radius-card)",
            }}
        >
            <div className="flex items-start justify-between gap-4">
                <div className="w-full">
                    <div className="h-5 w-2/3 rounded-lg shimmer" />
                    <div className="mt-2 h-3 w-full rounded-lg shimmer" />
                </div>
                <div className="h-9 w-20 rounded-xl shimmer" />
            </div>

            <div className="mt-6 grid gap-5 lg:grid-cols-2">
                <div>
                    <div className="h-4 w-28 rounded-lg shimmer" />
                    <div className="mt-3 space-y-2">
                        <div className="h-3 w-11/12 rounded-lg shimmer" />
                        <div className="h-3 w-10/12 rounded-lg shimmer" />
                        <div className="h-3 w-9/12 rounded-lg shimmer" />
                        <div className="h-3 w-10/12 rounded-lg shimmer" />
                    </div>
                </div>

                <div>
                    <div className="h-4 w-20 rounded-lg shimmer" />
                    <div className="mt-3 space-y-2">
                        <div className="h-3 w-full rounded-lg shimmer" />
                        <div className="h-3 w-10/12 rounded-lg shimmer" />
                        <div className="h-3 w-11/12 rounded-lg shimmer" />
                        <div className="h-3 w-9/12 rounded-lg shimmer" />
                    </div>
                </div>
            </div>
        </div>
    );
}
