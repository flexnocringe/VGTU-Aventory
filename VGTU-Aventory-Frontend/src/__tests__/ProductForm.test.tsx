import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ProductForm } from "@/app/(dashboard)/products/ProductForm";

jest.mock(
    "qrcode.react",
    () => ({
        QRCodeCanvas: () => null,
    }),
    { virtual: true },
);

describe("ProductForm", () => {
    it("renders categories and submits selected category", async () => {
        const user = userEvent.setup();
        const onSave = jest.fn();

        render(
            <ProductForm
                onSave={onSave}
                onCancel={jest.fn()}
                categories={[
                    { categoryId: 1, categoryName: "Electronics" },
                    { categoryId: 2, categoryName: "Accessories" },
                ]}
            />,
        );

        await user.type(screen.getByLabelText("Name"), "Mouse");
        await user.selectOptions(screen.getByLabelText("Category"), "2");
        await user.click(screen.getByRole("button", { name: "Add Product" }));

        expect(onSave).toHaveBeenCalledWith(
            expect.objectContaining({
                name: "Mouse",
                categoryId: 2,
            }),
        );
    });
});