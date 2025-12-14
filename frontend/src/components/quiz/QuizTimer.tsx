import { useEffect, useState, memo } from 'react';

interface QuizTimerProps {
      initialTime: number;
      onTimeExpired?: () => void;
}

function QuizTimer({ initialTime, onTimeExpired }: QuizTimerProps) {
      const [timeRemaining, setTimeRemaining] = useState<number>(initialTime);

      useEffect(() => {
            // Reset timer when initialTime changes
            setTimeRemaining(initialTime);
      }, [initialTime]);

      useEffect(() => {
            if (initialTime <= 0) {
                  if (onTimeExpired) {
                        onTimeExpired();
                  }
                  return;
            }

            // Start the countdown
            let interval: ReturnType<typeof setInterval>;

            const startCountdown = () => {
                  interval = setInterval(() => {
                        setTimeRemaining((prev) => {
                              const nextTime = prev - 1;

                              if (nextTime <= 0) {
                                    clearInterval(interval);
                                    if (onTimeExpired) {
                                          onTimeExpired();
                                    }
                                    return 0;
                              }

                              return nextTime;
                        });
                  }, 1000);
            };

            startCountdown();

            return () => {
                  if (interval) {
                        clearInterval(interval);
                  }
            };
      }, [initialTime, onTimeExpired]);

      return (
            <div className='w-full'>
                  <div className='bg-quiz-green relative h-6 w-full overflow-hidden rounded-[30px] lg:h-12'>
                        <div className='bg-quiz-dark-green absolute top-0 left-0 h-full transition-[width] duration-1000 ease-linear' style={{ width: `${(timeRemaining / initialTime) * 100}%` }} />

                        <div className='pointer-events-none absolute inset-0 flex items-center justify-center'>
                              <h1 className='text-xl font-bold text-white tabular-nums lg:text-3xl'>{timeRemaining}</h1>
                        </div>
                  </div>
            </div>
      );
}

export default QuizTimer;
