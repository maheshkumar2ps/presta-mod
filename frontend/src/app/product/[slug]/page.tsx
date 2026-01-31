import { Metadata } from 'next';
import { notFound } from 'next/navigation';
import { productsApi } from '@/lib/api';
import { ProductGallery } from '@/components/product/ProductGallery';
import { Breadcrumb } from '@/components/product/Breadcrumb';
import { formatPrice } from '@/lib/utils';
import { AddToCartButton } from './AddToCartButton';
import { VariantSelector } from './VariantSelector';

// Prevent static generation at build time
export const dynamic = 'force-dynamic';

interface Props {
  params: { slug: string };
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  try {
    const productRes = await productsApi.getBySlug(params.slug);
    const product = productRes.data;

    return {
      title: product.metaTitle || `${product.name} - PrestaShop`,
      description: product.metaDescription || product.descriptionShort,
      openGraph: {
        title: product.name,
        description: product.descriptionShort,
        images: product.coverImage ? [product.coverImage] : [],
      },
    };
  } catch {
    return {
      title: 'Product - PrestaShop',
    };
  }
}

export default async function ProductPage({ params }: Props) {
  let product;
  try {
    const productRes = await productsApi.getBySlug(params.slug);
    product = productRes.data;
  } catch {
    notFound();
  }

  const hasDiscount = product.salePrice && product.salePrice < product.price;
  const discountPercent = hasDiscount
    ? Math.round((1 - product.salePrice! / product.price) * 100)
    : 0;

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Breadcrumb */}
      {product.defaultCategory?.breadcrumb && (
        <div className="mb-6">
          <Breadcrumb
            items={product.defaultCategory.breadcrumb}
            current={product.name}
          />
        </div>
      )}

      <div className="grid gap-8 lg:grid-cols-2">
        {/* Gallery */}
        <ProductGallery
          images={product.images || []}
          productName={product.name}
        />

        {/* Product Info */}
        <div className="space-y-6">
          {/* Title */}
          <div>
            <h1 className="text-3xl font-bold">{product.name}</h1>
            {product.reference && (
              <p className="mt-1 text-sm text-gray-500">
                SKU: {product.reference}
              </p>
            )}
          </div>

          {/* Price */}
          <div className="flex items-center gap-4">
            {hasDiscount ? (
              <>
                <span className="text-3xl font-bold text-red-600">
                  {formatPrice(product.salePrice!)}
                </span>
                <span className="text-xl text-gray-400 line-through">
                  {formatPrice(product.price)}
                </span>
                <span className="rounded-full bg-red-100 px-3 py-1 text-sm font-medium text-red-600">
                  -{discountPercent}%
                </span>
              </>
            ) : (
              <span className="text-3xl font-bold">
                {formatPrice(product.price)}
              </span>
            )}
          </div>

          {/* Short Description */}
          {product.descriptionShort && (
            <p className="text-gray-600">{product.descriptionShort}</p>
          )}

          {/* Stock Status */}
          <div className="flex items-center gap-2">
            {product.inStock ? (
              <>
                <span className="h-3 w-3 rounded-full bg-green-500" />
                <span className="text-green-600">In Stock</span>
                {product.quantity <= 10 && (
                  <span className="text-sm text-orange-600">
                    (Only {product.quantity} left)
                  </span>
                )}
              </>
            ) : (
              <>
                <span className="h-3 w-3 rounded-full bg-red-500" />
                <span className="text-red-600">Out of Stock</span>
              </>
            )}
          </div>

          {/* Variants */}
          {product.variants && product.variants.length > 0 && (
            <VariantSelector variants={product.variants} />
          )}

          {/* Add to Cart */}
          <AddToCartButton inStock={product.inStock} />

          {/* Product Details */}
          <div className="border-t pt-6">
            <h2 className="mb-4 text-lg font-semibold">Product Details</h2>
            <dl className="grid grid-cols-2 gap-4 text-sm">
              {product.condition && (
                <>
                  <dt className="text-gray-500">Condition</dt>
                  <dd className="font-medium capitalize">
                    {product.condition.toLowerCase()}
                  </dd>
                </>
              )}
              {product.ean13 && (
                <>
                  <dt className="text-gray-500">EAN</dt>
                  <dd className="font-medium">{product.ean13}</dd>
                </>
              )}
              {product.weight && (
                <>
                  <dt className="text-gray-500">Weight</dt>
                  <dd className="font-medium">{product.weight} kg</dd>
                </>
              )}
            </dl>
          </div>
        </div>
      </div>

      {/* Full Description */}
      {product.description && (
        <div className="mt-12 border-t pt-8">
          <h2 className="mb-4 text-2xl font-bold">Description</h2>
          <div
            className="prose max-w-none text-gray-600"
            dangerouslySetInnerHTML={{ __html: product.description }}
          />
        </div>
      )}
    </div>
  );
}
