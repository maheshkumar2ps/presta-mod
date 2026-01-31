import { Metadata } from 'next';
import { notFound } from 'next/navigation';
import { categoriesApi } from '@/lib/api';
import { ProductCard } from '@/components/product/ProductCard';
import { Breadcrumb } from '@/components/product/Breadcrumb';
import { Pagination } from '@/components/ui/Pagination';
import { Suspense } from 'react';

// Prevent static generation at build time
export const dynamic = 'force-dynamic';

interface Props {
  params: { slug: string };
  searchParams: { page?: string; sort?: string };
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  try {
    const categoryRes = await categoriesApi.getBySlug(params.slug);
    const category = categoryRes.data;

    return {
      title: category.metaTitle || `${category.name} - PrestaShop`,
      description: category.metaDescription || category.description,
    };
  } catch {
    return {
      title: 'Category - PrestaShop',
    };
  }
}

async function CategoryProducts({ slug, page, sort }: { slug: string; page: number; sort: string }) {
  const productsRes = await categoriesApi.getProducts(slug, {
    page,
    size: 12,
    sort,
  });

  const products = productsRes.data?.content || [];
  const totalPages = productsRes.data?.totalPages || 1;

  return (
    <>
      {products.length > 0 ? (
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
          <p className="text-gray-500">No products found in this category.</p>
        </div>
      )}
    </>
  );
}

export default async function CategoryPage({ params, searchParams }: Props) {
  const page = Number(searchParams.page || 1) - 1;
  const sort = searchParams.sort || 'dateAdd,desc';

  let category;
  try {
    const categoryRes = await categoriesApi.getBySlug(params.slug);
    category = categoryRes.data;
  } catch {
    notFound();
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Breadcrumb */}
      {category.breadcrumb && (
        <div className="mb-6">
          <Breadcrumb items={category.breadcrumb.slice(0, -1)} current={category.name} />
        </div>
      )}

      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold">{category.name}</h1>
        {category.description && (
          <p className="mt-2 text-gray-600">{category.description}</p>
        )}
      </div>

      {/* Filters & Sort */}
      <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
        <div className="text-sm text-gray-500">
          Showing products in {category.name}
        </div>
        <select
          defaultValue={sort}
          className="input w-auto"
          onChange={(e) => {
            const url = new URL(window.location.href);
            url.searchParams.set('sort', e.target.value);
            url.searchParams.delete('page');
            window.location.href = url.toString();
          }}
        >
          <option value="dateAdd,desc">Newest First</option>
          <option value="price,asc">Price: Low to High</option>
          <option value="price,desc">Price: High to Low</option>
          <option value="name,asc">Name: A-Z</option>
          <option value="name,desc">Name: Z-A</option>
        </select>
      </div>

      {/* Products */}
      <Suspense
        fallback={
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
            {Array.from({ length: 8 }).map((_, i) => (
              <div key={i} className="card animate-pulse">
                <div className="aspect-square bg-gray-200" />
                <div className="p-4 space-y-2">
                  <div className="h-4 w-1/3 bg-gray-200 rounded" />
                  <div className="h-5 w-2/3 bg-gray-200 rounded" />
                  <div className="h-5 w-1/4 bg-gray-200 rounded" />
                </div>
              </div>
            ))}
          </div>
        }
      >
        <CategoryProducts slug={params.slug} page={page} sort={sort} />
      </Suspense>
    </div>
  );
}
