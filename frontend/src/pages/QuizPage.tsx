import Question from '../components/quiz/Question.tsx';
import QuizHeader from '../components/quiz/QuizHeader.tsx';
import { useLocation, useParams } from 'react-router-dom';
import { useState, useEffect, useRef } from 'react';
import { useQuizSessionData } from '../hooks/useQuizSessionData';
import * as StompJs from '@stomp/stompjs';
import { convertBackendQuestion, type QuestionData, type BackendQuestionData } from '../middleware/questionConverter';

interface TeamSelection {
      playerId: number;
      playerName: string;
      selectedIndex: number;
      selectedOption: string;
}

interface AnswerResult {
      questionId: number;
      finalAnswer: string;
      isCorrect: boolean;
      pointsAwarded: number;
      timestamp: string;
      correctAnswerIndex?: number;
}

function QuizPage() {
      const location = useLocation();
      const params = useParams();
      const { quizData } = useQuizSessionData();
      const [currentQuestion, setCurrentQuestion] = useState<QuestionData | null>(null);
      const [questionNumber, setQuestionNumber] = useState(1);
      const [questionCount, setQuestionCount] = useState(1);
      const [teamSelections, setTeamSelections] = useState<TeamSelection[]>([]);
      const [answerResult, setAnswerResult] = useState<AnswerResult | null>(null);

      // Get playerId from params or from quizData as fallback
      const playerId = params.playerId || quizData.quizPlayerId;

      const stompClientRef = useRef<StompJs.Client | null>(null);
      const answerResultSubscriptionRef = useRef<string | null>(null);

      useEffect(() => {
            if (!params.sessionCode || !params.teamCode) return;

            const stompClient = new StompJs.Client({
                  brokerURL: 'ws://localhost:8080/ws-quiz',
                  onConnect: () => {
                        console.log('Connected to WebSocket');

                        stompClient.subscribe(`/topic/quiz-session/${params.sessionCode}/events`, (message) => {
                              try {
                                    const event = JSON.parse(message.body);
                                    console.log('Received event:', event);

                                    if (event.eventType === 'QUESTION_SENT') {
                                          console.log('New question received:', event);
                                          const convertedQuestion = convertBackendQuestion(event);
                                          setCurrentQuestion(convertedQuestion);
                                          setQuestionNumber(event.position || 1);
                                          setQuestionCount(event.totalQuestions || 1);
                                          // Clear team selections and answer result for new question
                                          setTeamSelections([]);
                                          setAnswerResult(null);
                                    }
                              } catch (error) {
                                    console.error('Error parsing question message:', error);
                              }
                        });

                        // Subscribe to team member selections
                        stompClient.subscribe(`/topic/quiz-session/${params.sessionCode}/team/${params.teamCode}/selection`, (message) => {
                              try {
                                    const selection = JSON.parse(message.body);
                                    console.log('Team member selected:', selection);

                                    // Add or update team selection
                                    setTeamSelections((prev) => {
                                          const filtered = prev.filter((s) => s.playerId !== selection.playerId);
                                          return [...filtered, selection];
                                    });
                              } catch (error) {
                                    console.error('Error parsing selection message:', error);
                              }
                        });

                        // Subscribe to answer results
                        const subscription = stompClient.subscribe(`/topic/quiz-session/${params.sessionCode}/team/${params.teamCode}/answer-result`, (message) => {
                              try {
                                    const result = JSON.parse(message.body);
                                    console.log('Answer result received:', result, 'Current question ID:', currentQuestion?.id);

                                    // Only process if this result is for the current question
                                    if (!currentQuestion || result.questionId !== currentQuestion.id) {
                                          console.log('Ignoring answer result - not for current question');
                                          return;
                                    }

                                    // Find the correct answer index by matching the finalAnswer text against options
                                    let correctAnswerIndex = -1;
                                    correctAnswerIndex = currentQuestion.options.findIndex(
                                          (option) => option.toLowerCase() === result.finalAnswer.toString().toLowerCase()
                                    );
                                    console.log('Calculated correct answer index:', correctAnswerIndex, 'for answer:', result.finalAnswer);

                                    setAnswerResult({
                                          ...result,
                                          correctAnswerIndex,
                                    });
                              } catch (error) {
                                    console.error('Error parsing answer result message:', error);
                              }
                        });

                        answerResultSubscriptionRef.current = subscription.id;
                  },
                  onDisconnect: () => {
                        console.log('Disconnected from WebSocket');
                  },
                  onStompError: (frame) => {
                        console.error('STOMP Error:', frame.body);
                  },
            });

            stompClientRef.current = stompClient;
            stompClient.activate();

            return () => {
                  if (stompClientRef.current?.active) {
                        stompClientRef.current.deactivate();
                  }
            };
      }, [params.sessionCode, params.teamCode]);

      useEffect(() => {
            // Get first question from navigation state
            const firstQuestion = location.state?.firstQuestion;
            if (firstQuestion) {
                  // Convert backend format to frontend format
                  const convertedQuestion = convertBackendQuestion(firstQuestion);
                  setCurrentQuestion(convertedQuestion);
                  setQuestionNumber(firstQuestion.position || 1);
                  setQuestionCount(firstQuestion.totalQuestions || 1);
                  console.log('Loaded and converted question:', convertedQuestion);
            }
      }, [location.state]);

      const handleAnswerSelected = (optionIndex: number, optionText: string) => {
            if (!currentQuestion || !stompClientRef.current?.active) {
                  console.error('Cannot send answer: no current question or STOMP connection');
                  return;
            }

            const answerPayload = {
                  playerId,
                  questionId: currentQuestion.id,
                  selectedOption: optionText,
                  selectedIndex: optionIndex,
                  timestamp: Date.now(),
            };

            console.log('Sending answer to backend:', answerPayload);

            // Send answer via STOMP using publish
            stompClientRef.current.publish({
                  destination: `/app/quiz-session/${params.sessionCode}/team/${params.teamCode}/answer`,
                  body: JSON.stringify(answerPayload),
            });
      };

      if (!currentQuestion) {
            return (
                  <div className='flex h-screen items-center justify-center'>
                        <p>Loading question...</p>
                  </div>
            );
      }

      return (
            <div className='flex h-screen min-h-full flex-col'>
                  <QuizHeader quizTitle='Quiz' questionNumber={questionNumber} questionCount={questionCount} />

                  <main className='w-full flex-1 p-0 lg:overflow-hidden lg:p-8'>
                        <div className='bg-quiz-white mx-auto flex h-full w-full items-stretch justify-center ps-4 pe-4 pt-1 pb-4 lg:h-full lg:rounded-[30px] lg:p-4'>
                              <Question key={currentQuestion.id} question={currentQuestion} onAnswerSelected={handleAnswerSelected} teamSelections={teamSelections} answerResult={answerResult} />
                        </div>
                  </main>
            </div>
      );
}

export default QuizPage;
