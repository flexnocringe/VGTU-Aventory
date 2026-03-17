import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import Home from '../app/page'

describe('Home Page', () => {
  it('renders the hello world heading', () => {
    render(<Home />)
    expect(screen.getByText('Hello World')).toBeInTheDocument()
  })

  it('renders the "View My Events" link', () => {
    render(<Home />)
    const link = screen.getByRole('link', { name: /view my events/i })
    expect(link).toBeInTheDocument()
    expect(link).toHaveAttribute('href', '/my-events')
  })
})
