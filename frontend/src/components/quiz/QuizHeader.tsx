import { NavLink } from 'react-router-dom';

type QuizHeaderProps = {
      quizTitle: string;
      questionNumber: number;
      questionCount: number;
};

function QuizHeader({ quizTitle, questionNumber, questionCount }: QuizHeaderProps) {
      return (
            <div className='bg-quiz-white flex h-fit w-full flex-row flex-wrap items-center justify-center ps-4 first:pt-4 lg:h-18 lg:flex-row lg:flex-nowrap lg:justify-start lg:pb-4'>
                  <NavLink className='flex items-center' to='/'>
                        <img src='/src/assets/logo.svg' className='h-12 w-12 lg:h-16 lg:w-16' />
                  </NavLink>
                  <div className='ml-2 font-[Bungee] text-3xl'>TEAMMIES</div>
                  <div className='flex w-full flex-row justify-between gap-4 pe-4 pt-2 lg:justify-end lg:pt-2'>
                        <div>{quizTitle}</div>
                        <div>
                              Question {questionNumber} of {questionCount}
                        </div>
                  </div>
            </div>
      );
}

export default QuizHeader;
