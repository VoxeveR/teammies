import { useEffect, useState, memo } from 'react';

interface QuizTimerProps {
      initialTime: number; // Initial remaining time in seconds
      onTimeExpired?: () => void; // Callback when timer reaches 0
}

function QuizTimer({ initialTime, onTimeExpired }: QuizTimerProps) {
      const [timeRemaining, setTimeRemaining] = useState<number>(initialTime);

      useEffect(() => {
            setTimeRemaining(initialTime);
      }, [initialTime]);

      useEffect(() => {
            if (timeRemaining <= 0) {
                  if (onTimeExpired) {
                        onTimeExpired();
                  }
                  return;
            }

            const interval = setInterval(() => {
                  setTimeRemaining((prev) => {
                        const newTime = prev - 1;
                        if (newTime <= 0) {
                              clearInterval(interval);
                              if (onTimeExpired) {
                                    onTimeExpired();
                              }
                              return 0;
                        }
                        return newTime;
                  });
            }, 1000);

            return () => clearInterval(interval);
      }, [timeRemaining, onTimeExpired]);

      return (
            <div className='w-full p-12'>
                  <div className='bg-quiz-green relative h-12 w-full overflow-hidden rounded-[30px]'>
                        {/* progress fill (positioned) */}
                        <div className='bg-quiz-dark-green absolute top-0 left-0 h-full transition-[width] duration-1000 ease-linear' style={{ width: `${(timeRemaining / initialTime) * 100}%` }} />

                        {/* centered label overlay - sits above the progress fill and always centered */}
                        <div className='pointer-events-none absolute inset-0 flex items-center justify-center'>
                              <h1 className='text-3xl font-bold text-white tabular-nums'>{timeRemaining}</h1>
                        </div>
                  </div>
            </div>
      );
}

export default memo(QuizTimer);
