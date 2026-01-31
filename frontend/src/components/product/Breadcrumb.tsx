import Link from 'next/link';
import { Breadcrumb as BreadcrumbType } from '@/types';

interface BreadcrumbProps {
  items: BreadcrumbType[];
  current?: string;
}

export function Breadcrumb({ items, current }: BreadcrumbProps) {
  return (
    <nav className="flex items-center text-sm text-gray-500">
      <Link href="/" className="hover:text-primary-600">
        Home
      </Link>

      {items.map((item) => (
        <span key={item.id} className="flex items-center">
          <span className="mx-2">/</span>
          <Link
            href={`/category/${item.linkRewrite}`}
            className="hover:text-primary-600"
          >
            {item.name}
          </Link>
        </span>
      ))}

      {current && (
        <span className="flex items-center">
          <span className="mx-2">/</span>
          <span className="text-gray-900">{current}</span>
        </span>
      )}
    </nav>
  );
}
