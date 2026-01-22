import { useEffect, useRef, useState } from "react";
import { getParseRequest, parseRecipe } from "../api/recipes";
import type { ParseStatusResponse, SubmitUrlResponse } from "../types/recipe";

type Phase = "idle" | "submitting" | "polling" | "done" | "error";
type AnyResponse = SubmitUrlResponse | ParseStatusResponse;

export function useParseRecipe() {
    const [data, setData] = useState<AnyResponse | null>(null);
    const [phase, setPhase] = useState<Phase>("idle");
    const [error, setError] = useState<string | null>(null);

    const timerRef = useRef<number | null>(null);

    const clearTimers = () => {
        if (timerRef.current) {
            window.clearTimeout(timerRef.current);
            timerRef.current = null;
        }
    };

    const cancel = () => {
        clearTimers();
        setPhase("idle");
    };

    useEffect(() => () => cancel(), []);

    const run = async (url: string) => {
        cancel();
        setError(null);
        setData(null);
        setPhase("submitting");

        try {
            const initial = await parseRecipe({ url });
            setData(initial);

            if (initial.status === "COMPLETED") {
                setPhase("done");
                return initial;
            }

            if (initial.status === "FAILED") {
                setPhase("error");
                setError("Parsing failed");
                return initial;
            }

            setPhase("polling");

            const requestId = initial.requestId;
            const startedAt = Date.now();
            const timeoutMs = 60_000;
            const intervalMs = 1_000;

            const poll = async (): Promise<ParseStatusResponse> => {
                if (Date.now() - startedAt > timeoutMs) {
                    setPhase("error");
                    setError("Timeout: parsing took too long.");
                    throw new Error("Timeout");
                }

                const res = await getParseRequest(requestId);
                setData(res);

                if (res.status === "COMPLETED") {
                    setPhase("done");
                    return res;
                }

                if (res.status === "FAILED") {
                    setPhase("error");
                    setError(res.error ?? "Parsing failed");
                    return res;
                }

                return new Promise((resolve, reject) => {
                    timerRef.current = window.setTimeout(() => {
                        poll().then(resolve).catch(reject);
                    }, intervalMs);
                });
            };

            return await poll();
        } catch (e: any) {
            if (!error) setError(e?.message ?? "Request failed");
            setPhase("error");
            throw e;
        }
    };

    const reset = () => {
        cancel();
        setData(null);
        setError(null);
        setPhase("idle");
    };

    const loading = phase === "submitting" || phase === "polling";

    return { data, phase, loading, error, run, reset, cancel };
}
