export function exportToCSV(filename: string, rows: any[], headers?: Record<string, string>) {
  if (!rows || rows.length === 0) return
  const keys = headers ? Object.keys(headers) : Object.keys(rows[0])
  const headerRow = headers ? keys.map(k => headers[k]) : keys
  const csv = [headerRow]
  for (const row of rows) {
    csv.push(keys.map(k => formatCsvCell(row[k])))
  }
  const blob = new Blob([csv.map(r => r.join(',')).join('\n')], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', filename)
  link.click()
  URL.revokeObjectURL(url)
}

function formatCsvCell(value: any): string {
  if (value === null || value === undefined) return ''
  const s = String(value).replace(/"/g, '""')
  if (s.search(/([,\"\n])/g) >= 0) return `"${s}"`
  return s
}







