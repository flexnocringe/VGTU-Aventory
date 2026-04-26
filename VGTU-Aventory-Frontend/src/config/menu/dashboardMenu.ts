export type DashboardMenuItem = {
    label: string;
    href: string;
    icon: "dashboard" | "products" | "analytics" | "sales" | "events";
};

export const dashboardMenu: DashboardMenuItem[] = [
    { label: "Dashboard", href: "/", icon: "dashboard" },
    { label: "Products", href: "/products", icon: "products" },
    { label: "Sales", href: "/sales", icon: "sales" },
    { label: "Analytics", href: "/analytics", icon: "analytics" },
    { label: "My Events", href: "/my-events", icon: "events" },
];