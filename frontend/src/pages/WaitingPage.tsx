function WaitingPage() {
      return (
            <div className='flex h-screen w-full flex-col items-center justify-center-safe'>
                  <div className='bg-quiz-white flex h-128 w-lg flex-col items-center justify-center gap-8 rounded-lg shadow-md'>
                        <img src='/src/assets/logo.svg' className='h-32 w-32 animate-spin [animation-duration:1.5s] lg:h-64 lg:w-64' />
                        <div className='text-quiz-green text-4xl'>WAITING FOR START :D</div>
                  </div>
            </div>
      );
}

export default WaitingPage;
