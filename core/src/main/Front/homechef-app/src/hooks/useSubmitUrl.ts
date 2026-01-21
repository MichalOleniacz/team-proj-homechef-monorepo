import { useState } from "react";
import type { RecipeResponseDto } from "../types/api";
import { submitUrl } from "../api/recipes";

export function useSubmitUrl() {
    const [data, setData] = useState<RecipeResponseDto | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const run = async (url: string) => {
        setLoading(true);
        setError(null);
        try {
            const res = await submitUrl({ url });
            setData(res);
            return res;
        } catch (e: any) {
            const msg =
                (e?.name === "ApiError" && e?.message) ||
                null;
            setError(msg);
            setData(null);
            throw e;
        } finally {
            setLoading(false);
        }
    };

    return { data, loading, error, run, setData };
}
