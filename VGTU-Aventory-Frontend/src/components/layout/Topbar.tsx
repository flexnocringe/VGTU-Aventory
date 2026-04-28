"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { getAuthUserEmail } from "@/features/auth/session";
import { clearAuthToken } from "@/features/auth/session";
import { logoutUser } from "@/features/auth/services/logoutUser";

type TopbarProps = {
    onOpenMobileMenu: () => void;
    onToggleSidebar: () => void;
    sidebarCollapsed: boolean;
};

export function Topbar({ onOpenMobileMenu, onToggleSidebar, sidebarCollapsed }: TopbarProps) {
    const router = useRouter();
    const [isLoggingOut, setIsLoggingOut] = useState(false);
    const [mounted, setMounted] = useState(false);
    useEffect(() => {
        setMounted(true);
    }, []);
    const userEmail = mounted
    ? getAuthUserEmail()?.trim() || "User"
    : "User";

    async function handleLogout() {
        if (isLoggingOut) {
            return;
        }

        setIsLoggingOut(true);
        try {
            await logoutUser();
        } finally {
            clearAuthToken();
            router.push("/login");
            router.refresh();
            setIsLoggingOut(false);
        }
    }

    return (
        <header className="sticky top-0 z-20 flex h-14 items-center justify-between border-b border-[#f0dfc5] bg-white/85 px-4 backdrop-blur">
            <div className="flex items-center gap-2">
                <button
                    type="button"
                    onClick={onOpenMobileMenu}
                    aria-label="Open navigation menu"
                    className="group inline-flex h-10 w-10 items-center justify-center rounded-full border border-[#f0dfc5] bg-white text-[#5b4a37] shadow-sm transition hover:border-[#f59e0b] hover:text-[#2d2418] md:hidden"
                >
                    <span className="flex flex-col gap-1">
                        <span className="block h-0.5 w-4 rounded-full bg-current transition group-hover:w-5" />
                        <span className="block h-0.5 w-4 rounded-full bg-current transition group-hover:w-5" />
                        <span className="block h-0.5 w-3 rounded-full bg-current transition group-hover:w-4" />
                    </span>
                </button>

                <button
                    type="button"
                    onClick={onToggleSidebar}
                    aria-label={sidebarCollapsed ? "Expand sidebar" : "Collapse sidebar"}
                    className="group hidden h-10 w-10 items-center justify-center rounded-full border border-[#f0dfc5] bg-white text-[#5b4a37] shadow-sm transition hover:border-[#f59e0b] hover:text-[#2d2418] md:inline-flex"
                >
                    <span className="flex flex-col gap-1">
                        <span className="block h-0.5 w-4 rounded-full bg-current transition group-hover:w-5" />
                        <span className="block h-0.5 w-4 rounded-full bg-current transition group-hover:w-5" />
                        <span className="block h-0.5 w-3 rounded-full bg-current transition group-hover:w-4" />
                    </span>
                </button>
            </div>

            <p className="text-sm font-medium text-[#2d2418]">Inventory Management</p>
            <div className="flex items-center gap-3">
                <span className="text-sm text-[#8a6b45]">{userEmail}</span>
                <button
                    type="button"
                    onClick={handleLogout}
                    disabled={isLoggingOut}
                    className="rounded-md border border-[#f0dfc5] px-3 py-1 text-xs font-semibold text-[#6a5841] transition hover:border-[#f59e0b] hover:text-[#2d2418] disabled:cursor-not-allowed disabled:opacity-60"
                >
                    {isLoggingOut ? "Signing out..." : "Sign out"}
                </button>
            </div>
        </header>
    );
}