import Question from '../components/quiz/Question.tsx';
import QuizHeader from '../components/quiz/QuizHeader.tsx';

interface QuestionData {
      id: number;
      question: string;
      options: string[];
      timeLimit?: number;
      correctAnswer?: string | string[];
      showAnswer?: boolean;
}

function QuizPage() {
      const mockedQuestion: QuestionData = {
            id: 1,
            question: 'What is the capital of France?',
            options: ['Berlin', 'Madrid', 'Paris', 'Rome'],
            timeLimit: Date.now() + 31000,
            correctAnswer: 'Paris',
            showAnswer: false,
      };

      return (
            <div className='flex h-screen min-h-full flex-col'>
                  <QuizHeader quizTitle='Geography Quiz' questionNumber={1} questionCount={10} />

                  <main className='w-full flex-1 p-0 lg:overflow-hidden lg:p-8'>
                        <div className='bg-quiz-white mx-auto flex h-full w-full items-stretch justify-center ps-4 pe-4 pt-1 pb-4 lg:h-full lg:rounded-[30px] lg:p-4'>
                              <Question question={mockedQuestion} />
                        </div>
                  </main>
            </div>
      );
}

export default QuizPage;
