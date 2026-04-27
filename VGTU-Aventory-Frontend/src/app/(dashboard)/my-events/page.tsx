"use client";

import { useEffect, useState } from "react";
import { EventForm } from "./EventForm";

interface EventDTO {
  id?: number;
  startDate: string;
  endDate: string;
  description: string;
}

export default function MyEvents() {
  const [events, setEvents] = useState<EventDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState<EventDTO | null>(null);

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
          // If backend returns a message instead of array, it likely means no events found
          setEvents([]);
          setMessage(null); // Clear message to show the "No events found" state
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
        alert(errorData.message || errorData.error || "Failed to create event.");
      }
    } catch (error) {
      console.error("Create error:", error);
      alert("An error occurred while creating the event.");
    }
  };

  const handleUpdateEvent = async (data: { startDate: string; endDate: string; description: string }) => {
    if (!editingEvent?.id) return;

    try {
      const response = await fetch(`/api/events/edit/${editingEvent.id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
      });

      if (response.ok) {
        setEditingEvent(null);
        fetchEvents();
      } else {
        const errorData = await response.json();
        alert(errorData.message || errorData.error || "Failed to update event.");
      }
    } catch (error) {
      console.error("Update error:", error);
      alert("An error occurred while updating the event.");
    }
  };

  const handleDeleteEvent = async (id: number) => {
    if (!confirm("Are you sure you want to delete this event?")) return;

    try {
      const response = await fetch(`/api/events/delete/${id}`, {
        method: "DELETE",
      });

      if (response.ok) {
        fetchEvents();
      } else {
        const errorData = await response.json();
        alert(errorData.message || errorData.error || "Failed to delete event.");
      }
    } catch (error) {
      console.error("Delete error:", error);
      alert("An error occurred while deleting the event.");
    }
  };

  const openEditForm = (event: EventDTO) => {
    setEditingEvent(event);
    setIsFormOpen(false); // Close create form if open
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
        {!isFormOpen && !editingEvent && (
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

      {editingEvent && (
        <div className="mb-6">
          <EventForm
            isEditing
            initialData={editingEvent}
            onSave={handleUpdateEvent}
            onCancel={() => setEditingEvent(null)}
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
          {events.map((event) => (
            <div key={event.id} className="rounded-2xl border border-[#f0dfc5] bg-white p-6 shadow-[0_12px_30px_rgba(154,107,47,0.08)]">
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
                <div className="flex justify-end gap-4 pt-2">
                  <button
                    onClick={() => openEditForm(event)}
                    className="text-sm font-semibold text-[#f59e0b] hover:text-[#ea8c08] transition-colors"
                  >
                    Edit Event
                  </button>
                  <button
                    onClick={() => event.id && handleDeleteEvent(event.id)}
                    className="text-sm font-semibold text-red-600 hover:text-red-800 transition-colors"
                  >
                    Delete Event
                  </button>
                </div>
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
