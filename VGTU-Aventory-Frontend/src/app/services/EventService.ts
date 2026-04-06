export const deleteEvent = async (id: number) => {
    const response = await fetch(`http://localhost:8080/events/${id}`, {
        method: "DELETE",
    });

    if (!response.ok) {
        throw new Error("Failed to delete event");
    }
};