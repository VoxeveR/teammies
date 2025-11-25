import React, { useEffect, useRef, useState } from "react";

export default function ModalWithForm() {
  const [isOpen, setIsOpen] = useState(false);
  const modalRef = useRef(null);
  const firstFieldRef = useRef(null);

  useEffect(() => {
    function onKeyDown(e) {
      if (e.key === "Escape") setIsOpen(false);
    }
    if (isOpen) {
      document.addEventListener("keydown", onKeyDown);
      // lock scroll
      document.body.style.overflow = "hidden";
      // focus first field
      setTimeout(() => firstFieldRef.current?.focus(), 0);
    } else {
      document.removeEventListener("keydown", onKeyDown);
      document.body.style.overflow = "";
    }

    return () => {
      document.removeEventListener("keydown", onKeyDown);
      document.body.style.overflow = "";
    };
  }, [isOpen]);

  // prosty trap focus: wraca fokus do modala kiedy klikniesz tab poza
  useEffect(() => {
    if (!isOpen) return;
    const handleFocus = (e) => {
      if (modalRef.current && !modalRef.current.contains(e.target)) {
        e.stopPropagation();
        modalRef.current.focus();
      }
    };
    document.addEventListener("focus", handleFocus, true);
    return () => document.removeEventListener("focus", handleFocus, true);
  }, [isOpen]);

  const handleSubmit = (e) => {
    e.preventDefault();
    const data = new FormData(e.target);
    const payload = Object.fromEntries(data.entries());
    console.log("Wysłane dane:", payload);
    // tu możesz dodać walidację / wysyłkę do API
    setIsOpen(false);
  };

  return (
    <div className="p-6">
      <button
        onClick={() => setIsOpen(true)}
        className="inline-block rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-400"
      >
        Otwórz formularz
      </button>

      {isOpen && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center"
          aria-modal="true"
          role="dialog"
        >
          {/* overlay */}
          <div
            className="absolute inset-0 bg-black/50"
            onClick={() => setIsOpen(false)}
          />

          {/* modal */}
          <div
            ref={modalRef}
            tabIndex={-1}
            className="relative z-10 w-full max-w-lg rounded-lg bg-white p-6 shadow-2xl"
            onClick={(e) => e.stopPropagation()} // zapobiega zamykaniu przy kliknięciu wewnątrz
          >
            {/* close X */}
            <button
              onClick={() => setIsOpen(false)}
              aria-label="Zamknij"
              className="absolute right-3 top-3 rounded-md p-1 text-gray-600 hover:bg-gray-100 focus:outline-none"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path
                  fillRule="evenodd"
                  d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                  clipRule="evenodd"
                />
              </svg>
            </button>

            <h2 className="mb-4 text-xl font-semibold">Formularz kontaktowy</h2>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Imię</label>
                <input
                  ref={firstFieldRef}
                  name="name"
                  type="text"
                  required
                  className="w-full rounded border px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-300"
                  placeholder="Twoje imię"
                />
              </div>

              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Email</label>
                <input
                  name="email"
                  type="email"
                  required
                  className="w-full rounded border px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-300"
                  placeholder="email@przyklad.pl"
                />
              </div>

              <div>
                <label className="mb-1 block text-sm font-medium text-gray-700">Wiadomość</label>
                <textarea
                  name="message"
                  rows={4}
                  className="w-full rounded border px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-300"
                  placeholder="Napisz wiadomość..."
                />
              </div>

              <div className="flex items-center gap-2">
                <input id="subscribe" name="subscribe" type="checkbox" className="h-4 w-4" />
                <label htmlFor="subscribe" className="text-sm text-gray-700">
                  Zapisz mnie do newslettera
                </label>
              </div>

              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setIsOpen(false)}
                  className="rounded border px-4 py-2 hover:bg-gray-50 focus:outline-none"
                >
                  Anuluj
                </button>
                <button
                  type="submit"
                  className="rounded bg-blue-600 px-4 p