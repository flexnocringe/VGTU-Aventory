import { render, screen } from '@testing-library/react'
import Home from '../app/(dashboard)/page'

// Mock the child component to keep the test simple
jest.mock('@/features/dashboard/components/InventoryDashboard', () => ({
  InventoryDashboard: () => <div>Inventory Dashboard Mock</div>
}))

describe('Home Page', () => {
  it('renders the inventory dashboard', () => {
    render(<Home />)
    expect(screen.getByText('Inventory Dashboard Mock')).toBeInTheDocument()
  })
})
