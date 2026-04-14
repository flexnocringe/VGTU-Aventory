"use client";

import { useEffect, useState } from "react";
import { EventForm } from "./EventForm";

interface EventDTO {
  startDate: string;
  endDate: string;
  description: string;
}

export default function MyEvents() {
  const [events, setEvents] = useState<EventDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);

  async function fetchEvents() {
    setLoading(true);
    try {
      const response = await fetch("/api/events/my-events");
      const data = await response.json();

      if (response.ok) {
        if (Array.isArray(data)) {
          setEvents(data);
          setMessage(null);
        } else if (data.message) {
          setMessage(data.message);
          setEvents([]);
        }
      } else {
        setMessage("Failed to fetch events.");
      }
    } catch (error) {
      setMessage("An error occurred while fetching events.");
      console.error("Fetch error:", error);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchEvents();
  }, []);

  const handleCreateEvent = async (data: { startDate: string; endDate: string; description: string }) => {
    try {
      const response = await fetch("/api/events/create", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });

      if (response.ok) {
        setIsFormOpen(false);
        fetchEvents();
      } else {
        const errorData = await response.json();
        alert(errorData.message || "Failed to create event.");
      }
    } catch (error) {
      console.error("Create error:", error);
      alert("An error occurred while creating the event.");
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  return (
    <section className="mx-auto flex w-full max-w-6xl flex-col gap-6">
      <header className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-[#2d2418]">My Events</h1>
          <p className="text-sm text-[#6a5841]">
            View and manage your created events.
          </p>
        </div>
        {!isFormOpen && (
          <button
            onClick={() => setIsFormOpen(true)}
            className="rounded-full bg-[#f59e0b] px-6 py-2.5 text-sm font-bold text-white shadow-[0_4px_14px_rgba(245,158,11,0.39)] transition-all hover:bg-[#ea8c08] hover:shadow-[0_6px_20px_rgba(245,158,11,0.23)] focus:outline-none focus:ring-2 focus:ring-[#f59e0b]/50"
          >
            Create Event
          </button>
        )}
      </header>

      {isFormOpen && (
        <div className="mb-6">
          <EventForm
            onSave={handleCreateEvent}
            onCancel={() => setIsFormOpen(false)}
          />
        </div>
      )}

      {message && (
        <div className="rounded-2xl border border-blue-200 bg-blue-50 p-6 text-blue-800" role="alert">
          <p>{message}</p>
        </div>
      )}

      {loading ? (
        <div className="flex items-center justify-center py-12">
          <div className="text-xl font-semibold text-[#6a5841]">Loading events...</div>
        </div>
      ) : events.length > 0 ? (
        <div className="grid gap-6 md:grid-cols-2">
          {events.map((event, index) => (
            <div key={index} className="rounded-2xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)]">
              <div className="flex flex-col gap-4">
                <div className="flex flex-wrap justify-between gap-2 border-b border-[#f0dfc5] pb-4">
                  <div className="text-sm font-medium text-[#9a6b2f]">
                    <span className="font-bold">Starts:</span> {formatDate(event.startDate)}
                  </div>
                  <div className="text-sm font-medium text-[#b45309]">
                    <span className="font-bold">Ends:</span> {formatDate(event.endDate)}
                  </div>
                </div>
                <p className="text-[#5b4a37] text-base leading-relaxed">
                  {event.description}
                </p>
              </div>
            </div>
          ))}
        </div>
      ) : !message && (
        <div className="rounded-2xl border border-[#f0dfc5] bg-white p-12 text-center shadow-[0_12px_30px_rgba(154,107,47,0.08)]">
           <p className="text-[#6a5841]">No events found.</p>
        </div>
      )}
    </section>
  );
}
