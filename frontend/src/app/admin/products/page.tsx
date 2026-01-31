'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { adminProductsApi } from '@/lib/api';
import { Product } from '@/types';
import { formatPrice, formatDate } from '@/lib/utils';

export default function AdminProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [selected, setSelected] = useState<number[]>([]);

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const response = await adminProductsApi.list({ page, size: 10 });
      setProducts(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error('Failed to fetch products:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [page]);

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this product?')) return;

    try {
      await adminProductsApi.delete(id);
      fetchProducts();
    } catch (error) {
      alert('Failed to delete product');
    }
  };

  const handleBulkDelete = async () => {
    if (!confirm(`Delete ${selected.length} products?`)) return;

    try {
      await adminProductsApi.bulkDelete(selected);
      setSelected([]);
      fetchProducts();
    } catch (error) {
      alert('Failed to delete products');
    }
  };

  const handleBulkStatus = async (active: boolean) => {
    try {
      await adminProductsApi.bulkUpdateStatus(selected, active);
      setSelected([]);
      fetchProducts();
    } catch (error) {
      alert('Failed to update products');
    }
  };

  const toggleSelect = (id: number) => {
    setSelected((prev) =>
      prev.includes(id) ? prev.filter((i) => i !== id) : [...prev, id]
    );
  };

  const toggleSelectAll = () => {
    setSelected((prev) =>
      prev.length === products.length ? [] : products.map((p) => p.id)
    );
  };

  return (
    <div>
      {/* Actions */}
      <div className="mb-6 flex items-center justify-between">
        <div className="flex gap-2">
          {selected.length > 0 && (
            <>
              <button
                onClick={() => handleBulkStatus(true)}
                className="btn-secondary btn-sm"
              >
                Activate ({selected.length})
              </button>
              <button
                onClick={() => handleBulkStatus(false)}
                className="btn-secondary btn-sm"
              >
                Deactivate
              </button>
              <button
                onClick={handleBulkDelete}
                className="btn-sm bg-red-600 text-white hover:bg-red-700"
              >
                Delete
              </button>
            </>
          )}
        </div>
        <Link href="/admin/products/new" className="btn-primary btn-sm">
          Add Product
        </Link>
      </div>

      {/* Table */}
      <div className="card overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50">
            <tr>
              <th className="w-12 px-4 py-3">
                <input
                  type="checkbox"
                  checked={selected.length === products.length && products.length > 0}
                  onChange={toggleSelectAll}
                  className="rounded"
                />
              </th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                Product
              </th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                Price
              </th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                Stock
              </th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                Status
              </th>
              <th className="px-4 py-3 text-left text-sm font-medium text-gray-500">
                Updated
              </th>
              <th className="w-24 px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y">
            {loading ? (
              <tr>
                <td colSpan={7} className="px-4 py-8 text-center text-gray-500">
                  Loading...
                </td>
              </tr>
            ) : products.length === 0 ? (
              <tr>
                <td colSpan={7} className="px-4 py-8 text-center text-gray-500">
                  No products found
                </td>
              </tr>
            ) : (
              products.map((product) => (
                <tr key={product.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3">
                    <input
                      type="checkbox"
                      checked={selected.includes(product.id)}
                      onChange={() => toggleSelect(product.id)}
                      className="rounded"
                    />
                  </td>
                  <td className="px-4 py-3">
                    <div>
                      <p className="font-medium">{product.name}</p>
                      <p className="text-sm text-gray-500">{product.reference}</p>
                    </div>
                  </td>
                  <td className="px-4 py-3">{formatPrice(product.price)}</td>
                  <td className="px-4 py-3">
                    <span
                      className={
                        product.quantity > 10
                          ? 'text-green-600'
                          : product.quantity > 0
                          ? 'text-orange-600'
                          : 'text-red-600'
                      }
                    >
                      {product.quantity}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-flex rounded-full px-2 py-1 text-xs font-medium ${
                        product.active
                          ? 'bg-green-100 text-green-700'
                          : 'bg-gray-100 text-gray-700'
                      }`}
                    >
                      {product.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-500">
                    {product.dateUpd ? formatDate(product.dateUpd) : '-'}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      <Link
                        href={`/admin/products/${product.id}`}
                        className="text-sm text-primary-600 hover:text-primary-700"
                      >
                        Edit
                      </Link>
                      <button
                        onClick={() => handleDelete(product.id)}
                        className="text-sm text-red-600 hover:text-red-700"
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="mt-4 flex justify-center gap-2">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={page === 0}
            className="btn-outline btn-sm"
          >
            Previous
          </button>
          <span className="flex items-center px-4 text-sm">
            Page {page + 1} of {totalPages}
          </span>
          <button
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page === totalPages - 1}
            className="btn-outline btn-sm"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}
