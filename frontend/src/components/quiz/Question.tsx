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
            <div className='flex h-full w-full max-w-full flex-1 grow flex-col items-center justify-center pb-6 lg:pb-0'>
                  <QuizTimer initialTime={remainingTime} onTimeExpired={handleTimeExpired} />
                  <div className='flex h-3/5 w-full max-w-full flex-col items-center justify-center p-4 font-bold text-black'>
                        <p className='max-w-full text-center text-3xl leading-tight wrap-break-word hyphens-auto sm:text-4xl md:text-5xl lg:text-6xl'>{question.question}</p>
                        <img src='./src/assets/tower.png' className='aspect-auto max-h-full max-w-full p-4'></img>
                  </div>
                  <div className='grid w-full max-w-full flex-1 grow gap-3 p-8 lg:grid-cols-2 lg:gap-8'>
                        {question.options.map((option, index) => {
                              const textLen = String.fromCharCode(65 + index).length + 2 + option.length;
                              const isLong = textLen > 80;
                              const optionTextClass = isLong ? 'text-sm md:text-base lg:text-lg' : 'text-lg lg:text-5xl';

                              return (
                                    <button
                                          key={index}
                                          onClick={() => handleOptionClick(index)}
                                          className={`box-border block h-full w-full max-w-full min-w-0 rounded-2xl border p-4 text-start transition-all duration-200 last:mb-2 lg:min-h-36 lg:p-2 ${
                                                selectedIndex === index
                                                      ? 'scale-103 border-white bg-[#1CABB0] text-white shadow-lg'
                                                      : 'bg-quiz-dark-green border-gray-600 text-white hover:scale-102 hover:bg-[#0a4a4d]'
                                          }`}
                                    >
                                          <span className={`${optionTextClass} wrap-break-word text-ellipsis whitespace-normal`}>
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
