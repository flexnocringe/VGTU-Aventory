"use client";

import { useEffect, useState } from "react";
import { deleteEvent } from "../services/EventService";

type Event = {
    id: number;
    description: string;
};

export default function EventList() {
    const [events, setEvents] = useState<Event[]>([]);
    const [message, setMessage] = useState("");

    const fetchEvents = async () => {
        try {
            const res = await fetch("http://localhost:8080/events");
            const data = await res.json();
            return data; // return the data instead of calling setEvents here
        } catch (err) {
            console.error("Failed to fetch events:", err);
            setMessage("Klaida gaunant renginius ❌");
            return [];
        }
    };

    useEffect(() => {
        const load = async () => {
            const eventsData = await fetchEvents();
            setEvents(eventsData); // call setEvents outside fetchEvents
        };
        load();
    }, []);

    const handleDelete = async (id: number) => {
        const confirmDelete = window.confirm("Ar tikrai nori ištrinti renginį?");
        if (!confirmDelete) return;

        try {
            await deleteEvent(id);
            setMessage("Renginys ištrintas ✅");
            fetchEvents(); // refresh list
        } catch {
            setMessage("Klaida trinant ❌");
        }
    };

    return (
        <div>
            {message && <p>{message}</p>}

            {events.length === 0 ? (
                <p>No events found.</p>
            ) : (
                events.map((event) => (
                    <div key={event.id}>
                        <p>{event.description}</p>
                        <button onClick={() => handleDelete(event.id)}>Delete</button>
                    </div>
                ))
            )}
        </div>
    );
}