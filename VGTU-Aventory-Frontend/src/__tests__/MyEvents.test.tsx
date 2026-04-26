import { render, screen } from '@testing-library/react'
import MyEvents from '../app/(dashboard)/my-events/page'

describe('MyEvents Page', () => {
  const mockFetch = jest.fn()

  beforeAll(() => {
    global.fetch = mockFetch as unknown as typeof fetch
  })

  beforeEach(() => {
    mockFetch.mockReset()
  })

  it('renders events correctly', async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => [
        { 
          id: 1, 
          startDate: '2026-03-17T10:00:00Z', 
          endDate: '2026-03-17T12:00:00Z', 
          description: 'Simplified Test Event' 
        }
      ],
    } as Response)

    render(<MyEvents />)

    expect(await screen.findByText('Simplified Test Event')).toBeInTheDocument()
  })

  it('renders empty message when no events', async () => {
    mockFetch.mockResolvedValue({
      ok: true,
      json: async () => [],
    } as Response)

    render(<MyEvents />)

    expect(await screen.findByText(/no events found/i)).toBeInTheDocument()
  })

  it('renders error message on fetch failure', async () => {
    mockFetch.mockResolvedValue({
      ok: false,
      json: async () => ({}),
    } as Response)

    render(<MyEvents />)

    expect(await screen.findByText(/failed to fetch events/i)).toBeInTheDocument()
  })
})
