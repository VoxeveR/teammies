function QuizJoin() {
      return (
            <div className='flex h-full w-full items-center justify-center overflow-hidden lg:opacity-75'>
                  <div className='h-full w-full rounded-[30px] bg-[#CAF5F7] p-6 lg:h-75 lg:w-175'>
                        <div className='flex h-full w-full flex-col items-center justify-center'>
                              <div className='max-w-full text-center text-3xl leading-tight wrap-break-word hyphens-auto sm:text-4xl md:text-5xl lg:text-6xl'>Join With Code</div>
                              <input placeholder='Your Code' type='text' className='w-full rounded-[30px] bg-[#083335] p-4 text-white sm:text-4xl md:text-5xl lg:h-1/2 lg:w-full lg:text-6xl'></input>
                        </div>
                  </div>
            </div>
      );
}

export default QuizJoin;
