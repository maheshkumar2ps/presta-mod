'use client';

import Link from 'next/link';
import { usePathname, useSearchParams } from 'next/navigation';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
}

export function Pagination({ currentPage, totalPages }: PaginationProps) {
  const pathname = usePathname();
  const searchParams = useSearchParams();

  if (totalPages <= 1) return null;

  const createPageUrl = (page: number) => {
    const params = new URLSearchParams(searchParams);
    params.set('page', String(page));
    return `${pathname}?${params.toString()}`;
  };

  const pages = [];
  const showEllipsisStart = currentPage > 3;
  const showEllipsisEnd = currentPage < totalPages - 2;

  for (let i = 1; i <= totalPages; i++) {
    if (
      i === 1 ||
      i === totalPages ||
      (i >= currentPage - 1 && i <= currentPage + 1)
    ) {
      pages.push(i);
    } else if (
      (i === 2 && showEllipsisStart) ||
      (i === totalPages - 1 && showEllipsisEnd)
    ) {
      pages.push(-i); // negative means ellipsis
    }
  }

  return (
    <nav className="flex items-center justify-center gap-1">
      {/* Previous */}
      <Link
        href={createPageUrl(Math.max(1, currentPage - 1))}
        className={`btn-outline btn-sm ${
          currentPage === 1 ? 'pointer-events-none opacity-50' : ''
        }`}
      >
        Previous
      </Link>

      {/* Page Numbers */}
      {pages.map((page) =>
        page < 0 ? (
          <span key={page} className="px-2 text-gray-400">
            ...
          </span>
        ) : (
          <Link
            key={page}
            href={createPageUrl(page)}
            className={`btn-sm ${
              page === currentPage
                ? 'btn-primary'
                : 'btn-outline'
            }`}
          >
            {page}
          </Link>
        )
      )}

      {/* Next */}
      <Link
        href={createPageUrl(Math.min(totalPages, currentPage + 1))}
        className={`btn-outline btn-sm ${
          currentPage === totalPages ? 'pointer-events-none opacity-50' : ''
        }`}
      >
        Next
      </Link>
    </nav>
  );
}
