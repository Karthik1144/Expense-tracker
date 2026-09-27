import { useEffect, useState } from 'react'
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import client from '../api/client'

const monthNames = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']

function formatMoney(n) {
  const num = Number(n || 0)
  return num.toLocaleString('en-US', { style: 'currency', currency: 'USD' })
}

export default function Dashboard() {
  const [summary, setSummary] = useState(null)
  const [breakdown, setBreakdown] = useState([])
  const [trend, setTrend] = useState([])
  const [insight, setInsight] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function load() {
      try {
        const [s, b, t, i] = await Promise.all([
          client.get('/dashboard/summary'),
          client.get('/dashboard/category-breakdown'),
          client.get('/dashboard/trend', { params: { months: 6 } }),
          client.get('/dashboard/discretionary-insight'),
        ])
        setSummary(s.data)
        setBreakdown(b.data)
        setTrend(t.data.map(p => ({
          label: `${monthNames[p.month - 1]} ${String(p.year).slice(2)}`,
          income: Number(p.totalIncome),
          expense: Number(p.totalExpense),
        })))
        setInsight(i.data)
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  if (loading) return <p className="empty-state">Loading your ledger…</p>

  const maxBreakdown = Math.max(1, ...breakdown.map(b => Number(b.total)))

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Dashboard</h1>
          <p>Your finances this month, at a glance.</p>
        </div>
      </div>

      <div className="stat-row">
        <div className="stat">
          <div className="stat-label">Income</div>
          <div className="stat-value mono">{formatMoney(summary.totalIncome)}</div>
        </div>
        <div className="stat">
          <div className="stat-label">Expenses</div>
          <div className="stat-value mono">{formatMoney(summary.totalExpense)}</div>
        </div>
        <div className="stat">
          <div className="stat-label">Net savings</div>
          <div className="stat-value mono">{formatMoney(summary.netSavings)}</div>
        </div>
      </div>

      {insight && (
        <div className="form-panel">
          <h3 style={{ fontSize: '1rem' }}>Discretionary spending</h3>
          <p style={{ fontSize: '0.88rem', color: 'var(--ink-soft)', margin: '0 0 4px 0' }}>
            {Number(insight.percentageChange) <= 0
              ? `Down ${Math.abs(insight.percentageChange)}% from last month — nice work.`
              : `Up ${insight.percentageChange}% from last month.`}
          </p>
          <p className="mono" style={{ fontSize: '0.85rem', color: 'var(--ink-soft)' }}>
            {formatMoney(insight.previousPeriodDiscretionary)} → {formatMoney(insight.currentPeriodDiscretionary)}
          </p>
        </div>
      )}

      <h3 style={{ fontSize: '1rem', marginTop: 32 }}>6-month trend</h3>
      <div style={{ height: 220, marginBottom: 32 }}>
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={trend} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
            <CartesianGrid stroke="var(--line)" vertical={false} />
            <XAxis dataKey="label" tick={{ fontSize: 12, fontFamily: 'Inter' }} stroke="var(--ink-soft)" />
            <YAxis tick={{ fontSize: 11, fontFamily: 'IBM Plex Mono' }} stroke="var(--ink-soft)" />
            <Tooltip formatter={(v) => formatMoney(v)} />
            <Line type="monotone" dataKey="income" stroke="#2F6D5C" strokeWidth={2} dot={false} name="Income" />
            <Line type="monotone" dataKey="expense" stroke="#B3472B" strokeWidth={2} dot={false} name="Expense" />
          </LineChart>
        </ResponsiveContainer>
      </div>

      <h3 style={{ fontSize: '1rem' }}>Spending by category</h3>
      {breakdown.length === 0 ? (
        <p className="empty-state">No expenses recorded this month yet.</p>
      ) : (
        <div style={{ marginTop: 12 }}>
          {breakdown.sort((a, b) => Number(b.total) - Number(a.total)).map((row) => (
            <div key={row.categoryName} style={{ marginBottom: 10 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: 4 }}>
                <span>{row.categoryName}</span>
                <span className="mono">{formatMoney(row.total)}</span>
              </div>
              <div className="budget-bar-track">
                <div
                  className="budget-bar-fill"
                  style={{ width: `${(Number(row.total) / maxBreakdown) * 100}%`, background: 'var(--primary)' }}
                />
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
