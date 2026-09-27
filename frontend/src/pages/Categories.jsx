import { useEffect, useState } from 'react'
import client from '../api/client'

export default function Categories() {
  const [categories, setCategories] = useState([])
  const [name, setName] = useState('')
  const [type, setType] = useState('DISCRETIONARY')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function load() {
    const res = await client.get('/categories')
    setCategories(res.data)
  }

  useEffect(() => { load() }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await client.post('/categories', { name, type })
      setName('')
      await load()
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create this category.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete(id) {
    try {
      await client.delete(`/categories/${id}`)
      setCategories(categories.filter((c) => c.id !== id))
    } catch (err) {
      setError(err.response?.data?.message || 'Could not delete this category.')
    }
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Categories</h1>
          <p>Default categories plus any you've added yourself.</p>
        </div>
      </div>

      <div className="form-panel">
        {error && <div className="error-banner">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="field">
              <label>Name</label>
              <input value={name} onChange={(e) => setName(e.target.value)} placeholder="e.g. Subscriptions" required />
            </div>
            <div className="field">
              <label>Type</label>
              <select value={type} onChange={(e) => setType(e.target.value)}>
                <option value="ESSENTIAL">Essential</option>
                <option value="DISCRETIONARY">Discretionary</option>
              </select>
            </div>
          </div>
          <button className="btn-primary" type="submit" disabled={submitting}>
            {submitting ? 'Adding…' : 'Add category'}
          </button>
        </form>
      </div>

      <table className="ledger">
        <thead>
          <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Source</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {categories.map((c) => (
            <tr key={c.id}>
              <td>{c.name}</td>
              <td>{c.type === 'ESSENTIAL' ? 'Essential' : 'Discretionary'}</td>
              <td style={{ color: 'var(--ink-soft)' }}>{c.custom ? 'Custom' : 'Default'}</td>
              <td style={{ textAlign: 'right' }}>
                {c.custom && <button className="btn-text" onClick={() => handleDelete(c.id)}>Delete</button>}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
