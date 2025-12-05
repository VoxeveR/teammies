import { faCirclePlus } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useEffect, useRef, useState } from 'react';

interface ModalWithFormProps {
      onSubmitData: (payload: any, quizId?: number) => void;
      initialData?: any; // for editing
      quizId?: number;
      onClose?: () => void;
}

interface Question {
      id: string;
      title: string;
      options: string[];
      correctIndex: number;
}

export default function ModalWithForm({ onSubmitData, initialData, quizId, onClose }: ModalWithFormProps) {
      const [isOpen, setIsOpen] = useState(false);
      const [published, setPublished] = useState(initialData?.published ?? true);
      const modalRef = useRef<HTMLDivElement>(null);
      const firstFieldRef = useRef<HTMLInputElement>(null);

      const [questions, setQuestions] = useState<Question[]>(
            initialData?.questions?.map((q: any) => ({
                  id: String(Date.now()) + Math.random(),
                  title: q.text,
                  options: q.answerOptions.map((a: any) => a.text),
                  correctIndex: q.answerOptions.findIndex((a: any) => a.correct),
            })) || [{ id: String(Date.now()) + Math.random(), title: '', options: ['', '', '', ''], correctIndex: 0 }]
      );

      // Open modal automatically if initialData exists (editing)
      useEffect(() => {
            if (initialData) setIsOpen(true);
      }, [initialData]);

      // Reset questions when opening modal for new quiz
      useEffect(() => {
            if (initialData && isOpen) {
                  setPublished(initialData.published ?? true);
                  setQuestions(
                        initialData.questions?.map((q: any) => ({
                              id: String(Date.now()) + Math.random(),
                              title: q.text,
                              options: q.answerOptions.map((a: any) => a.text),
                              correctIndex: q.answerOptions.findIndex((a: any) => a.correct),
                        })) || [{ id: String(Date.now()) + Math.random(), title: '', options: ['', '', '', ''], correctIndex: 0 }]
                  );
            } else if (!initialData && isOpen) {
                  setPublished(true);
                  setQuestions([{ id: String(Date.now()) + Math.random(), title: '', options: ['', '', '', ''], correctIndex: 0 }]);
            }
      }, [initialData, isOpen]);

      useEffect(() => {
            const onKeyDown = (e: KeyboardEvent) => {
                  if (e.key === 'Escape') close();
            };
            if (isOpen) {
                  document.addEventListener('keydown', onKeyDown);
                  document.body.style.overflow = 'hidden';
                  setTimeout(() => firstFieldRef.current?.focus(), 0);
            } else {
                  document.removeEventListener('keydown', onKeyDown);
                  document.body.style.overflow = '';
            }
            return () => {
                  document.removeEventListener('keydown', onKeyDown);
                  document.body.style.overflow = '';
            };
      }, [isOpen]);

      const close = () => {
            setIsOpen(false);
            onClose?.();
      };

      // --- Question helpers ---
      const addQuestion = () => setQuestions((qs) => [...qs, { id: String(Date.now()) + Math.random(), title: '', options: ['', '', '', ''], correctIndex: 0 }]);
      const removeQuestion = (id: string) => setQuestions((qs) => qs.filter((q) => q.id !== id));
      const updateQuestionTitle = (id: string, value: string) => setQuestions((qs) => qs.map((q) => (q.id === id ? { ...q, title: value } : q)));
      const updateQuestionOption = (id: string, idx: number, value: string) =>
            setQuestions((qs) => qs.map((q) => (q.id === id ? { ...q, options: q.options.map((opt, i) => (i === idx ? value : opt)) } : q)));
      const setCorrectIndex = (id: string, idx: number) => setQuestions((qs) => qs.map((q) => (q.id === id ? { ...q, correctIndex: idx } : q)));

      const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
            e.preventDefault();
            const formData = new FormData(e.currentTarget);
            const payload = {
                  title: formData.get('name'),
                  description: formData.get('message'),
                  published,
                  questions: questions.map((q, idx) => ({
                        text: q.title,
                        questionType: 'SINGLE_CHOICE',
                        points: 10,
                        position: idx + 1,
                        answerOptions: q.options.map((opt, i) => ({
                              text: opt,
                              correct: i === q.correctIndex,
                              position: i + 1,
                        })),
                  })),
            };

            onSubmitData(payload, quizId);
            close();
      };

      return (
            <div>
                  {!quizId && (
                        <button onClick={() => setIsOpen(true)} className='button flex'>
                              <FontAwesomeIcon icon={faCirclePlus} />
                              <div className='ps-2'>Add new quiz</div>
                        </button>
                  )}

                  {isOpen && (
                        <div className='fixed inset-0 z-50 flex items-center justify-center' aria-modal='true' role='dialog'>
                              <div className='absolute inset-0 bg-black/50' onClick={() => close()} />
                              <div
                                    ref={modalRef}
                                    tabIndex={-1}
                                    className='bg-quiz-white scrollbar relative z-10 max-h-[90vh] w-full max-w-3xl overflow-auto rounded-lg p-6 shadow-2xl'
                                    onClick={(e) => e.stopPropagation()}
                              >
                                    <button onClick={() => close()} className='absolute top-3 right-3 cursor-pointer rounded-md p-1 text-black focus:outline-none'>
                                          X
                                    </button>
                                    <h2 className='text-quiz-dark-green mb-4 text-5xl font-semibold'>{quizId ? 'Edit Quiz' : 'Quiz Creator'}</h2>

                                    <form onSubmit={handleSubmit} className='space-y-4'>
                                          <div>
                                                <label className='text-quiz-dark-green mb-1 block text-xl'>Quiz Name</label>
                                                <input
                                                      ref={firstFieldRef}
                                                      name='name'
                                                      type='text'
                                                      defaultValue={initialData?.title || ''}
                                                      required
                                                      className='input focus:ring-quiz-dark-green'
                                                      placeholder='Quiz title'
                                                />
                                          </div>

                                          <div>
                                                <label className='text-quiz-dark-green mb-1 block text-xl'>Description</label>
                                                <textarea
                                                      name='message'
                                                      rows={4}
                                                      defaultValue={initialData?.description || ''}
                                                      className='input focus:ring-quiz-dark-green'
                                                      placeholder='Describe this quiz...'
                                                />
                                          </div>

                                          <div>
                                                <label className='text-quiz-dark-green flex items-center gap-2 text-xl'>
                                                      <input type='checkbox' checked={published} onChange={(e) => setPublished(e.target.checked)} />
                                                      Published
                                                </label>
                                          </div>

                                          <hr className='my-4' />

                                          <div className='space-y-4'>
                                                {questions.map((q, qi) => (
                                                      <div key={q.id} className='rounded border p-4'>
                                                            <div className='flex items-start justify-between gap-4'>
                                                                  <div className='flex-1'>
                                                                        <label className='text-quiz-dark-green mb-1 block text-sm'>Question Name</label>
                                                                        <input
                                                                              type='text'
                                                                              value={q.title}
                                                                              onChange={(e) => updateQuestionTitle(q.id, e.target.value)}
                                                                              placeholder={`Question ${qi + 1}`}
                                                                              className='input focus:ring-quiz-dark-green'
                                                                        />
                                                                  </div>
                                                                  <div className='ml-4 shrink-0'>
                                                                        <button
                                                                              type='button'
                                                                              onClick={() => removeQuestion(q.id)}
                                                                              className='button focus:ring-quiz-dark-green'
                                                                              aria-label={`Remove Question ${qi + 1}`}
                                                                        >
                                                                              Remove
                                                                        </button>
                                                                  </div>
                                                            </div>

                                                            <div className='mt-3 space-y-2'>
                                                                  <div className='text-quiz-dark-green text-sm'>Answers (choose correct one):</div>
                                                                  {q.options.map((opt, idx) => (
                                                                        <label key={idx} className='flex items-center gap-3'>
                                                                              <input
                                                                                    type='radio'
                                                                                    name={`correct-${q.id}`}
                                                                                    checked={q.correctIndex === idx}
                                                                                    onChange={() => setCorrectIndex(q.id, idx)}
                                                                                    className='h-4 w-4'
                                                                              />
                                                                              <input
                                                                                    type='text'
                                                                                    value={opt}
                                                                                    onChange={(e) => updateQuestionOption(q.id, idx, e.target.value)}
                                                                                    className='input focus:ring-quiz-dark-green'
                                                                                    placeholder={`Answer ${idx + 1}`}
                                                                              />
                                                                        </label>
                                                                  ))}
                                                            </div>
                                                      </div>
                                                ))}

                                                <button type='button' onClick={addQuestion} className='button'>
                                                      Add question!
                                                </button>
                                          </div>

                                          <div className='flex justify-end gap-2'>
                                                <button type='button' onClick={() => close()} className='button bg-quiz-white! text-quiz-dark-green! rounded border px-4 py-2'>
                                                      Cancel
                                                </button>
                                                <button type='submit' className='button focus:ring-quiz-dark-green rounded'>
                                                      {quizId ? 'Update' : 'Create'}
                                                </button>
                                          </div>
                                    </form>
                              </div>
                        </div>
                  )}
            </div>
      );
}
