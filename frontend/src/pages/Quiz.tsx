import Question from '../components/quiz/Question.tsx';

interface QuestionData {
      id: number;
      question: string;
      options: string[];
      timeLimit?: number;
      correctAnswer?: string | string[];
      showAnswer?: boolean;
}

function Quiz() {
      const mockedQuestion: QuestionData = {
            id: 1,
            question: 'What is the capital of France France France France France France France France France France France France France France France France France France France?',
            options: ['Berlin', 'Madrid', 'Paris', 'Rome'],
            timeLimit: Date.now() + 31000,
            correctAnswer: 'Paris',
            showAnswer: false,
      };

      return (
            <div className='h-full w-full overflow-hidden p-24'>
                  <div className='h-full w-full items-center justify-center rounded-[30px] bg-[#CAF5F7] opacity-75'>
                        <Question question={mockedQuestion} />
                  </div>
            </div>
      );
}

export default Quiz;
