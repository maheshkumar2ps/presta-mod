'use client';

import { useState } from 'react';
import { ProductVariant } from '@/types';
import { formatPrice } from '@/lib/utils';

interface VariantSelectorProps {
  variants: ProductVariant[];
}

export function VariantSelector({ variants }: VariantSelectorProps) {
  const [selectedVariant, setSelectedVariant] = useState<ProductVariant | null>(
    variants.find((v) => v.defaultOn) || variants[0] || null
  );

  return (
    <div className="space-y-4">
      <h3 className="font-medium">Select Variant</h3>
      <div className="flex flex-wrap gap-2">
        {variants.map((variant) => (
          <button
            key={variant.id}
            type="button"
            onClick={() => setSelectedVariant(variant)}
            className={`rounded-md border px-4 py-2 text-sm transition-colors ${
              selectedVariant?.id === variant.id
                ? 'border-primary-600 bg-primary-50 text-primary-600'
                : variant.inStock
                ? 'border-gray-300 hover:border-gray-400'
                : 'border-gray-200 text-gray-400 line-through'
            }`}
            disabled={!variant.inStock}
          >
            {variant.name}
            {variant.priceImpact !== 0 && (
              <span className="ml-1 text-xs text-gray-500">
                ({variant.priceImpact > 0 ? '+' : ''}
                {formatPrice(variant.priceImpact)})
              </span>
            )}
          </button>
        ))}
      </div>

      {selectedVariant && (
        <div className="text-sm text-gray-600">
          <p>
            Price:{' '}
            <span className="font-medium">{formatPrice(selectedVariant.price)}</span>
          </p>
          {selectedVariant.reference && (
            <p>
              SKU: <span className="font-medium">{selectedVariant.reference}</span>
            </p>
          )}
          <p>
            Stock:{' '}
            <span
              className={
                selectedVariant.inStock ? 'text-green-600' : 'text-red-600'
              }
            >
              {selectedVariant.inStock
                ? `${selectedVariant.quantity} available`
                : 'Out of stock'}
            </span>
          </p>
        </div>
      )}
    </div>
  );
}
