// app/page.tsx
"use client";

import React, { useState, useEffect, ChangeEvent, FormEvent } from "react";

// Tipai
interface Product {
    productId: number;
    productName: string;
    quantity: number;
}

type SaleType = "SALE" | "RETURN";

export default function Page() {
    const [products, setProducts] = useState<Product[]>([]);
    const [selectedProductId, setSelectedProductId] = useState<number | "">("");
    const [quantity, setQuantity] = useState<number>(1);
    const [saleType, setSaleType] = useState<SaleType>("SALE");
    const [saleLocation, setSaleLocation] = useState("Vilnius");
    const [saleNote, setSaleNote] = useState("");
    const [ownerId] = useState<number>(1); // jei reikia dinamiškai, vėliau gali pakeisti
    const [responseMessage, setResponseMessage] = useState<string>("");

    // Gauti produktus iš backendo
    useEffect(() => {
        fetch("http://localhost:8080/products")
            .then(res => res.json())
            .then((data: Product[]) => setProducts(data))
            .catch(err => console.error(err));
    }, []);

    const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
        e.preventDefault();

        if (selectedProductId === "") {
            setResponseMessage("Pasirinkite produktą!");
            return;
        }

        if (quantity < 1) {
            setResponseMessage("Kiekis turi būti bent 1!");
            return;
        }

        const body = {
            productId: selectedProductId,
            ownerId,
            quantity,
            saleType,
            saleLocation,
            saleNote
        };

        try {
            const res = await fetch("http://localhost:8080/sales", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            });

            const data = await res.json();
            if (!res.ok) throw new Error(data.message || JSON.stringify(data));

            setResponseMessage("Sale registered successfully!");
            // Galima resetint formą po sėkmės
            setSelectedProductId("");
            setQuantity(1);
            setSaleType("SALE");
            setSaleLocation("Vilnius");
            setSaleNote("");
        } catch (err: any) {
            setResponseMessage("Error: " + err.message);
        }
    };

    return (
        <div style={{ maxWidth: 500, margin: "50px auto", fontFamily: "Arial" }}>
            <h2>Pardavimo forma</h2>
            <form onSubmit={handleSubmit}>
                <div style={{ marginBottom: 10 }}>
                    <label>Produktas:</label>
                    <select
                        value={selectedProductId}
                        onChange={(e: ChangeEvent<HTMLSelectElement>) =>
                            setSelectedProductId(Number(e.target.value))
                        }
                        required
                    >
                        <option value="">-- Pasirink produktą --</option>
                        {products.map((p) => (
                            <option key={p.productId} value={p.productId}>
                                {p.productName} ({p.quantity} vnt.)
                            </option>
                        ))}
                    </select>
                </div>

                <div style={{ marginBottom: 10 }}>
                    <label>Kiekis:</label>
                    <input
                        type="number"
                        value={quantity}
                        min={1}
                        max={
                            selectedProductId
                                ? products.find((p) => p.productId === selectedProductId)?.quantity
                                : undefined
                        }
                        onChange={(e: ChangeEvent<HTMLInputElement>) =>
                            setQuantity(Number(e.target.value))
                        }
                        required
                    />
                </div>

                <div style={{ marginBottom: 10 }}>
                    <label>Tipas:</label>
                    <select
                        value={saleType}
                        onChange={(e: ChangeEvent<HTMLSelectElement>) =>
                            setSaleType(e.target.value as SaleType)
                        }
                    >
                        <option value="SALE">Pardavimas</option>
                        <option value="RETURN">Grąžinimas</option>
                    </select>
                </div>

                <div style={{ marginBottom: 10 }}>
                    <label>Vieta:</label>
                    <input
                        type="text"
                        value={saleLocation}
                        onChange={(e: ChangeEvent<HTMLInputElement>) =>
                            setSaleLocation(e.target.value)
                        }
                    />
                </div>

                <div style={{ marginBottom: 10 }}>
                    <label>Pastaba:</label>
                    <input
                        type="text"
                        value={saleNote}
                        onChange={(e: ChangeEvent<HTMLInputElement>) =>
                            setSaleNote(e.target.value)
                        }
                    />
                </div>

                <button type="submit" style={{ marginTop: 10 }}>
                    Patvirtinti pardavimą
                </button>
            </form>

            {responseMessage && (
                <p style={{ marginTop: 20, fontWeight: "bold" }}>{responseMessage}</p>
            )}
        </div>
    );
}