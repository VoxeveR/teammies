import { faPause, faPlay } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

export type Quiz = {
      name: string;
      date: string;
      status: 'Completed' | 'Live' | 'Upcoming';
};

export default function QuizTable({ mockedQuizzes }: { mockedQuizzes: Quiz[] }) {
      const statusStyles = {
            Completed: 'bg-green-200 text-green-800',
            Live: 'bg-blue-200 text-blue-800',
            Upcoming: 'bg-yellow-200 text-yellow-800',
      };

      return (
            <div className='bg-quiz-white mx-auto w-full overflow-hidden rounded-2xl border border-[#92D1D8] shadow-md'>
                  <table className='w-full text-center text-sm'>
                        <thead className='text-shadow-quiz-dark-green bg-quiz-dark-green text-quiz-white'>
                              <tr>
                                    <th className='p-6 font-semibold lg:p-2'></th>
                                    <th className='p-4 font-semibold'>QUIZ</th>
                                    <th className='p-4 font-semibold'>DATE</th>
                                    <th className='p-4 font-semibold'>STATUS</th>
                              </tr>
                        </thead>
                        <tbody>
                              {mockedQuizzes.map((quiz, idx) => (
                                    <tr key={idx} className='text-quiz-dark-green border-t border-[#92D1D8] transition hover:bg-[#b8e5eb]'>
                                          <td className=''>
                                                {quiz.status === 'Upcoming' && <FontAwesomeIcon icon={faPlay} />}
                                                {quiz.status === 'Live' && <FontAwesomeIcon icon={faPause} />}
                                          </td>
                                          <td className='p-4'>{quiz.name}</td>
                                          <td className='p-4'>{quiz.date}</td>
                                          <td className='p-4'>
                                                <span className={`rounded-xl px-3 py-1 text-xs font-semibold shadow-sm ${statusStyles[quiz.status]}`}>{quiz.status}</span>
                                          </td>
                                    </tr>
                              ))}
                        </tbody>
                  </table>
            </div>
      );
}
