'use client';

import { useEffect } from 'react';

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center px-4">
      <h1 className="mb-2 text-6xl font-bold text-gray-300">Oops!</h1>
      <h2 className="mb-4 text-2xl font-semibold">Something went wrong</h2>
      <p className="mb-6 text-gray-600">
        We encountered an error while loading this page.
      </p>
      <button onClick={reset} className="btn-primary btn-lg">
        Try Again
      </button>
    </div>
  );
}
