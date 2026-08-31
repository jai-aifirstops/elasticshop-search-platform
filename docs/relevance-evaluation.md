# ElasticShop Search Relevance Evaluation

## Ranking strategy

Text relevance remains the primary ranking signal.

- Product name: 4x
- Brand: 2.5x
- Category: 1.5x
- Description: 1.5x
- Exact normalized name: strong boost
- Product-name phrase: strong boost
- Rating: small secondary `function_score` signal

## Autocomplete

`products_v2` adds `nameSearch` using Elasticsearch `search_as_you_type`.

Autocomplete searches:

- `nameSearch`
- `nameSearch._2gram`
- `nameSearch._3gram`

with `multi_match` type `bool_prefix`.

## Evaluation cases

| Query | Expected result |
|---|---|
| Apple MacBook Air M4 | Exact product ranks first |
| macbook | MacBook products lead |
| MackBook | Fuzzy search finds MacBook |
| Apple Mac | Autocomplete suggests MacBooks |
| laptop + Apple + Laptops | Apple laptop results only |

## Index versioning

Application alias: `products`

Current physical index after Phase 9: `products_v2`.
