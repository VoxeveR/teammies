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
      root: { base: 'relative dark:text-dark-green' },
      input: {
            field: {
                  input: {
                        base: 'bg-quiz-green text-quiz-white rounded-2xl border-0 dark:text-quiz-dark-green',
                  },
                  icon: {
                        base: 'dark:text-quiz-dark-green', // 🔹 Icon color in dark mode
                  },
            },
      },
      popup: {
            root: {
                  base: 'absolute top-10 z-50 block pt-2',
                  inline: 'relative top-0 z-auto',
                  inner: 'inline-block rounded-lg bg-white p-4 shadow-lg dark:bg-quiz-green',
                  colors: {
                        custom: 'bg-quiz-light-green border-quiz-dark-green text-white dark:bg-quiz-dark-green dark:text-quiz-white',
                  },
            },
            header: {
                  title: 'px-2 py-3 text-center font-semibold text-quiz-dark-green',
                  selectors: {
                        base: 'mb-2 flex justify-between  ',
                        button: {
                              base: 'rounded-lg bg-white px-5 py-2.5 text-sm font-semibold text-gray-900 dark:hover:bg-quiz-light-green focus:outline-none focus:ring-2 focus:ring-gray-200 dark:bg-quiz-dark-green dark:text-quiz-white dark:hover:bg-quiz-light-green',
                              prev: '',
                              next: '',
                              view: '',
                        },
                  },
            },
            footer: {
                  base: 'mt-2 flex space-x-2',
                  button: {
                        base: 'w-full rounded-lg px-5 py-2 text-center text-sm font-medium focus:ring-4 focus:ring-primary-300',
                        today: 'dark:bg-quiz-dark-green dark:hover:bg-quiz-light-green dark:text-quiz-white',
                        clear: 'border border-gray-300 bg-white text-gray-900 hover:bg-gray-100 dark:bg-quiz-green dark:text-quiz-white dark:hover:bg-quiz-light-green dark:border-2 dark:border-quiz-dark-green',
                  },
            },
      },
      views: {
            days: {
                  header: {
                        base: 'mb-1 grid grid-cols-7',
                        title: 'h-6 text-center text-sm font-medium leading-6 text-quiz-white dark:text-quiz-light-green',
                  },
                  items: {
                        item: {
                              base: 'block flex-1 cursor-pointer rounded-lg border-0 text-center text-sm font-semibold leading-9 text-gray-900 hover:bg-gray-100 dark:text-white dark:hover:bg-quiz-light-green',
                              selected: 'bg-quiz-light-green text-white ',
                        },
                  },
            },

            months: {
                  items: {
                        item: {
                              base: 'block flex-1 cursor-pointer rounded-lg px-2 py-2 text-center text-sm font-semibold text-gray-900 hover:bg-gray-100 dark:text-white dark:hover:bg-quiz-light-green',
                              selected: 'bg-quiz-light-green text-white',
                        },
                  },
            },

            /* ▼ NEW — YEARS */
            years: {
                  items: {
                        item: {
                              base: 'block flex-1 cursor-pointer rounded-lg px-2 py-2 text-center text-sm font-semibold text-gray-900 hover:bg-gray-100 dark:text-white dark:hover:bg-quiz-light-green',
                              selected: 'bg-quiz-light-green text-white',
                        },
                  },
            },

            /* ▼ NEW — DECADES */
            decades: {
                  items: {
                        item: {
                              base: 'block flex-1 cursor-pointer rounded-lg px-2 py-2 text-center text-sm font-semibold text-gray-900 hover:bg-gray-100 dark:text-white dark:hover:bg-quiz-light-green',
                              selected: 'bg-quiz-light-green text-white',
                        },
                  },
            },
      },
};

export default function DatepickerWrapper({ value = null, onChange, className = '', placeholder = 'Select date', theme }: DatepickerWrapperProps) {
      return (
            <ThemeProvider theme={{ datepicker: theme || defaultTheme }}>
                  <div className={`datepicker-wrapper ${className}`}>
                        <Datepicker value={value} onChange={onChange} placeholder={placeholder} minDate={new Date()} color='custom' className='bg-quiz-green text-quiz-white rounded-2xl border-0' />
                  </div>
            </ThemeProvider>
      );
}
