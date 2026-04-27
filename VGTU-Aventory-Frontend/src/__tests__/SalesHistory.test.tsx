import { render, screen } from '@testing-library/react'
import { SalesHistory } from '../features/sales/components/SalesHistory'
import * as getSalesHistoryModule from '../features/sales/services/getSalesHistory'

jest.mock('../features/sales/services/getSalesHistory')

describe('SalesHistory Component', () => {
  const mockGetSalesHistory = getSalesHistoryModule.getSalesHistory as jest.MockedFunction<typeof getSalesHistoryModule.getSalesHistory>

  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('renders sales correctly', async () => {
    mockGetSalesHistory.mockResolvedValue([
      { 
        id: 1, 
        product: { productId: 1, productName: 'Test Product' },
        quantity: 5,
        saleType: 'SALE' as const,
        saleLocation: 'Test Location',
        saleProductPrice: 10.00,
        totalPrice: 50.00,
        saleDate: '2026-03-17T10:00:00Z',
        owner: { id: 1, email: 'test@test.com' },
        saleNote: ''
      }
    ])

    render(<SalesHistory />)

    expect(await screen.findByText('Test Product')).toBeInTheDocument()
    expect(await screen.findByText('5')).toBeInTheDocument()
    expect(await screen.findByText('SALE')).toBeInTheDocument()
    expect(await screen.findByText('Test Location')).toBeInTheDocument()
    expect(await screen.findByText('$50.00')).toBeInTheDocument()
  })

  it('renders loading state', () => {
    mockGetSalesHistory.mockImplementation(() => new Promise(() => {}))

    render(<SalesHistory />)

    expect(screen.getByText('Loading sales history...')).toBeInTheDocument()
  })

  it('renders empty message when no sales', async () => {
    mockGetSalesHistory.mockResolvedValue([])

    render(<SalesHistory />)

    expect(await screen.findByText('No sales recorded yet')).toBeInTheDocument()
  })

  it('renders error message on fetch failure', async () => {
    mockGetSalesHistory.mockRejectedValue(new Error('Failed to fetch'))

    render(<SalesHistory />)

    expect(await screen.findByText(/error loading sales history/i)).toBeInTheDocument()
  })

  it('renders RENTAL sale type correctly', async () => {
    mockGetSalesHistory.mockResolvedValue([
      { 
        id: 2, 
        product: { productId: 2, productName: 'Rental Product' },
        quantity: 2,
        saleType: 'RETURN' as const,
        saleLocation: 'Rental Location',
        saleProductPrice: 15.00,
        totalPrice: 30.00,
        saleDate: '2026-03-18T14:00:00Z',
        owner: { id: 1, email: 'test@test.com' },
        saleNote: ''
      }
    ])

    render(<SalesHistory />)

    expect(await screen.findByText('RETURN')).toBeInTheDocument()
  })
})