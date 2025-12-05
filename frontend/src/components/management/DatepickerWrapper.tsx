// src/components/DatepickerWrapper.tsx
import { Datepicker, ThemeProvider } from 'flowbite-react';

interface DatepickerWrapperProps {
      value?: Date | null;
      onChange?: (date: Date | null) => void; // match Flowbite's type
      className?: string;
      placeholder?: string;
      theme?: any;
}

// Default theme
const defaultTheme: any = {
      root: { base: 'relative' },
      popup: {
            root: {
                  base: 'absolute bottom-full z-50 block pt-2',
                  inner: 'inline-block rounded-lg bg-white dark:bg-quiz-light-green p-4 shadow-lg border-2 border-quiz-dark-green',
            },
            header: {
                  title: 'px-2 py-3 text-center font-semibold text-quiz-dark-green',
                  selectors: {
                        base: 'mb-2 flex justify-between',
                        button: {
                              base: 'rounded-lg bg-white px-5 py-2.5 text-sm font-semibold text-gray-900 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-200 dark:bg-quiz-dark-green dark:text-quiz-white dark:hover:bg-gray-600',
                              prev: '',
                              next: '',
                              view: '',
                        },
                  },
                  footer: {
                        base: 'mt-2 flex space-x-2',
                        button: {
                              base: 'w-full rounded-lg px-5 py-2 text-center text-sm font-medium focus:ring-4 focus:ring-primary-300',
                              today: 'bg-primary-700 text-white hover:bg-primary-800 dark:bg-primary-600 dark:hover:bg-primary-700',
                              clear: 'border border-gray-300 bg-white text-gray-900 hover:bg-gray-100 dark:border-gray-600 dark:bg-gray-700 dark:text-white dark:hover:bg-gray-600',
                        },
                  },
            },
            views: {
                  days: {
                        header: {
                              base: 'mb-1 grid grid-cols-7',
                              title: 'h-6 text-center text-sm font-medium leading-6 dark:text-gray-700',
                        },
                        items: {
                              item: {
                                    selected: 'bg-quiz-light-green text-white',
                              },
                        },
                  },
            },
      },
};

export default function DatepickerWrapper({ value = null, onChange, className = '', placeholder = 'Select date', theme }: DatepickerWrapperProps) {
      return (
            <ThemeProvider theme={{ datepicker: theme || defaultTheme }}>
                  <div className={`datepicker-wrapper ${className}`}>
                        <Datepicker value={value} onChange={onChange} placeholder={placeholder} />
                  </div>
            </ThemeProvider>
      );
}
