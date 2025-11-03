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
                  <div className='h-12 w-full overflow-hidden rounded-[30px] bg-[#1CABB0]'>
                        <div
                              className={`flex h-full w-full items-center justify-center bg-[#083335] transition-[width] duration-1000 ease-linear`}
                              style={{ width: `${(timeRemaining / initialTime) * 100}%` }}
                        >
                              <h1 className='align-center absolute right-0 left-0 m-auto w-fit text-3xl font-bold text-white'>{timeRemaining}</h1>
                        </div>
                  </div>
            </div>
      );
}

export default memo(QuizTimer);
