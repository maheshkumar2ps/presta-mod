import Link from 'next/link';
import { productsApi, categoriesApi } from '@/lib/api';
import { ProductCard } from '@/components/product/ProductCard';
import { ProductListing, Category } from '@/types';

// Prevent static generation at build time
export const dynamic = 'force-dynamic';

export default async function HomePage() {
  let products: ProductListing[] = [];
  let categories: Category[] = [];

  try {
    const [productsRes, categoriesRes] = await Promise.all([
      productsApi.list({ size: 8, sort: 'dateAdd,desc' }),
      categoriesApi.getTree(),
    ]);
    products = productsRes.data?.content || [];
    categories = categoriesRes.data || [];
  } catch (error) {
    // API not available, show empty state
    console.error('Failed to fetch data:', error);
  }

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Hero Section */}
      <section className="mb-12 rounded-2xl bg-gradient-to-r from-primary-600 to-primary-800 p-8 text-white md:p-12">
        <h1 className="mb-4 text-3xl font-bold md:text-5xl">
          Welcome to PrestaShop
        </h1>
        <p className="mb-6 text-lg text-primary-100 md:text-xl">
          Discover amazing products at great prices
        </p>
        <Link
          href="/category/home"
          className="btn-secondary btn-lg inline-flex"
        >
          Shop Now
        </Link>
      </section>

      {/* Categories */}
      <section className="mb-12">
        <h2 className="mb-6 text-2xl font-bold">Shop by Category</h2>
        <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
          {categories
            .filter((c) => !c.linkRewrite.includes('home'))
            .slice(0, 4)
            .map((category) => (
              <Link
                key={category.id}
                href={`/category/${category.linkRewrite}`}
                className="card flex flex-col items-center p-6 text-center transition-shadow hover:shadow-md"
              >
                <div className="mb-3 h-12 w-12 rounded-full bg-primary-100 flex items-center justify-center">
                  <span className="text-xl">📦</span>
                </div>
                <span className="font-medium">{category.name}</span>
              </Link>
            ))}
        </div>
      </section>

      {/* Featured Products */}
      <section>
        <div className="mb-6 flex items-center justify-between">
          <h2 className="text-2xl font-bold">New Arrivals</h2>
          <Link
            href="/category/home"
            className="text-primary-600 hover:text-primary-700"
          >
            View All →
          </Link>
        </div>
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
        {products.length === 0 && (
          <p className="text-center text-gray-500 py-12">
            No products available yet. Check back soon!
          </p>
        )}
      </section>
    </div>
  );
}
