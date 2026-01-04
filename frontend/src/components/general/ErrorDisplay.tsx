import { NavLink } from 'react-router-dom';

type Props = {
      title: string;
      message: string;
      stack?: string;
      isFullHeight?: boolean;
};

export default function ErrorDisplay({ title, message, stack, isFullHeight = false }: Props) {
      return (
            <div className={`bg-quiz-white flex ${isFullHeight ? 'h-full' : 'h-screen'} w-full flex-col items-center justify-center gap-1.5 lg:bg-transparent`}>
                  <div className='text-quiz-green lg:text-quiz-white text-8xl lg:text-[350px]'>Oops!</div>
                  <h1 className='text-quiz-dark-green lg:text-quiz-white max-w-[80%] text-3xl font-bold text-wrap wrap-break-word lg:text-5xl'>{title}</h1>
                  <p className='text-quiz-light-green lg:text-quiz-white max-w-[60%] text-center text-[15px] text-wrap wrap-break-word lg:w-[35%] lg:text-xl'>{message}</p>
                  {import.meta.env.DEV && stack && (
                        <pre className='text-quiz-light-green lg:text-quiz-white mt-2 max-h-52 w-full overflow-auto rounded bg-transparent p-2 text-center text-sm whitespace-pre-wrap'>{stack}</pre>
                  )}
                  <NavLink
                        to='/'
                        className='bg-quiz-dark-green text-quiz-white lg:text-quiz-dark-green lg:bg-quiz-white mt-1.5 w-[60%] rounded-2xl ps-4 pe-4 pt-2 pb-2 text-center text-[20px] lg:w-[20%] lg:p-7'
                  >
                        GO TO HOMEPAGE
                  </NavLink>
            </div>
      );
}
