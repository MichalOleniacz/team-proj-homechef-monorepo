import { useState } from "react";
import type { SubmitUrlResponse } from "../types/recipe";
import { parseRecipe } from "../api/recipes";

export function useSubmitUrl() {
    const [data, setData] = useState<SubmitUrlResponse | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const run = async (url: string) => {
        setLoading(true);
        setError(null);
        try {
            const res = await parseRecipe({ url });
            setData(res);
            return res;
        } catch (e: any) {
            setError(e?.message ?? "Request failed");
            setData(null);
            throw e;
        } finally {
            setLoading(false);
        }
    };

    return { data, loading, error, run, setData };
}
