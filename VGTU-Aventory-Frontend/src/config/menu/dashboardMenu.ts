export type DashboardMenuItem = {
    label: string;
    href: string;
    icon: "dashboard" | "products" | "analytics" | "sales";
};

export const dashboardMenu: DashboardMenuItem[] = [
    { label: "Dashboard", href: "/", icon: "dashboard" },
    { label: "Products", href: "/products", icon: "products" },
    { label: "Sales", href: "/sales", icon: "sales" },
    { label: "Analytics", href: "/analytics", icon: "analytics" },
];