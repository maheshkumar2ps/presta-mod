import Link from 'next/link';
import Image from 'next/image';
import { ProductListing } from '@/types';
import { formatPrice, getImageUrl } from '@/lib/utils';

interface ProductCardProps {
  product: ProductListing;
}

export function ProductCard({ product }: ProductCardProps) {
  const hasDiscount = product.salePrice && product.salePrice < product.price;

  return (
    <Link href={`/product/${product.linkRewrite}`} className="group">
      <div className="card overflow-hidden transition-shadow hover:shadow-lg">
        {/* Image */}
        <div className="relative aspect-square bg-gray-100">
          {product.coverImage ? (
            <Image
              src={getImageUrl(product.coverImage)!}
              alt={product.name}
              fill
              className="object-cover transition-transform group-hover:scale-105"
              sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 25vw"
            />
          ) : (
            <div className="flex h-full items-center justify-center text-gray-400">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                strokeWidth={1.5}
                stroke="currentColor"
                className="h-12 w-12"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="m2.25 15.75 5.159-5.159a2.25 2.25 0 0 1 3.182 0l5.159 5.159m-1.5-1.5 1.409-1.409a2.25 2.25 0 0 1 3.182 0l2.909 2.909m-18 3.75h16.5a1.5 1.5 0 0 0 1.5-1.5V6a1.5 1.5 0 0 0-1.5-1.5H3.75A1.5 1.5 0 0 0 2.25 6v12a1.5 1.5 0 0 0 1.5 1.5Zm10.5-11.25h.008v.008h-.008V8.25Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Z"
                />
              </svg>
            </div>
          )}

          {/* Badges */}
          <div className="absolute left-2 top-2 flex flex-col gap-1">
            {product.onSale && (
              <span className="rounded bg-red-500 px-2 py-0.5 text-xs font-medium text-white">
                Sale
              </span>
            )}
            {!product.inStock && (
              <span className="rounded bg-gray-500 px-2 py-0.5 text-xs font-medium text-white">
                Out of Stock
              </span>
            )}
          </div>
        </div>

        {/* Content */}
        <div className="p-4">
          {/* Category */}
          {product.defaultCategory && (
            <span className="text-xs text-gray-500">
              {product.defaultCategory.name}
            </span>
          )}

          {/* Name */}
          <h3 className="mt-1 font-medium text-gray-900 line-clamp-2 group-hover:text-primary-600">
            {product.name}
          </h3>

          {/* Price */}
          <div className="mt-2 flex items-center gap-2">
            {hasDiscount ? (
              <>
                <span className="font-bold text-red-600">
                  {formatPrice(product.salePrice!)}
                </span>
                <span className="text-sm text-gray-400 line-through">
                  {formatPrice(product.price)}
                </span>
              </>
            ) : (
              <span className="font-bold text-gray-900">
                {formatPrice(product.price)}
              </span>
            )}
          </div>
        </div>
      </div>
    </Link>
  );
}
