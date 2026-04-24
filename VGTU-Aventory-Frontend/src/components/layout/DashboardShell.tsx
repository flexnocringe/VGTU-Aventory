"use client";

import { useState } from "react";
import { Sidebar } from "@/components/layout/Sidebar";
import { Topbar } from "@/components/layout/Topbar";

type DashboardShellProps = {
    children: React.ReactNode;
};

export function DashboardShell({ children }: DashboardShellProps) {
    const [mobileOpen, setMobileOpen] = useState(false);
    const [sidebarCollapsed, setSidebarCollapsed] = useState(false);

    const toggleSidebar = () => {
        setSidebarCollapsed((current) => !current);
    };

    return (
        <div className="min-h-screen bg-transparent">
            <div className={[
                "hidden md:fixed md:inset-y-0 md:block transition-all duration-300 ease-out",
                sidebarCollapsed ? "md:w-20 md:hover:w-64" : "md:w-64",
            ].join(" ")}
            >
                <Sidebar isCollapsed={sidebarCollapsed} onToggleCollapse={toggleSidebar} />
            </div>

            <div
                className={[
                    "fixed inset-0 z-40 md:hidden transition-opacity duration-300",
                    mobileOpen ? "pointer-events-auto opacity-100" : "pointer-events-none opacity-0",
                ].join(" ")}
            >
                <div
                    className="absolute inset-0 bg-black/45 backdrop-blur-[1px]"
                    onClick={() => setMobileOpen(false)}
                />
                <div
                    className={[
                        "absolute inset-y-0 left-0 z-50 w-64 transform transition-transform duration-300 ease-out",
                        mobileOpen ? "translate-x-0" : "-translate-x-full",
                    ].join(" ")}
                >
                    <Sidebar
                        mobileOpen={mobileOpen}
                        onNavigate={() => setMobileOpen(false)}
                        isCollapsed={false}
                        onToggleCollapse={() => undefined}
                    />
                </div>
            </div>

            <div className={sidebarCollapsed ? "md:pl-20 md:transition-[padding] md:duration-300 md:ease-out" : "md:pl-64 md:transition-[padding] md:duration-300 md:ease-out"}>
                <Topbar
                    onOpenMobileMenu={() => setMobileOpen(true)}
                    onToggleSidebar={toggleSidebar}
                    sidebarCollapsed={sidebarCollapsed}
                />
                <main className="p-4 md:p-6">{children}</main>
            </div>
        </div>
    );
}