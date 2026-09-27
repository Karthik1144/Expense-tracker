import { useEffect, useState } from 'react'
import client from '../api/client'

const emptyForm = { categoryId: '', amount: '', type: 'EXPENSE', description: '', paymentMethod: '', transactionDate: new Date().toISOString().slice(0, 10) }

function formatMoney(n) {
  return Number(n || 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' })
}

export default function Transactions() {
  const [transactions, setTransactions] = useState([])
  const [categories, setCategories] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function loadAll() {
    const [txRes, catRes] = await Promise.all([
      client.get('/transactions', { params: { size: 50, sort: 'transactionDate,desc' } }),
      client.get('/categories'),
    ])
    setTransactions(txRes.data.content || txRes.data)
    setCategories(catRes.data)
  }

  useEffect(() => { loadAll() }, [])

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await client.post('/transactions', {
        ...form,
        categoryId: Number(form.categoryId),
        amount: Number(form.amount),
      })
      setForm(emptyForm)
      await loadAll()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not save this transaction.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete(id) {
    await client.delete(`/transactions/${id}`)
    setTransactions(transactions.filter((t) => t.id !== id))
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Transactions</h1>
          <p>Every entry in your ledger, newest first.</p>
        </div>
      </div>

      <div className="form-panel">
        {error && <div className="error-banner">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="field">
              <label>Type</label>
              <select name="type" value={form.type} onChange={handleChange}>
                <option value="EXPENSE">Expense</option>
                <option value="INCOME">Income</option>
              </select>
            </div>
            <div className="field">
              <label>Category</label>
              <select name="categoryId" value={form.categoryId} onChange={handleChange} required>
                <option value="" disabled>Select…</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
            <div className="field">
              <label>Amount</label>
              <input name="amount" type="number" step="0.01" min="0.01" value={form.amount} onChange={handleChange} required />
            </div>
            <div className="field">
              <label>Date</label>
              <input name="transactionDate" type="date" value={form.transactionDate} onChange={handleChange} required />
            </div>
            <div className="field">
              <label>Payment method</label>
              <input name="paymentMethod" type="text" placeholder="Card, cash…" value={form.paymentMethod} onChange={handleChange} />
            </div>
            <div className="field">
              <label>Description</label>
              <input name="description" type="text" placeholder="Optional note" value={form.description} onChange={handleChange} />
            </div>
          </div>
          <button className="btn-primary" type="submit" disabled={submitting}>
            {submitting ? 'Saving…' : 'Add transaction'}
          </button>
        </form>
      </div>

      {transactions.length === 0 ? (
        <p className="empty-state">No transactions yet. Add your first one above.</p>
      ) : (
        <table className="ledger">
          <thead>
            <tr>
              <th>Date</th>
              <th>Category</th>
              <th>Description</th>
              <th style={{ textAlign: 'right' }}>Amount</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((t) => (
              <tr key={t.id}>
                <td className="mono">{t.transactionDate}</td>
                <td>{t.categoryName}</td>
                <td>{t.description || '—'}</td>
                <td className={`mono ${t.type === 'EXPENSE' ? 'amount-expense' : 'amount-income'}`} style={{ textAlign: 'right' }}>
                  {t.type === 'EXPENSE' ? '-' : '+'}{formatMoney(t.amount)}
                </td>
                <td style={{ textAlign: 'right' }}>
                  <button className="btn-text" onClick={() => handleDelete(t.id)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
