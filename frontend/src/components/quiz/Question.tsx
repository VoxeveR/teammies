import { useState, useEffect, useCallback } from 'react';
import QuizTimer from './QuizTimer.tsx';

interface QuestionData {
      id: number;
      question: string;
      options: string[];
      timeLimit?: number;
      correctAnswer?: string | string[];
      showAnswer?: boolean;
}

interface QuestionProps {
      question: QuestionData;
      onTimeExpired?: () => void;
}

//TODO: useQuestionTimer
//TODO: extract QuestionOption (button)
function Question({ question, onTimeExpired }: QuestionProps) {
      const [remainingTime, setRemainingTime] = useState<number>(0);
      const [selectedIndex, setSelectedIndex] = useState<number | null>(null);

      useEffect(() => {
            if (question.timeLimit) {
                  const now = Date.now();
                  const timeLeft = Math.max(0, Math.floor((question.timeLimit - now) / 1000));
                  setRemainingTime(timeLeft);
            }
      }, [question.timeLimit]);

      const handleTimeExpired = useCallback(() => {
            if (onTimeExpired) {
                  onTimeExpired();
            }
            console.log('Time expired for question:', question.id);
      }, [onTimeExpired, question.id]);

      const handleOptionClick = (index: number) => {
            if (index === selectedIndex) {
                  setSelectedIndex(null);
                  return;
            }

            setSelectedIndex(index);
            console.log('Selected option index:', index, 'Option:', question.options[index]);
      };

      return (
            <div className='flex h-full min-h-0 w-full max-w-full grow flex-col pb-4 lg:pb-0'>
                  <div className='h-fit'>
                        <QuizTimer initialTime={remainingTime} onTimeExpired={handleTimeExpired} />
                  </div>
                  <div className='flex h-6/10 w-full max-w-full flex-col items-center p-4 font-bold text-black'>
                        <p className='max-w-full text-center text-3xl leading-tight wrap-break-word hyphens-auto sm:text-4xl md:text-5xl lg:text-6xl'>{question.question}</p>*{' '}
                        <img src='./src/assets/tower.png' className='max-h-64 max-w-64 overflow-hidden lg:max-h-fit lg:max-w-fit'></img> *
                  </div>
                  <div className='grid h-2/5 min-h-0 w-full max-w-full gap-4 overflow-auto lg:grid-cols-2 lg:gap-4'>
                        {question.options.map((option, index) => {
                              const textLen = String.fromCharCode(65 + index).length + 2 + option.length;
                              const isLong = textLen > 80;
                              const optionTextClass = isLong ? 'text-sm md:text-base lg:text-lg' : 'text-lg lg:text-5xl';

                              return (
                                    <button
                                          key={index}
                                          onClick={() => handleOptionClick(index)}
                                          className={`box-border block w-full max-w-full min-w-0 overflow-hidden rounded-2xl border p-4 text-start transition-all duration-200 lg:h-auto ${
                                                selectedIndex === index
                                                      ? 'scale-103 border-white bg-[#1CABB0] text-white shadow-lg'
                                                      : 'bg-quiz-dark-green border-gray-600 text-white hover:scale-102 hover:bg-[#0a4a4d]'
                                          }`}
                                    >
                                          <span className={`${optionTextClass} wrap-break-word whitespace-normal`}>
                                                {String.fromCharCode(65 + index)}. {option}
                                          </span>
                                    </button>
                              );
                        })}
                  </div>

                  {/* small spacer on mobile so the last option isn't flush to the viewport bottom */}
                  <div className='h-6 lg:hidden' />
            </div>
      );
}

export default Question;
