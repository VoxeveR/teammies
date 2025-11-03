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
            <div className='flex h-full w-full flex-col items-center justify-center'>
                  <QuizTimer initialTime={remainingTime} onTimeExpired={handleTimeExpired} />
                  <div className='flex h-3/5 w-full max-w-full flex-col items-center justify-center p-4 font-bold text-black'>
                        <p className='max-w-full text-center text-3xl leading-tight wrap-break-word hyphens-auto sm:text-4xl md:text-5xl lg:text-6xl'>{question.question}</p>
                        <img src='./src/assets/tower.png' className='aspect-auto max-h-full max-w-full p-4'></img>
                  </div>
                  <div className='grid w-full flex-1 grid-cols-2 gap-8 p-8'>
                        {question.options.map((option, index) => (
                              <button
                                    key={index}
                                    onClick={() => handleOptionClick(index)}
                                    className={`block h-full w-full rounded-[15px] border p-2 text-5xl font-bold transition-all duration-200 ${
                                          selectedIndex === index
                                                ? 'scale-103 border-white bg-[#1CABB0] text-white shadow-lg'
                                                : 'border-gray-600 bg-[#083335] text-white hover:scale-102 hover:bg-[#0a4a4d]'
                                    }`}
                              >
                                    {String.fromCharCode(65 + index)}. {option}
                              </button>
                        ))}
                  </div>
            </div>
      );
}

export default Question;
