"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { dashboardMenu } from "@/config/menu/dashboardMenu";

type SidebarProps = {
    onNavigate?: () => void;
    mobileOpen?: boolean;
    isCollapsed?: boolean;
    onToggleCollapse?: () => void;
};

function DashboardIcon() {
    return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" className="h-5 w-5">
            <path d="M4 13.5V5.75C4 5.34 4.34 5 4.75 5H9.5C9.91 5 10.25 5.34 10.25 5.75V13.5C10.25 13.91 9.91 14.25 9.5 14.25H4.75C4.34 14.25 4 13.91 4 13.5Z" stroke="currentColor" strokeWidth="1.7" />
            <path d="M13.75 18.25V5.75C13.75 5.34 14.09 5 14.5 5H19.25C19.66 5 20 5.34 20 5.75V18.25C20 18.66 19.66 19 19.25 19H14.5C14.09 19 13.75 18.66 13.75 18.25Z" stroke="currentColor" strokeWidth="1.7" />
            <path d="M4 18.25V16.5C4 16.09 4.34 15.75 4.75 15.75H9.5C9.91 15.75 10.25 16.09 10.25 16.5V18.25C10.25 18.66 9.91 19 9.5 19H4.75C4.34 19 4 18.66 4 18.25Z" stroke="currentColor" strokeWidth="1.7" />
        </svg>
    );
}

function ProductsIcon() {
    return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" className="h-5 w-5">
            <path d="M7.25 7.5H16.75C17.44 7.5 18 8.06 18 8.75V17.25C18 17.94 17.44 18.5 16.75 18.5H7.25C6.56 18.5 6 17.94 6 17.25V8.75C6 8.06 6.56 7.5 7.25 7.5Z" stroke="currentColor" strokeWidth="1.7" />
            <path d="M9 7.5V6.5C9 5.12 10.12 4 11.5 4H12.5C13.88 4 15 5.12 15 6.5V7.5" stroke="currentColor" strokeWidth="1.7" />
            <path d="M9 11H15" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
            <path d="M9 14H15" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
        </svg>
    );
}

function AnalyticsIcon() {
    return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" className="h-5 w-5">
            <path d="M5 19V10.75" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
            <path d="M10.5 19V7.5" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
            <path d="M16 19V13" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
            <path d="M4.5 19.5H19.5" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
            <path d="M6.5 9.5L10.75 6.75L16.25 11.25L19 8.75" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
    );
}

function SalesIcon() {
    return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true" className="h-5 w-5">
            <path d="M3 9L12 3L21 9V19C21 19.55 20.55 20 20 20H4C3.45 20 3 19.55 3 19V9Z" stroke="currentColor" strokeWidth="1.7" />
            <path d="M9 20V12H15V20" stroke="currentColor" strokeWidth="1.7" />
            <path d="M9 14L12 11L15 14" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
    );
}

function MenuIcon({ icon }: { icon: (typeof dashboardMenu)[number]["icon"] }) {
    switch (icon) {
        case "products":
            return <ProductsIcon />;
        case "analytics":
            return <AnalyticsIcon />;
        case "sales":
            return <SalesIcon />;
        case "dashboard":
        default:
            return <DashboardIcon />;
    }
}

export function Sidebar({ onNavigate, isCollapsed = false }: SidebarProps) {
    const pathname = usePathname();
    const collapseMode = isCollapsed;

    return (
        <aside className={[
            "group h-full border-r border-[#f0dfc5] bg-white/95 backdrop-blur transition-all duration-300 ease-out",
            collapseMode ? "w-20" : "w-64",
        ].join(" ")}
        >
            <div className={[
                "border-b border-[#f0dfc5] py-4 transition-all duration-300 ease-out",
                collapseMode ? "px-3" : "px-4",
            ].join(" ")}
            >
                <div className="flex items-start justify-between gap-3">
                    {!collapseMode ? (
                        <div className="min-w-0">
                            <h1 className="text-lg font-semibold text-[#2d2418] transition-all duration-300 ease-out group-hover:text-[#1f1810]">Aventory</h1>
                            <p className="text-xs text-[#8a6b45] transition-all duration-300 ease-out group-hover:text-[#6f5536]">Admin Panel</p>
                        </div>
                    ) : (
                        <div />
                    )}

                    <div className="flex items-center gap-2">
                        <div className="flex h-10 w-10 items-center justify-center rounded-2xl bg-[#fff4e2] text-[#9a6b2f] shadow-sm transition-all duration-300 ease-out group-hover:rotate-6 group-hover:bg-[#ffe9c2]">
                            <span className="text-base font-semibold">◎</span>
                        </div>
                        {null}
                    </div>
                </div>
            </div>

            <nav className="p-3">
                {dashboardMenu.map((item) => {
                    const active = pathname === item.href;

                    return (
                        <Link
                            key={item.href}
                            href={item.href}
                            onClick={onNavigate}
                            className={[
                                "mb-1 flex items-center rounded-lg py-2 text-sm transition-all duration-300 ease-out",
                                collapseMode ? "justify-center px-2" : "gap-3 px-3",
                                active
                                    ? "bg-[#f59e0b]/10 text-[#9a6b2f] ring-1 ring-[#f59e0b]/15"
                                    : "text-[#5b4a37] hover:bg-[#fff4e2] hover:text-[#2d2418]",
                            ].join(" ")}
                            title={collapseMode ? item.label : undefined}
                        >
                            <span className={[
                                "transition-all duration-300 ease-out",
                                active ? "text-[#f59e0b]" : "text-[#8a6b45]",
                                collapseMode ? "scale-110" : "scale-100",
                            ].join(" ")}
                            >
                                <MenuIcon icon={item.icon} />
                            </span>
                            {collapseMode ? (
                                <span className="sr-only">{item.label}</span>
                            ) : (
                                <span className="font-medium transition-all duration-300 ease-out">{item.label}</span>
                            )}
                        </Link>
                    );
                })}
            </nav>
        </aside>
    );
}