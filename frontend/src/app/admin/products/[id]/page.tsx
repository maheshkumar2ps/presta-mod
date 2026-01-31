'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { adminProductsApi, adminCategoriesApi } from '@/lib/api';
import { Product, Category, ProductCreateDto } from '@/types';

export default function AdminProductFormPage() {
  const router = useRouter();
  const params = useParams();
  const isNew = params.id === 'new';
  const productId = isNew ? null : Number(params.id);

  const [loading, setLoading] = useState(!isNew);
  const [saving, setSaving] = useState(false);
  const [categories, setCategories] = useState<Category[]>([]);
  const [formData, setFormData] = useState<ProductCreateDto>({
    name: '',
    description: '',
    descriptionShort: '',
    price: 0,
    quantity: 0,
    reference: '',
    active: true,
    visibility: 'BOTH',
    condition: 'NEW',
    productType: 'STANDARD',
    categoryIds: [],
  });

  useEffect(() => {
    fetchCategories();
    if (!isNew && productId) {
      fetchProduct(productId);
    }
  }, [isNew, productId]);

  const fetchCategories = async () => {
    try {
      const response = await adminCategoriesApi.list();
      setCategories(response.data);
    } catch (error) {
      console.error('Failed to fetch categories:', error);
    }
  };

  const fetchProduct = async (id: number) => {
    try {
      const response = await adminProductsApi.getById(id);
      const product = response.data;
      setFormData({
        name: product.name,
        description: product.description || '',
        descriptionShort: product.descriptionShort || '',
        price: product.price,
        wholesalePrice: product.wholesalePrice,
        quantity: product.quantity,
        reference: product.reference || '',
        ean13: product.ean13 || '',
        weight: product.weight,
        active: product.active,
        visibility: product.visibility,
        condition: product.condition,
        productType: product.productType,
        onSale: product.onSale,
        metaTitle: product.metaTitle || '',
        metaDescription: product.metaDescription || '',
        defaultCategoryId: product.defaultCategory?.id,
        categoryIds: product.categories?.map((c) => c.id) || [],
      });
    } catch (error) {
      alert('Failed to load product');
      router.push('/admin/products');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);

    try {
      if (isNew) {
        await adminProductsApi.create(formData);
      } else {
        await adminProductsApi.update(productId!, formData);
      }
      router.push('/admin/products');
    } catch (error: any) {
      alert(error.message || 'Failed to save product');
    } finally {
      setSaving(false);
    }
  };

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value, type } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]:
        type === 'checkbox'
          ? (e.target as HTMLInputElement).checked
          : type === 'number'
          ? parseFloat(value) || 0
          : value,
    }));
  };

  const handleCategoryChange = (categoryId: number) => {
    setFormData((prev) => ({
      ...prev,
      categoryIds: prev.categoryIds?.includes(categoryId)
        ? prev.categoryIds.filter((id) => id !== categoryId)
        : [...(prev.categoryIds || []), categoryId],
    }));
  };

  if (loading) {
    return <div className="text-center py-8">Loading...</div>;
  }

  return (
    <div className="max-w-4xl">
      <div className="mb-6">
        <h2 className="text-2xl font-bold">
          {isNew ? 'Add New Product' : 'Edit Product'}
        </h2>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Basic Info */}
        <div className="card p-6">
          <h3 className="mb-4 text-lg font-semibold">Basic Information</h3>
          <div className="grid gap-4 md:grid-cols-2">
            <div className="md:col-span-2">
              <label className="label mb-1 block">Product Name *</label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                className="input"
                required
              />
            </div>
            <div>
              <label className="label mb-1 block">Reference/SKU</label>
              <input
                type="text"
                name="reference"
                value={formData.reference}
                onChange={handleChange}
                className="input"
              />
            </div>
            <div>
              <label className="label mb-1 block">EAN13</label>
              <input
                type="text"
                name="ean13"
                value={formData.ean13 || ''}
                onChange={handleChange}
                className="input"
              />
            </div>
            <div className="md:col-span-2">
              <label className="label mb-1 block">Short Description</label>
              <textarea
                name="descriptionShort"
                value={formData.descriptionShort}
                onChange={handleChange}
                className="input min-h-[80px]"
                rows={2}
              />
            </div>
            <div className="md:col-span-2">
              <label className="label mb-1 block">Full Description</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                className="input min-h-[150px]"
                rows={5}
              />
            </div>
          </div>
        </div>

        {/* Pricing & Stock */}
        <div className="card p-6">
          <h3 className="mb-4 text-lg font-semibold">Pricing & Stock</h3>
          <div className="grid gap-4 md:grid-cols-3">
            <div>
              <label className="label mb-1 block">Price *</label>
              <input
                type="number"
                name="price"
                value={formData.price}
                onChange={handleChange}
                className="input"
                step="0.01"
                min="0"
                required
              />
            </div>
            <div>
              <label className="label mb-1 block">Wholesale Price</label>
              <input
                type="number"
                name="wholesalePrice"
                value={formData.wholesalePrice || ''}
                onChange={handleChange}
                className="input"
                step="0.01"
                min="0"
              />
            </div>
            <div>
              <label className="label mb-1 block">Quantity</label>
              <input
                type="number"
                name="quantity"
                value={formData.quantity}
                onChange={handleChange}
                className="input"
                min="0"
              />
            </div>
            <div>
              <label className="label mb-1 block">Weight (kg)</label>
              <input
                type="number"
                name="weight"
                value={formData.weight || ''}
                onChange={handleChange}
                className="input"
                step="0.01"
                min="0"
              />
            </div>
          </div>
        </div>

        {/* Categories */}
        <div className="card p-6">
          <h3 className="mb-4 text-lg font-semibold">Categories</h3>
          <div>
            <label className="label mb-1 block">Default Category</label>
            <select
              name="defaultCategoryId"
              value={formData.defaultCategoryId || ''}
              onChange={handleChange}
              className="input"
            >
              <option value="">Select category</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {'—'.repeat(cat.levelDepth)} {cat.name}
                </option>
              ))}
            </select>
          </div>
          <div className="mt-4">
            <label className="label mb-2 block">Additional Categories</label>
            <div className="max-h-48 overflow-y-auto rounded border p-3">
              {categories.map((cat) => (
                <label
                  key={cat.id}
                  className="flex items-center gap-2 py-1"
                  style={{ paddingLeft: cat.levelDepth * 16 }}
                >
                  <input
                    type="checkbox"
                    checked={formData.categoryIds?.includes(cat.id) || false}
                    onChange={() => handleCategoryChange(cat.id)}
                    className="rounded"
                  />
                  {cat.name}
                </label>
              ))}
            </div>
          </div>
        </div>

        {/* Settings */}
        <div className="card p-6">
          <h3 className="mb-4 text-lg font-semibold">Settings</h3>
          <div className="grid gap-4 md:grid-cols-3">
            <div>
              <label className="label mb-1 block">Status</label>
              <select
                name="active"
                value={formData.active ? 'true' : 'false'}
                onChange={(e) =>
                  setFormData((prev) => ({ ...prev, active: e.target.value === 'true' }))
                }
                className="input"
              >
                <option value="true">Active</option>
                <option value="false">Inactive</option>
              </select>
            </div>
            <div>
              <label className="label mb-1 block">Visibility</label>
              <select
                name="visibility"
                value={formData.visibility}
                onChange={handleChange}
                className="input"
              >
                <option value="BOTH">Everywhere</option>
                <option value="CATALOG">Catalog only</option>
                <option value="SEARCH">Search only</option>
                <option value="NONE">Nowhere</option>
              </select>
            </div>
            <div>
              <label className="label mb-1 block">Condition</label>
              <select
                name="condition"
                value={formData.condition}
                onChange={handleChange}
                className="input"
              >
                <option value="NEW">New</option>
                <option value="USED">Used</option>
                <option value="REFURBISHED">Refurbished</option>
              </select>
            </div>
          </div>
          <div className="mt-4 flex gap-4">
            <label className="flex items-center gap-2">
              <input
                type="checkbox"
                name="onSale"
                checked={formData.onSale || false}
                onChange={handleChange}
                className="rounded"
              />
              On Sale
            </label>
            <label className="flex items-center gap-2">
              <input
                type="checkbox"
                name="onlineOnly"
                checked={formData.onlineOnly || false}
                onChange={handleChange}
                className="rounded"
              />
              Online Only
            </label>
          </div>
        </div>

        {/* SEO */}
        <div className="card p-6">
          <h3 className="mb-4 text-lg font-semibold">SEO</h3>
          <div className="space-y-4">
            <div>
              <label className="label mb-1 block">Meta Title</label>
              <input
                type="text"
                name="metaTitle"
                value={formData.metaTitle || ''}
                onChange={handleChange}
                className="input"
              />
            </div>
            <div>
              <label className="label mb-1 block">Meta Description</label>
              <textarea
                name="metaDescription"
                value={formData.metaDescription || ''}
                onChange={handleChange}
                className="input"
                rows={3}
              />
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex gap-4">
          <button
            type="button"
            onClick={() => router.back()}
            className="btn-outline btn-lg"
          >
            Cancel
          </button>
          <button type="submit" disabled={saving} className="btn-primary btn-lg">
            {saving ? 'Saving...' : isNew ? 'Create Product' : 'Save Changes'}
          </button>
        </div>
      </form>
    </div>
  );
}
