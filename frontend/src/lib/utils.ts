import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Returns the image URL for display. Handles both:
 * - Relative URLs (e.g. /images/products/19/xxx.jpg): prepends API base so the
 *   browser can reach the backend (Docker port mapping).
 * - Absolute URLs (e.g. https://bucket.s3.region.amazonaws.com/...): returned as-is
 *   for S3 or other external image hosts.
 */
export function getImageUrl(relativeUrl: string | undefined | null): string | undefined {
  if (!relativeUrl) return undefined;
  // Absolute URLs (S3, etc.) pass through unchanged
  if (!relativeUrl.startsWith('/')) return relativeUrl;
  const apiBase = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';
  const base = apiBase.replace(/\/api\/v1\/?$/, '');
  return `${base}${relativeUrl}`;
}

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatPrice(price: number, currency = 'USD'): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
  }).format(price);
}

export function formatDate(date: string): string {
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

export function truncate(str: string, length: number): string {
  if (str.length <= length) return str;
  return str.slice(0, length) + '...';
}
