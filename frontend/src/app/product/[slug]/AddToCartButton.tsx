'use client';

import { useState } from 'react';

interface AddToCartButtonProps {
  inStock: boolean;
}

export function AddToCartButton({ inStock }: AddToCartButtonProps) {
  const [quantity, setQuantity] = useState(1);

  const handleAddToCart = () => {
    // TODO: Implement cart functionality in Phase 2
    alert(`Added ${quantity} item(s) to cart`);
  };

  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:items-center">
      {/* Quantity Selector */}
      <div className="flex items-center rounded-md border">
        <button
          type="button"
          onClick={() => setQuantity((q) => Math.max(1, q - 1))}
          className="px-4 py-2 text-gray-600 hover:bg-gray-100"
          disabled={!inStock}
        >
          -
        </button>
        <input
          type="number"
          value={quantity}
          onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
          className="w-16 border-x py-2 text-center focus:outline-none"
          min="1"
          disabled={!inStock}
        />
        <button
          type="button"
          onClick={() => setQuantity((q) => q + 1)}
          className="px-4 py-2 text-gray-600 hover:bg-gray-100"
          disabled={!inStock}
        >
          +
        </button>
      </div>

      {/* Add to Cart Button */}
      <button
        type="button"
        onClick={handleAddToCart}
        disabled={!inStock}
        className="btn-primary btn-lg flex-1"
      >
        {inStock ? 'Add to Cart' : 'Out of Stock'}
      </button>
    </div>
  );
}
