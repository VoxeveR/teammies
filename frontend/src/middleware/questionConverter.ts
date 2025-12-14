export interface QuestionData {
      id: number;
      question: string;
      options: string[];
      timeLimit?: number;
      expiresAt?: number; // Timestamp when question expires (milliseconds)
      correctAnswer?: string | string[];
      showAnswer?: boolean;
}

export interface BackendQuestionData {
      questionId: number;
      questionText: string;
      questionTime: number;
      questionTimeTimestamp: number | string; // Unix timestamp in milliseconds or ISO string
      questionType: string;
      points: number;
      position: number;
      totalQuestions: number;
      answerOptions: Array<{
            id: number;
            text: string;
            position: number;
            correct?: boolean;
      }>;
      eventType: string;
}

export function convertBackendQuestion(backendQuestion: BackendQuestionData): QuestionData {
      // Sort answerOptions by position to maintain order
      const sortedOptions = [...backendQuestion.answerOptions].sort((a, b) => a.position - b.position);

      // Convert expiresAt from ISO string to Unix timestamp (milliseconds)
      let expiresAtTimestamp = 0;
      if (typeof backendQuestion.questionTimeTimestamp === 'string') {
            // If it's an ISO string like "2025-12-13T17:13:00.261353200Z"
            expiresAtTimestamp = new Date(backendQuestion.questionTimeTimestamp).getTime();
      } else if (typeof backendQuestion.questionTimeTimestamp === 'number') {
            // If it's already a number, use it directly
            expiresAtTimestamp = backendQuestion.questionTimeTimestamp;
      }

      return {
            id: backendQuestion.questionId,
            question: backendQuestion.questionText,
            options: sortedOptions.map((opt) => opt.text),
            timeLimit: backendQuestion.questionTime,
            expiresAt: expiresAtTimestamp, // Unix timestamp in milliseconds
            correctAnswer: sortedOptions
                  .filter((opt) => opt.correct)
                  .map((opt) => opt.text)
                  .filter((answer) => answer.length > 0), // Filter out empty answers
            showAnswer: false,
      };
}
