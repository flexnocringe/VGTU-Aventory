import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CategoryForm } from "@/app/(dashboard)/products/CategoryForm";

describe("CategoryForm", () => {
    it("submits the category name", async () => {
        const user = userEvent.setup();
        const onSave = jest.fn();

        render(<CategoryForm onSave={onSave} onCancel={jest.fn()} />);

        await user.type(screen.getByLabelText("Category Name"), "Office");
        await user.click(screen.getByRole("button", { name: "Add Category" }));

        expect(onSave).toHaveBeenCalledWith({ categoryName: "Office" });
    });

    it("calls cancel", async () => {
        const user = userEvent.setup();
        const onCancel = jest.fn();

        render(<CategoryForm onSave={jest.fn()} onCancel={onCancel} />);

        await user.click(screen.getByRole("button", { name: "Cancel" }));

        expect(onCancel).toHaveBeenCalledTimes(1);
    });
});