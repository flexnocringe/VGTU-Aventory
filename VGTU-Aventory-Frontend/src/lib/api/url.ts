export function apiUrl(path: string): string {
    const base = process.env.NEXT_PUBLIC_API_BASE_URL;

    if (!base) {
        throw new Error("NEXT_PUBLIC_API_BASE_URL is not set");
    }

    const normalizedBase = base.endsWith("/") ? base.slice(0, -1) : base;
    const normalizedPath = path.startsWith("/") ? path : `/${path}`;

    return `${normalizedBase}${normalizedPath}`;
}