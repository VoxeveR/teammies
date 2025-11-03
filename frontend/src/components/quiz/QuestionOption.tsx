function QuestionOption(isSelected: boolean) {
      return (
            <button key={index} className='block h-full w-full rounded-[15px] border bg-[#083335] p-2 text-5xl font-bold text-white'>
                  {String.fromCharCode(65 + index)}. {option}
            </button>
      );
}

export default QuestionOption;
