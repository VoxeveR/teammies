import Question from '../components/quiz/Question.tsx';

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
            <div className='h-full w-full overflow-x-hidden lg:ps-24 lg:pe-24 lg:pt-24'>
                  <div className='h-fit min-h-full w-full items-center justify-center bg-[#CAF5F7] lg:rounded-[30px]'>
                        <Question question={mockedQuestion} />
                  </div>
                  <div className='hidden h-24 lg:block'></div>
            </div>
      );
}

export default QuizPage;
