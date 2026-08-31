import { useEffect, useMemo, useRef, useState } from 'react'

const DEFAULT_PAGE_SIZE = 6

function money(value) {
  if (value === undefined || value === null) return '—'

  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(value)
}

function getTotal(data) {
  if (!data?.hits?.total) return 0

  if (typeof data.hits.total === 'number') {
    return data.hits.total
  }

  return data.hits.total.value ?? 0
}

function ProductCard({ hit }) {
  const product = hit._source || {}
  const highlightedName = hit.highlight?.name?.[0]

  return (
    <article className="product-card">
      <div className="product-card-top">
        <span className="category-pill">
          {product.category || 'Product'}
        </span>

        <span
          className={
            product.inStock
              ? 'stock-badge in-stock'
              : 'stock-badge out-stock'
          }
        >
          {product.inStock ? 'In stock' : 'Out of stock'}
        </span>
      </div>

      <div className="product-icon">
        {product.category === 'Laptops' && '💻'}
        {product.category === 'Smartphones' && '📱'}
        {product.category === 'Accessories' && '🖱️'}
        {!['Laptops', 'Smartphones', 'Accessories'].includes(product.category) && '📦'}
      </div>

      {highlightedName ? (
        <h3
          className="product-name"
          dangerouslySetInnerHTML={{
            __html: highlightedName,
          }}
        />
      ) : (
        <h3 className="product-name">
          {product.name}
        </h3>
      )}

      <p className="brand">
        {product.brand}
      </p>

      <p className="description">
        {product.description}
      </p>

      <div className="product-meta">
        <span className="rating">
          ★ {product.rating ?? '—'}
        </span>

        <strong className="price">
          {money(product.price)}
        </strong>
      </div>

      {typeof hit._score === 'number' && (
        <div className="score">
          Elasticsearch score: {hit._score.toFixed(3)}
        </div>
      )}
    </article>
  )
}

function App() {
  const [query, setQuery] = useState('')
  const [brand, setBrand] = useState('')
  const [category, setCategory] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [minRating, setMinRating] = useState('')
  const [inStockOnly, setInStockOnly] = useState(false)
  const [sort, setSort] = useState('relevance')

  const [page, setPage] = useState(0)
  const [pageSize] = useState(DEFAULT_PAGE_SIZE)

  const [results, setResults] = useState(null)
  const [facets, setFacets] = useState(null)
  const [suggestions, setSuggestions] = useState([])

  const [loading, setLoading] = useState(true)
  const [suggestionLoading, setSuggestionLoading] = useState(false)
  const [error, setError] = useState('')
  const [backendStatus, setBackendStatus] = useState('checking')

  const [showSuggestions, setShowSuggestions] = useState(false)

  const searchBoxRef = useRef(null)

  const brandBuckets =
    facets?.aggregations?.brands?.buckets || []

  const categoryBuckets =
    facets?.aggregations?.categories?.buckets || []

  const priceStats =
    facets?.aggregations?.price_stats || {}

  const averageRating =
    facets?.aggregations?.average_rating?.value

  const hits =
    results?.hits?.hits || []

  const total = getTotal(results)

  const totalPages = Math.max(
    1,
    Math.ceil(total / pageSize),
  )

  const activeFilters = useMemo(() => {
    const filters = []

    if (query) filters.push(`Search: "${query}"`)
    if (brand) filters.push(`Brand: ${brand}`)
    if (category) filters.push(`Category: ${category}`)
    if (maxPrice) filters.push(`Max: $${maxPrice}`)
    if (minRating) filters.push(`Rating: ${minRating}+`)
    if (inStockOnly) filters.push('In stock')

    return filters
  }, [
    query,
    brand,
    category,
    maxPrice,
    minRating,
    inStockOnly,
  ])

  useEffect(() => {
    async function bootstrap() {
      try {
        const [healthResponse, facetsResponse] =
          await Promise.all([
            fetch('/api/health'),
            fetch('/api/search/facets'),
          ])

        if (!healthResponse.ok) {
          throw new Error('Backend health check failed')
        }

        if (!facetsResponse.ok) {
          throw new Error('Facet request failed')
        }

        const healthData =
          await healthResponse.json()

        const facetData =
          await facetsResponse.json()

        setBackendStatus(
          healthData.backend === 'UP'
            ? 'up'
            : 'down',
        )

        setFacets(facetData)
      } catch (err) {
        setBackendStatus('down')
        setError(err.message)
      }
    }

    bootstrap()
  }, [])

  useEffect(() => {
    setPage(0)
  }, [
    query,
    brand,
    category,
    maxPrice,
    minRating,
    inStockOnly,
    sort,
  ])

  useEffect(() => {
    const controller = new AbortController()

    const timer = setTimeout(async () => {
      setLoading(true)
      setError('')

      try {
        const params = new URLSearchParams()

        if (query.trim()) {
          params.set('q', query.trim())
        }

        if (brand) {
          params.set('brand', brand)
        }

        if (category) {
          params.set('category', category)
        }

        if (maxPrice) {
          params.set('maxPrice', maxPrice)
        }

        if (minRating) {
          params.set('minRating', minRating)
        }

        if (inStockOnly) {
          params.set('inStock', 'true')
        }

        params.set('page', String(page))
        params.set('size', String(pageSize))
        params.set('sort', sort)

        const response = await fetch(
          `/api/search/advanced?${params.toString()}`,
          {
            signal: controller.signal,
          },
        )

        if (!response.ok) {
          throw new Error(
            `Search failed with HTTP ${response.status}`,
          )
        }

        const data = await response.json()

        setResults(data)
      } catch (err) {
        if (err.name !== 'AbortError') {
          setError(err.message)
        }
      } finally {
        setLoading(false)
      }
    }, 250)

    return () => {
      clearTimeout(timer)
      controller.abort()
    }
  }, [
    query,
    brand,
    category,
    maxPrice,
    minRating,
    inStockOnly,
    sort,
    page,
    pageSize,
  ])

  useEffect(() => {
    const trimmed = query.trim()

    if (trimmed.length < 2) {
      setSuggestions([])
      return
    }

    const controller = new AbortController()

    const timer = setTimeout(async () => {
      setSuggestionLoading(true)

      try {
        const params = new URLSearchParams({
          q: trimmed,
          size: '6',
        })

        const response = await fetch(
          `/api/search/autocomplete?${params.toString()}`,
          {
            signal: controller.signal,
          },
        )

        if (!response.ok) {
          return
        }

        const data = await response.json()

        const nextSuggestions =
          (data?.hits?.hits || [])
            .map((hit) => hit._source)
            .filter(Boolean)

        setSuggestions(nextSuggestions)

        if (nextSuggestions.length > 0) {
          setShowSuggestions(true)
        }
      } catch (err) {
        if (err.name !== 'AbortError') {
          console.error(err)
        }
      } finally {
        setSuggestionLoading(false)
      }
    }, 180)

    return () => {
      clearTimeout(timer)
      controller.abort()
    }
  }, [query])

  function clearFilters() {
    setQuery('')
    setBrand('')
    setCategory('')
    setMaxPrice('')
    setMinRating('')
    setInStockOnly(false)
    setSort('relevance')
    setSuggestions([])
    setShowSuggestions(false)
    setPage(0)
  }

  function chooseSuggestion(product) {
    setQuery(product.name)
    setSuggestions([])
    setShowSuggestions(false)
    setPage(0)
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand-block">
          <div className="logo-mark">
            E
          </div>

          <div>
            <div className="logo-title">
              ElasticShop
            </div>

            <div className="logo-subtitle">
              Search Engineering Lab
            </div>
          </div>
        </div>

        <div className="status-area">
          <span
            className={`status-dot ${
              backendStatus === 'up'
                ? 'status-up'
                : backendStatus === 'down'
                  ? 'status-down'
                  : ''
            }`}
          />

          <span>
            {backendStatus === 'up'
              ? 'Spring Boot + Elasticsearch connected'
              : backendStatus === 'down'
                ? 'Backend unavailable'
                : 'Checking backend'}
          </span>
        </div>
      </header>

      <main>
        <section className="hero">
          <div className="hero-copy">
            <span className="eyebrow">
              PRODUCTION SEARCH PROJECT
            </span>

            <h1>
              Find the right product
              <span> with Elasticsearch.</span>
            </h1>

            <p>
              Full-text relevance, filters, facets,
              autocomplete, fuzzy matching, sorting and
              pagination — backed by Spring Boot and
              Elasticsearch.
            </p>
          </div>

          <div className="architecture-card">
            <div>
              React
            </div>
            <span>→</span>
            <div>
              Spring Boot
            </div>
            <span>→</span>
            <div>
              Elasticsearch
            </div>
          </div>
        </section>

        <section className="search-panel">
          <div
            className="search-wrapper"
            ref={searchBoxRef}
          >
            <span className="search-icon">
              ⌕
            </span>

            <input
              className="search-input"
              value={query}
              onChange={(event) => {
                setQuery(event.target.value)
                setShowSuggestions(true)
              }}
              onFocus={() => {
                if (suggestions.length > 0) {
                  setShowSuggestions(true)
                }
              }}
              placeholder="Try MacBook, Apple laptop, Galaxy..."
              aria-label="Search products"
            />

            {query && (
              <button
                className="search-clear"
                onClick={() => {
                  setQuery('')
                  setSuggestions([])
                }}
                type="button"
              >
                ×
              </button>
            )}

            {showSuggestions &&
              query.trim().length >= 2 && (
                <div className="suggestions">
                  <div className="suggestion-title">
                    {suggestionLoading
                      ? 'Searching...'
                      : 'Autocomplete'}
                  </div>

                  {suggestions.map((product) => (
                    <button
                      type="button"
                      className="suggestion-row"
                      key={product.productId || product.name}
                      onClick={() =>
                        chooseSuggestion(product)
                      }
                    >
                      <span>
                        {product.name}
                      </span>

                      <small>
                        {product.brand} ·{' '}
                        {money(product.price)}
                      </small>
                    </button>
                  ))}

                  {!suggestionLoading &&
                    suggestions.length === 0 && (
                      <div className="suggestion-empty">
                        No suggestions yet
                      </div>
                    )}
                </div>
              )}
          </div>

          <div className="quick-stats">
            <div>
              <strong>
                {facets
                  ? brandBuckets.reduce(
                      (sum, bucket) =>
                        sum + bucket.doc_count,
                      0,
                    )
                  : '—'}
              </strong>
              <span>
                Indexed products
              </span>
            </div>

            <div>
              <strong>
                {brandBuckets.length || '—'}
              </strong>
              <span>
                Brands
              </span>
            </div>

            <div>
              <strong>
                {categoryBuckets.length || '—'}
              </strong>
              <span>
                Categories
              </span>
            </div>

            <div>
              <strong>
                {averageRating
                  ? averageRating.toFixed(2)
                  : '—'}
              </strong>
              <span>
                Avg. rating
              </span>
            </div>
          </div>
        </section>

        <section className="workspace">
          <aside className="filters">
            <div className="filter-heading">
              <div>
                <span className="eyebrow">
                  FILTER CONTEXT
                </span>
                <h2>
                  Refine results
                </h2>
              </div>

              <button
                type="button"
                className="text-button"
                onClick={clearFilters}
              >
                Clear
              </button>
            </div>

            <label className="filter-control">
              <span>
                Brand
              </span>

              <select
                value={brand}
                onChange={(event) =>
                  setBrand(event.target.value)
                }
              >
                <option value="">
                  All brands
                </option>

                {brandBuckets.map((bucket) => (
                  <option
                    key={bucket.key}
                    value={bucket.key}
                  >
                    {bucket.key} ({bucket.doc_count})
                  </option>
                ))}
              </select>
            </label>

            <label className="filter-control">
              <span>
                Category
              </span>

              <select
                value={category}
                onChange={(event) =>
                  setCategory(event.target.value)
                }
              >
                <option value="">
                  All categories
                </option>

                {categoryBuckets.map((bucket) => (
                  <option
                    key={bucket.key}
                    value={bucket.key}
                  >
                    {bucket.key} ({bucket.doc_count})
                  </option>
                ))}
              </select>
            </label>

            <label className="filter-control">
              <span>
                Maximum price
              </span>

              <input
                type="number"
                min="0"
                step="50"
                value={maxPrice}
                onChange={(event) =>
                  setMaxPrice(event.target.value)
                }
                placeholder={
                  priceStats.max
                    ? `Up to $${priceStats.max}`
                    : 'No maximum'
                }
              />

              {priceStats.min !== undefined && (
                <small>
                  Dataset range:{' '}
                  {money(priceStats.min)} –{' '}
                  {money(priceStats.max)}
                </small>
              )}
            </label>

            <label className="filter-control">
              <span>
                Minimum rating
              </span>

              <select
                value={minRating}
                onChange={(event) =>
                  setMinRating(event.target.value)
                }
              >
                <option value="">
                  Any rating
                </option>
                <option value="4.8">
                  4.8 ★ and above
                </option>
                <option value="4.5">
                  4.5 ★ and above
                </option>
                <option value="4">
                  4.0 ★ and above
                </option>
              </select>
            </label>

            <label className="checkbox-control">
              <input
                type="checkbox"
                checked={inStockOnly}
                onChange={(event) =>
                  setInStockOnly(event.target.checked)
                }
              />

              <span>
                In-stock products only
              </span>
            </label>

            <div className="tech-card">
              <span className="eyebrow">
                SEARCH PIPELINE
              </span>

              <code>
                multi_match
              </code>
              <code>
                bool.filter
              </code>
              <code>
                aggregations
              </code>
              <code>
                highlighting
              </code>
              <code>
                pagination
              </code>
            </div>
          </aside>

          <section className="results-section">
            <div className="results-toolbar">
              <div>
                <span className="eyebrow">
                  QUERY RESULTS
                </span>

                <h2>
                  {loading
                    ? 'Searching...'
                    : `${total} product${
                        total === 1 ? '' : 's'
                      } found`}
                </h2>
              </div>

              <label className="sort-control">
                <span>
                  Sort
                </span>

                <select
                  value={sort}
                  onChange={(event) =>
                    setSort(event.target.value)
                  }
                >
                  <option value="relevance">
                    Relevance
                  </option>
                  <option value="price_asc">
                    Price: Low → High
                  </option>
                  <option value="price_desc">
                    Price: High → Low
                  </option>
                  <option value="rating_desc">
                    Rating
                  </option>
                  <option value="name_asc">
                    Name
                  </option>
                </select>
              </label>
            </div>

            {activeFilters.length > 0 && (
              <div className="active-filters">
                {activeFilters.map((filter) => (
                  <span
                    className="filter-chip"
                    key={filter}
                  >
                    {filter}
                  </span>
                ))}
              </div>
            )}

            {error && (
              <div className="error-box">
                <strong>
                  Search error
                </strong>
                <span>
                  {error}
                </span>
              </div>
            )}

            {loading ? (
              <div className="loading-grid">
                {Array.from({
                  length: 6,
                }).map((_, index) => (
                  <div
                    className="skeleton-card"
                    key={index}
                  />
                ))}
              </div>
            ) : hits.length > 0 ? (
              <div className="product-grid">
                {hits.map((hit) => (
                  <ProductCard
                    key={hit._id}
                    hit={hit}
                  />
                ))}
              </div>
            ) : (
              <div className="empty-state">
                <div>
                  ⌕
                </div>

                <h3>
                  No products matched
                </h3>

                <p>
                  Try a broader search or clear some
                  filters.
                </p>

                <button
                  type="button"
                  onClick={clearFilters}
                >
                  Clear filters
                </button>
              </div>
            )}

            {!loading && total > 0 && (
              <div className="pagination">
                <button
                  type="button"
                  disabled={page === 0}
                  onClick={() =>
                    setPage((current) =>
                      Math.max(0, current - 1),
                    )
                  }
                >
                  ← Previous
                </button>

                <span>
                  Page {page + 1} of {totalPages}
                </span>

                <button
                  type="button"
                  disabled={
                    page + 1 >= totalPages
                  }
                  onClick={() =>
                    setPage((current) =>
                      current + 1,
                    )
                  }
                >
                  Next →
                </button>
              </div>
            )}
          </section>
        </section>
      </main>

      <footer>
        <div>
          <strong>
            ElasticShop Search Lab
          </strong>
          <span>
            React · Spring Boot · Elasticsearch
          </span>
        </div>

        <div>
          Portfolio search-engine project
        </div>
      </footer>
    </div>
  )
}

export default App