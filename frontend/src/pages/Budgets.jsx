import { useEffect, useState } from 'react'
import client from '../api/client'

const now = new Date()

function formatMoney(n) {
  return Number(n || 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' })
}

const statusColor = {
  UNDER_BUDGET: 'var(--primary)',
  NEAR_LIMIT: '#C08A2E',
  OVER_LIMIT: 'var(--danger)',
}

export default function Budgets() {
  const [month, setMonth] = useState(now.getMonth() + 1)
  const [year, setYear] = useState(now.getFullYear())
  const [budgets, setBudgets] = useState([])
  const [categories, setCategories] = useState([])
  const [categoryId, setCategoryId] = useState('')
  const [monthlyLimit, setMonthlyLimit] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function loadBudgets() {
    const res = await client.get('/budgets', { params: { month, year } })
    setBudgets(res.data)
  }

  useEffect(() => {
    client.get('/categories').then((res) => setCategories(res.data))
  }, [])

  useEffect(() => { loadBudgets() }, [month, year])

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await client.post('/budgets', {
        categoryId: Number(categoryId),
        monthlyLimit: Number(monthlyLimit),
        periodMonth: Number(month),
        periodYear: Number(year),
      })
      setCategoryId('')
      setMonthlyLimit('')
      await loadBudgets()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not save this budget.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete(id) {
    await client.delete(`/budgets/${id}`)
    setBudgets(budgets.filter((b) => b.id !== id))
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Budgets</h1>
          <p>Set a monthly limit per category and track how close you are.</p>
        </div>
        <div style={{ display: 'flex', gap: 8 }}>
          <select value={month} onChange={(e) => setMonth(Number(e.target.value))}>
            {Array.from({ length: 12 }, (_, i) => (
              <option key={i + 1} value={i + 1}>{new Date(2000, i).toLocaleString('en-US', { month: 'long' })}</option>
            ))}
          </select>
          <select value={year} onChange={(e) => setYear(Number(e.target.value))}>
            {[year - 1, year, year + 1].map((y) => <option key={y} value={y}>{y}</option>)}
          </select>
        </div>
      </div>

      <div className="form-panel">
        {error && <div className="error-banner">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="field">
              <label>Category</label>
              <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
                <option value="" disabled>Select…</option>
                {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
            <div className="field">
              <label>Monthly limit</label>
              <input type="number" step="0.01" min="0.01" value={monthlyLimit} onChange={(e) => setMonthlyLimit(e.target.value)} required />
            </div>
          </div>
          <button className="btn-primary" type="submit" disabled={submitting}>
            {submitting ? 'Saving…' : 'Set budget'}
          </button>
        </form>
      </div>

      {budgets.length === 0 ? (
        <p className="empty-state">No budgets set for this period yet.</p>
      ) : (
        <div>
          {budgets.map((b) => (
            <div className="budget-row" key={b.id}>
              <div className="budget-row-top">
                <span>
                  {b.categoryName}
                  <span className={`status-tag status-${b.status}`}>
                    {b.status.replace('_', ' ').toLowerCase()}
                  </span>
                </span>
                <span>
                  <span className="mono">{formatMoney(b.spent)} / {formatMoney(b.monthlyLimit)}</span>
                  <button className="btn-text" style={{ marginLeft: 12 }} onClick={() => handleDelete(b.id)}>Remove</button>
                </span>
              </div>
              <div className="budget-bar-track">
                <div
                  className="budget-bar-fill"
                  style={{
                    width: `${Math.min(100, Number(b.utilizationPct))}%`,
                    background: statusColor[b.status] || 'var(--primary)',
                  }}
                />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
