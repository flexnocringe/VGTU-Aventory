import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import MyEvents from '../app/my-events/page'

describe('MyEvents Page', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('renders loading state initially', () => {
    // Return a promise that doesn't resolve immediately
    ;(global.fetch as any).mockReturnValue(new Promise(() => {}))
    
    render(<MyEvents />)
    expect(screen.getByText(/loading events/i)).toBeInTheDocument()
  })

  it('renders events when fetch is successful', async () => {
    const mockEvents = [
      {
        startDate: '2026-03-17T10:00:00Z',
        endDate: '2026-03-17T12:00:00Z',
        description: 'Test Event 1',
      },
      {
        startDate: '2026-03-18T14:00:00Z',
        endDate: '2026-03-18T16:00:00Z',
        description: 'Test Event 2',
      },
    ]

    ;(global.fetch as any).mockResolvedValue({
      ok: true,
      json: async () => mockEvents,
    })

    render(<MyEvents />)

    await waitFor(() => {
      expect(screen.queryByText(/loading events/i)).not.toBeInTheDocument()
    }, { timeout: 3000 })

    expect(screen.getByText('Test Event 1')).toBeInTheDocument()
    expect(screen.getByText('Test Event 2')).toBeInTheDocument()
    expect(screen.getAllByText(/Starts:/i)[0]).toBeInTheDocument()
  })

  it('renders error message when fetch fails', async () => {
    ;(global.fetch as any).mockResolvedValue({
      ok: false,
      json: async () => ({}),
    })

    render(<MyEvents />)

    await waitFor(() => {
      expect(screen.getByText(/failed to fetch events/i)).toBeInTheDocument()
    })
  })

  it('renders custom message from API if present', async () => {
    ;(global.fetch as any).mockResolvedValue({
      ok: true,
      json: async () => ({ message: 'No events found' }),
    })

    render(<MyEvents />)

    await waitFor(() => {
      expect(screen.getByText('No events found')).toBeInTheDocument()
    })
  })
})
