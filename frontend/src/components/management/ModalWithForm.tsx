import { faCirclePlus } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useEffect, useRef, useState } from 'react';

interface ModalWithFormProps {
      onSubmitData?: (payload: any) => void;
}

export default function ModalWithForm({ onSubmitData }: ModalWithFormProps) {
      const [isOpen, setIsOpen] = useState(false);
      const modalRef = useRef<HTMLDivElement>(null);
      const firstFieldRef = useRef<HTMLInputElement>(null);

      // dynamiczne pytania
      const [questions, setQuestions] = useState(() => [
            // możemy zacząć z jednym pustym pytaniem lub pustą listą
            {
                  id: String(Date.now()) + Math.random(),
                  title: '',
                  options: ['', '', '', ''],
                  correctIndex: 0,
            },
      ]);

      useEffect(() => {
            function onKeyDown(e: KeyboardEvent) {
                  if (e.key === 'Escape') setIsOpen(false);
            }
            if (isOpen) {
                  document.addEventListener('keydown', onKeyDown);
                  // lock scroll
                  document.body.style.overflow = 'hidden';
                  // focus first field
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

      // prosty trap focus: wraca fokus do modala kiedy klikniesz tab poza
      useEffect(() => {
            if (!isOpen) return;
            const handleFocus = (e: FocusEvent) => {
                  if (modalRef.current && !modalRef.current.contains(e.target as Node)) {
                        e.stopPropagation();
                        modalRef.current.focus();
                  }
            };
            document.addEventListener('focus', handleFocus, true);
            return () => document.removeEventListener('focus', handleFocus, true);
      }, [isOpen]);

      const addQuestion = () => {
            setQuestions((q) => [
                  ...q,
                  {
                        id: String(Date.now()) + Math.random(),
                        title: '',
                        options: ['', '', '', ''],
                        correctIndex: 0,
                  },
            ]);
      };

      const removeQuestion = (id: string) => {
            setQuestions((q) => q.filter((item) => item.id !== id));
      };

      const updateQuestionTitle = (id: string, value: string) => {
            setQuestions((q) => q.map((item) => (item.id === id ? { ...item, title: value } : item)));
      };

      const updateQuestionOption = (id: string, optionIndex: number, value: string) => {
            setQuestions((q) => q.map((item) => (item.id === id ? { ...item, options: item.options.map((opt, i) => (i === optionIndex ? value : opt)) } : item)));
      };

      const setCorrectIndex = (id: string, index: number) => {
            setQuestions((q) => q.map((item) => (item.id === id ? { ...item, correctIndex: index } : item)));
      };

      const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
            e.preventDefault();
            const data = new FormData(e.currentTarget);
            const basic = Object.fromEntries(data.entries());
            // checkbox 'subscribe' returns "on" if checked, else null -> normalize
            const payloadBasic = {
                  ...basic,
                  subscribe: data.get('subscribe') !== null,
            };

            // Dołączamy pytania ze stanu (z typami prostymi)
            const payload = {
                  ...payloadBasic,
                  questions: questions.map((q) => ({
                        title: q.title,
                        options: q.options.slice(),
                        correctIndex: q.correctIndex,
                  })),
            };

            console.log('Wysłane dane:', payload);
            onSubmitData?.(payload);
            setIsOpen(false);
      };

      return (
            <div className='p-6'>
                  <button onClick={() => setIsOpen(true)} className='button flex'>
                        <FontAwesomeIcon icon={faCirclePlus} />
                        <div className='ps-2'>Add new quiz</div>
                  </button>

                  {isOpen && (
                        <div className='fixed inset-0 z-50 flex items-center justify-center' aria-modal='true' role='dialog'>
                              {/* overlay */}
                              <div className='absolute inset-0 bg-black/50' onClick={() => setIsOpen(false)} />

                              {/* modal */}
                              <div
                                    ref={modalRef}
                                    tabIndex={-1}
                                    className='bg-quiz-white relative z-10 max-h-[90vh] w-full max-w-3xl overflow-auto rounded-lg p-6 shadow-2xl'
                                    onClick={(e) => e.stopPropagation()}
                              >
                                    <button onClick={() => setIsOpen(false)} aria-label='Zamknij' className='absolute top-3 right-3 rounded-md p-1 text-black focus:outline-none'>
                                          X
                                    </button>

                                    <h2 className='text-quiz-dark-green mb-4 text-5xl font-semibold'>Quiz Creator</h2>

                                    <form onSubmit={handleSubmit} className='space-y-4'>
                                          <div>
                                                <label className='text-quiz-dark-green mb-1 block text-xl'>Quiz Name</label>
                                                <input ref={firstFieldRef} name='name' type='text' required className='input focus:ring-quiz-dark-green' placeholder='Quiz title' />
                                          </div>

                                          <div>
                                                <label className='text-quiz-dark-green mb-1 block text-xl'>Description</label>
                                                <textarea name='message' rows={4} className='input focus:ring-quiz-dark-green' placeholder='Describe this quiz...' />
                                          </div>

                                          <div>
                                                <label className='text-quiz-dark-green mb-1 block text-xl'>Seconds per question</label>
                                                <input name='secondsPerQuestion' type='number' min={5} defaultValue={30} className='input' placeholder='30' aria-label='Seconds per question' />
                                                <p className='mt-1 text-sm text-gray-500'>Minimum 5 seconds.</p>
                                          </div>

                                          <hr className='my-4' />

                                          <div className='space-y-4'>
                                                {questions.length === 0 && <p className='text-sm text-gray-500'>No questions! Add your first question :)</p>}

                                                <div className='space-y-6'>
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
                                                                        <div className='grid gap-2 sm:grid-cols-1'>
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
                                                                                                placeholder={`Answer ${idx + 1}`}
                                                                                                className='input focus:ring-quiz-dark-green'
                                                                                          />
                                                                                    </label>
                                                                              ))}
                                                                        </div>
                                                                  </div>
                                                            </div>
                                                      ))}
                                                      <div className='flex items-center justify-center'>
                                                            <button type='button' onClick={addQuestion} className='button'>
                                                                  Add question!
                                                            </button>
                                                      </div>
                                                </div>
                                          </div>

                                          <div className='flex justify-end gap-2'>
                                                <button
                                                      type='button'
                                                      onClick={() => setIsOpen(false)}
                                                      className='button bg-quiz-white! text-quiz-dark-green! rounded border px-4 py-2 focus:outline-none'
                                                >
                                                      Cancel
                                                </button>
                                                <button type='submit' className='button focus:ring-quiz-dark-green 2 rounded focus:outline-none'>
                                                      Create
                                                </button>
                                          </div>
                                    </form>
                              </div>
                        </div>
                  )}
            </div>
      );
}
