"use client";

import { useEffect, useState } from "react";

interface EventDTO {
  startDate: string;
  endDate: string;
  description: string;
}

export default function MyEvents() {
  const [events, setEvents] = useState<EventDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    async function fetchEvents() {
      try {
        const response = await fetch("/api/events/my-events");
        const data = await response.json();

        if (response.ok) {
          if (Array.isArray(data)) {
            setEvents(data);
          } else if (data.message) {
            setMessage(data.message);
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

    fetchEvents();
  }, []);

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-100">
        <div className="text-xl font-semibold text-gray-700">Loading events...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 mb-6 text-center">My Events</h1>

        {message && (
          <div className="bg-blue-100 border-l-4 border-blue-500 text-blue-700 p-4 mb-6" role="alert">
            <p>{message}</p>
          </div>
        )}

        {events.length > 0 && (
          <div className="space-y-4">
            {events.map((event, index) => (
              <div key={index} className="bg-white p-6 rounded-lg shadow-md border border-gray-200">
                <div className="flex flex-col sm:flex-row sm:justify-between mb-4">
                  <div className="text-sm font-medium text-blue-600">
                    <span className="font-bold">Starts:</span> {formatDate(event.startDate)}
                  </div>
                  <div className="text-sm font-medium text-red-600">
                    <span className="font-bold">Ends:</span> {formatDate(event.endDate)}
                  </div>
                </div>
                <p className="text-gray-700 text-base leading-relaxed">
                  {event.description}
                </p>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
