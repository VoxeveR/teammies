import { useState, useEffect, useCallback } from 'react';
import QuizTimer from './QuizTimer.tsx';
import { type QuestionData } from '../../middleware/questionConverter';

interface TeamSelection {
      playerId: number;
      playerName: string;
      selectedIndex: number;
      selectedOption: string;
}

interface QuestionProps {
      question: QuestionData;
      onTimeExpired?: () => void;
      onAnswerSelected?: (optionIndex: number, optionText: string) => void;
      teamSelections?: TeamSelection[];
      finalAnswer?: any;
}

//TODO: useQuestionTimer
//TODO: extract QuestionOption (button)
function Question({ question, onTimeExpired, onAnswerSelected, teamSelections = [], finalAnswer }: QuestionProps) {
      // Initialize remainingTime from question's timeLimit
      const [remainingTime, setRemainingTime] = useState<number>(question.timeLimit || 0);
      const [selectedIndex, setSelectedIndex] = useState<number | null>(null);

      useEffect(() => {
            console.log('component data:', question);
      }, [question]);

      useEffect(() => {
            console.log('Question changed, resetting timer. ID:', question.id, 'TimeLimit:', question.timeLimit);
            // Reset state when new question arrives
            if (question.timeLimit && question.timeLimit > 0) {
                  console.log('Setting remaining time to:', question.timeLimit);
                  setRemainingTime(question.timeLimit);
                  setSelectedIndex(null); // Reset selected option
            } else if (question.expiresAt) {
                  const now = Date.now();
                  const remainingMs = Math.max(0, question.expiresAt - now);
                  const remainingSeconds = Math.ceil(remainingMs / 1000);
                  console.log('Setting remaining time from expiresAt to:', remainingSeconds);
                  setRemainingTime(remainingSeconds);
                  setSelectedIndex(null); // Reset selected option
            }
      }, [question.id, question.timeLimit]);

      const handleTimeExpired = useCallback(() => {
            if (onTimeExpired) {
                  onTimeExpired();
            }
            console.log('Time expired for question:', question.id);
      }, [onTimeExpired, question.id]);

      const handleOptionClick = (index: number) => {
            // Prevent deselection - once an answer is selected, it stays selected
            if (index === selectedIndex) {
                  return;
            }

            setSelectedIndex(index);
            const selectedOption = question.options[index];
            console.log('Selected option index:', index, 'Option:', selectedOption);

            // Send answer to backend
            if (onAnswerSelected) {
                  onAnswerSelected(index, selectedOption);
            }
      };

      return (
            <div className='flex h-full min-h-0 w-full max-w-full grow flex-col pb-4 lg:pb-0'>
                  <div className='h-fit'>
                        <QuizTimer initialTime={remainingTime} onTimeExpired={handleTimeExpired} />
                  </div>
                  <div className='flex h-6/10 w-full max-w-full flex-col items-center p-4 font-bold text-black'>
                        <p className='max-w-full text-center text-3xl leading-tight wrap-break-word hyphens-auto sm:text-4xl md:text-5xl lg:text-6xl'>{question.question}</p>*{' '}
                        
                  </div>
                  <div className='grid h-2/5 min-h-0 w-full max-w-full gap-4 overflow-auto lg:grid-cols-2 lg:gap-4'>
                        {question.options.map((option, index) => {
                              const textLen = String.fromCharCode(65 + index).length + 2 + option.length;
                              const isLong = textLen > 80;
                              const optionTextClass = isLong ? 'text-sm md:text-base lg:text-lg' : 'text-lg lg:text-5xl';

                              // Find teammates who selected this option
                              const selectorsForThisOption = teamSelections.filter((s) => s.selectedIndex === index);

                              // Check if this is the final answer and determine styling
                              const isFinalAnswerOption = finalAnswer && finalAnswer.finalAnswerIndex === index;
                              const isCorrectAnswerOption = finalAnswer && finalAnswer.correctAnswerIndex === index;
                              const isFinalAnswerCorrect = finalAnswer && finalAnswer.isCorrect;
                              const hasCorrectAnswer = finalAnswer && finalAnswer.correctAnswerIndex !== null && finalAnswer.correctAnswerIndex !== undefined;

                              let buttonColor = 'bg-quiz-dark-green border-gray-600 text-white hover:scale-100 hover:bg-[#0a4a4d]';

                              if (isFinalAnswerOption) {
                                    if (isFinalAnswerCorrect) {
                                          // Answer is good - highlight green
                                          buttonColor = 'scale-100 border-white bg-green-500 text-white shadow-lg';
                                    } else if (hasCorrectAnswer) {
                                          // Answer is bad - highlight orange
                                          buttonColor = 'scale-100 border-white bg-orange-500 text-white shadow-lg';
                                    } else {
                                          // No correct answer provided - highlight red
                                          buttonColor = 'scale-100 border-white bg-red-600 text-white shadow-lg';
                                    }
                              } else if (isCorrectAnswerOption && finalAnswer && !finalAnswer.isCorrect) {
                                    // Show correct answer when answer was wrong
                                    buttonColor = 'scale-100 border-white bg-green-500 text-white shadow-lg';
                              } else if (selectedIndex === index) {
                                    buttonColor = 'scale-100 border-white bg-[#1CABB0] text-white shadow-lg';
                              }

                              return (
                                    <div key={index} className='relative'>
                                          <button
                                                onClick={() => handleOptionClick(index)}
                                                className={`relative box-border block w-full max-w-full min-w-0 overflow-visible rounded-2xl border p-4 text-start transition-all duration-200 lg:h-full ${buttonColor}`}
                                          >
                                                <span className={`${optionTextClass} wrap-break-word whitespace-normal`}>
                                                      {String.fromCharCode(65 + index)}. {option}
                                                </span>

                                                {/* Teammate selections in top right */}
                                                {selectorsForThisOption.length > 0 && (
                                                      <div className='absolute top-1 right-1 flex flex-wrap gap-1 pe-2'>
                                                            {selectorsForThisOption.map((selector) => (
                                                                  <div
                                                                        key={selector.playerId}
                                                                        className='bg-quiz-green text-quiz-white flex h-6 w-6 items-center justify-center rounded-full p-5 text-xl font-bold'
                                                                        title={selector.playerName}
                                                                  >
                                                                        {selector.playerName.charAt(0).toUpperCase()}
                                                                  </div>
                                                            ))}
                                                      </div>
                                                )}
                                          </button>
                                    </div>
                              );
                        })}
                  </div>

                  <div className='h-6 lg:hidden' />
            </div>
      );
}

export default Question;
