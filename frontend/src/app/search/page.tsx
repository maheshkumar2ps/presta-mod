import { Metadata } from 'next';
import { productsApi } from '@/lib/api';
import { ProductCard } from '@/components/product/ProductCard';
import { Pagination } from '@/components/ui/Pagination';
import { ProductListing } from '@/types';

// Prevent static generation at build time
export const dynamic = 'force-dynamic';

interface Props {
  searchParams: { q?: string; page?: string };
}

export async function generateMetadata({ searchParams }: Props): Promise<Metadata> {
  return {
    title: searchParams.q
      ? `Search: ${searchParams.q} - PrestaShop`
      : 'Search - PrestaShop',
  };
}

export default async function SearchPage({ searchParams }: Props) {
  const query = searchParams.q || '';
  const page = Number(searchParams.page || 1) - 1;

  let products: ProductListing[] = [];
  let totalPages = 1;
  let totalElements = 0;

  if (query) {
    try {
      const productsRes = await productsApi.search(query, { page, size: 12 });
      products = productsRes.data?.content || [];
      totalPages = productsRes.data?.totalPages || 1;
      totalElements = productsRes.data?.totalElements || 0;
    } catch (error) {
      console.error('Search failed:', error);
    }
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-3xl font-bold">Search Results</h1>
        {query && (
          <p className="mt-2 text-gray-600">
            {totalElements} results for &ldquo;{query}&rdquo;
          </p>
        )}
      </div>

      {!query ? (
        <div className="py-12 text-center">
          <p className="text-gray-500">Enter a search term to find products.</p>
        </div>
      ) : products.length > 0 ? (
        <>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {products.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
          <div className="mt-8">
            <Pagination currentPage={page + 1} totalPages={totalPages} />
          </div>
        </>
      ) : (
        <div className="py-12 text-center">
          <p className="text-gray-500">
            No products found for &ldquo;{query}&rdquo;. Try a different search term.
          </p>
        </div>
      )}
    </div>
  );
}
