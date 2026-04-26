type InventoryMetricCardProps = {
    title: string;
    value: string | number;
    accentClassName: string;
    labelClassName: string;
    bgClassName: string;
    ringClassName?: string;
    valueClassName?: string;
    trend: "up" | "down";
};

export function InventoryMetricCard({
    title,
    value,
    accentClassName,
    labelClassName,
    bgClassName,
    ringClassName = "",
    valueClassName = "",
    trend,
}: InventoryMetricCardProps) {
    return (
        <article
            className={[
                "relative overflow-hidden rounded-[20px] border border-transparent text-white shadow-[0_18px_40px_rgba(45,36,24,0.18)]",
                bgClassName,
                ringClassName,
            ].join(" ")}
        >
            <div
                className={[
                    "pointer-events-none absolute -right-20 -top-20 h-52 w-52 rounded-full opacity-40",
                    accentClassName,
                ].join(" ")}
            />
            <div
                className={[
                    "pointer-events-none absolute -right-10 top-[-88px] h-52 w-52 rounded-full opacity-20",
                    accentClassName,
                ].join(" ")}
            />

            <div className="relative z-10 p-6">

                <div className="mt-5 flex items-end gap-3">
                    <div className={[
                        "text-[2.125rem] font-medium leading-none tracking-tight text-white",
                        valueClassName,
                    ].join(" ")}>
                        {value}
                    </div>
                    <div className="mb-1 flex h-7 w-7 items-center justify-center rounded-full bg-white/15 text-sm font-semibold text-white/90">
                        {trend === "up" ? "↗" : "↘"}
                    </div>
                </div>

                <p className={[
                    "mt-2 text-[1rem] font-medium",
                    labelClassName,
                ].join(" ")}>{title}</p>

                <div className="mt-4 h-px w-full bg-white/10" />

                <p className="mt-3 text-xs text-white/65">
                    Lorem ipsum perchance
                </p>
            </div>
        </article>
    );
}