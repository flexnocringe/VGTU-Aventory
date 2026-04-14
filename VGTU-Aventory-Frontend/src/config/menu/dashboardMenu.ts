export type DashboardMenuItem = {
    label: string;
    href: string;
    icon: "dashboard" | "products" | "analytics";
};

export const dashboardMenu: DashboardMenuItem[] = [
    { label: "Dashboard", href: "/", icon: "dashboard" },
    { label: "Products", href: "/products", icon: "products" },
    { label: "Analytics", href: "/analytics", icon: "analytics" },
];