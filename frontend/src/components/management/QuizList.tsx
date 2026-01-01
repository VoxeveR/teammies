import { faPause, faPlay } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Dropdown, DropdownItem } from 'flowbite-react';

export type Quiz = {
      id: number;
      name: string;
      date: string;
      status: 'Completed' | 'Live' | 'Upcoming';
};

interface QuizTableProps {
      mockedQuizzes: Quiz[];
      onQuizClick?: (quiz: Quiz) => void;
      onDeleteQuiz?: (quiz: Quiz) => void;
      onStartQuiz?: (quiz: Quiz) => void;
}

const dropdownTheme = {
      arrowIcon: 'ml-2 h-4 w-4',
      content: 'py-1 focus:outline-none dark:bg-quiz-green rounded-xl',
      floating: {
            animation: 'transition-opacity',
            arrow: {
                  base: 'absolute z-10 h-2 w-2 rotate-45',
                  style: {
                        dark: 'bg-gray-900 dark:bg-gray-700',
                        light: 'bg-white',
                        auto: 'bg-white dark:bg-gray-700',
                  },
                  placement: '-4px',
            },
            base: 'z-10 w-fit divide-y divide-gray-100 rounded shadow focus:outline-none',
            content: 'py-1 text-sm text-gray-700 dark:text-gray-200',
            divider: 'my-1 h-px bg-gray-100 dark:bg-gray-600',
            header: 'block px-4 py-2 text-sm text-gray-700 dark:text-gray-200',
            hidden: 'invisible opacity-0',
            item: {
                  container: 'dark:bg-quiz-green',
                  base: 'flex w-full cursor-pointer items-center justify-start px-4 py-2 text-sm text-gray-700 hover:bg-gray-100 focus:bg-gray-100 focus:outline-none dark:text-quiz-white dark:hover:bg-quiz-green dark:hover:text-white dark:focus:bg-quiz-green dark:focus:text-white',
                  icon: 'mr-2 h-4 w-4',
            },
            style: {
                  dark: 'bg-red-900 text-white dark:bg-gray-700',
                  light: 'border border-gray-200 bg-white text-gray-900',
                  auto: 'border border-gray-200 bg-white text-gray-900 dark:border-none dark:bg-gray-700 dark:text-white',
            },
            target: 'w-fit',
      },
      inlineWrapper: 'flex items-center',
};

export default function QuizTable({ mockedQuizzes, onQuizClick, onDeleteQuiz, onStartQuiz }: QuizTableProps) {
      const statusStyles = {
            Completed: 'bg-green-200 text-green-800',
            Live: 'bg-blue-200 text-blue-800',
            Upcoming: 'bg-yellow-200 text-yellow-800',
      };

      const hasActions = onQuizClick || onDeleteQuiz || onStartQuiz;

      return (
            <div className='bg-quiz-white mx-auto w-full overflow-hidden rounded-2xl border border-[#92D1D8] shadow-md'>
                  <table className='w-full text-center text-sm'>
                        <thead className='text-shadow-quiz-dark-green bg-quiz-green text-quiz-white'>
                              <tr>
                                    <th className='p-4 font-semibold'>QUIZ</th>
                                    <th className='p-4 font-semibold'>DATE</th>
                                    <th className='p-4 font-semibold'>STATUS</th>
                                    {hasActions && <th className='p-6'></th>}
                              </tr>
                        </thead>
                        <tbody>
                              {mockedQuizzes.map((quiz, idx) => (
                                    <tr key={idx} className='text-quiz-dark-green border-quiz-green cursor-pointer border-t transition hover:bg-[#b8e5eb]'>
                                          <td className='p-4'>{quiz.name}</td>
                                          <td className='p-4'>{quiz.date}</td>
                                          <td className='p-4'>
                                                <span className={`rounded-xl px-3 py-1 text-xs font-semibold shadow-sm ${statusStyles[quiz.status]}`}>{quiz.status}</span>
                                          </td>
                                          {hasActions && (
                                                <td className='p-4'>
                                                      <Dropdown
                                                            inline
                                                            label='...'
                                                            dismissOnClick={false}
                                                            className='w-fit bg-none'
                                                            theme={dropdownTheme}
                                                            placement='bottom-start'
                                                            renderTrigger={() => <span>...</span>}
                                                      >
                                                            {onStartQuiz && <DropdownItem onClick={() => onStartQuiz?.(quiz)}>Start Quiz</DropdownItem>}
                                                            {onQuizClick && <DropdownItem onClick={() => onQuizClick?.(quiz)}>Edit</DropdownItem>}
                                                            {onDeleteQuiz && <DropdownItem onClick={() => onDeleteQuiz?.(quiz)}>Delete</DropdownItem>}
                                                      </Dropdown>
                                                </td>
                                          )}
                                    </tr>
                              ))}
                        </tbody>
                  </table>
            </div>
      );
}
